import time
import os
import argparse
import torch
import torch.nn as nn
from torch.autograd import Variable
from torchvision import datasets, transforms

from vgg import vgg
import numpy as np
from thop import profile
# Prune settings
parser = argparse.ArgumentParser(description='PyTorch Slimming CIFAR prune')
parser.add_argument('--dataset', type=str, default='cifar10',
                    help='training dataset (default: cifar10)')
parser.add_argument('--test-batch-size', type=int, default=1000, metavar='N',
                    help='input batch size for testing (default: 1000)')
parser.add_argument('--no-cuda', action='store_true', default=False,
                    help='disables CUDA training')
parser.add_argument('--percent', type=float, default=0.5,
                    help='scale sparse rate (default: 0.5)')
parser.add_argument('--model', default='', type=str, metavar='PATH',
                    help='path to raw trained model (default: none)')
parser.add_argument('--save', default='', type=str, metavar='PATH',
                    help='path to save prune model (default: none)')
args = parser.parse_args()
args.cuda = not args.no_cuda and torch.cuda.is_available()

model = vgg()
if args.cuda:
    model.cuda()
if args.model:
    if os.path.isfile(args.model):
        print("=> loading checkpoint '{}'".format(args.model))
        checkpoint = torch.load(args.model)
        args.start_epoch = checkpoint['epoch']
        best_prec1 = checkpoint['best_prec1']
        model.load_state_dict(checkpoint['state_dict'])
        print("=> loaded checkpoint '{}' (epoch {}) Prec1: {:f}"
              .format(args.model, checkpoint['epoch'], best_prec1))
    else:
        print("=> no checkpoint found at '{}'".format(args.resume))

obtain_num_parameters = lambda model:sum([param.nelement() for param in model.parameters()])
initial_nparameters = obtain_num_parameters(model)

total = 0
for m in model.modules():
    if isinstance(m, nn.BatchNorm2d):
        total += m.weight.data.shape[0]

bn = torch.zeros(total)
index = 0
for m in model.modules():
    if isinstance(m, nn.BatchNorm2d):
        size = m.weight.data.shape[0]
        bn[index:(index+size)] = m.weight.data.abs().clone()
        index += size

y, i = torch.sort(bn)
thre_index = int(total * args.percent)
thre = y[thre_index]
print("thre:", thre)
pruned = 0
cfg = []
cfg_mask = []
for k, m in enumerate(model.modules()):
    if isinstance(m, nn.BatchNorm2d):
        weight_copy = m.weight.data.clone()  #process this layer, and keep the minumum channels in one layer.
        weight_copy_tmp = m.weight.data.abs().clone()
        tmp_y, tmp_i = torch.sort(weight_copy_tmp)
        thre_index_tmp =int (weight_copy_tmp.shape[0]*0.996)
        thre_tmp = tmp_y[thre_index_tmp]
        #thre_tmp = tmp_y[-2]
        if(thre_tmp>thre):
            mask = weight_copy.abs().gt(thre).float()
        else:
            mask = weight_copy.abs().gt(thre_tmp).float()
        if args.cuda:
            mask.cuda()
        pruned = pruned + mask.shape[0] - torch.sum(mask)
        m.weight.data.mul_(mask)
        m.bias.data.mul_(mask)
        cfg.append(int(torch.sum(mask)))
        cfg_mask.append(mask.clone())
        print('layer index: {:d} \t total channel: {:d} \t remaining channel: {:d}'.
            format(k, mask.shape[0], int(torch.sum(mask))))
    elif isinstance(m, nn.MaxPool2d):
        cfg.append('M')

pruned_ratio = pruned/total
print("prune_ration is", pruned_ratio)
print('Pre-processing Successful!')


# simple test model after Pre-processing prune (simple set BN scales to zeros)
def test():
    kwargs = {'num_workers': 1, 'pin_memory': True} if args.cuda else {}
    test_loader = torch.utils.data.DataLoader(
        datasets.CIFAR10('./data', train=False, transform=transforms.Compose([
            transforms.ToTensor(),
            transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))])),
        batch_size=args.test_batch_size, shuffle=True, **kwargs)
    model.eval()
    correct = 0
    with torch.no_grad():
        for data, target in test_loader:
            if args.cuda:
                data, target = data.cuda(), target.cuda()
            output = model(data)
            pred = output.data.max(1, keepdim=True)[1] # get the index of the max log-probability
            correct += pred.eq(target.data.view_as(pred)).cpu().sum()

    print('\nTest set: Accuracy: {}/{} ({:.1f}%)\n'.format(
        correct, len(test_loader.dataset), 100. * correct / len(test_loader.dataset)))
    return correct / float(len(test_loader.dataset))



# Make real prune
print(cfg)
newmodel = vgg(cfg=cfg)
if args.cuda:
    newmodel.cuda()

layer_id_in_cfg = 0
start_mask = torch.ones(3)
end_mask = cfg_mask[layer_id_in_cfg]
for [m0, m1] in zip(model.modules(), newmodel.modules()):
    if isinstance(m0, nn.BatchNorm2d):
        idx1 = np.squeeze(np.argwhere(np.asarray(end_mask.cpu().numpy())))
        m1.weight.data = m0.weight.data[idx1].clone()
        m1.bias.data = m0.bias.data[idx1].clone()
        m1.running_mean = m0.running_mean[idx1].clone()
        m1.running_var = m0.running_var[idx1].clone()
        layer_id_in_cfg += 1
        start_mask = end_mask.clone()
        if layer_id_in_cfg < len(cfg_mask):  # do not change in Final FC
            end_mask = cfg_mask[layer_id_in_cfg]
    elif isinstance(m0, nn.Conv2d):
        idx0 = np.squeeze(np.argwhere(np.asarray(start_mask.cpu().numpy())))
        idx1 = np.squeeze(np.argwhere(np.asarray(end_mask.cpu().numpy())))
        print('In shape: {:d} Out shape:{:d}'.format(idx0.shape[0], idx1.shape[0]))
        w = m0.weight.data[:, idx0, :, :].clone()
        w = w[idx1, :, :, :].clone()
        m1.weight.data = w.clone()
        # m1.bias.data = m0.bias.data[idx1].clone()
    elif isinstance(m0, nn.Linear):
        idx0 = np.squeeze(np.argwhere(np.asarray(start_mask.cpu().numpy())))
        m1.weight.data = m0.weight.data[:, idx0].clone()


torch.save({'cfg': cfg, 'state_dict': newmodel.state_dict()}, args.save)

pruned_model_parameters = obtain_num_parameters(newmodel)
print("initial model parameters:", initial_nparameters)
print("pruned_model_parameters:", pruned_model_parameters)
def obtain_avg_forward_time(input, model, repeat=500):
    model.eval()
    start = time.time()
    with torch.no_grad():
        for i in range(repeat):
            output = model(input)[0]
    avg_infer_time = (time.time() - start) / repeat

    return avg_infer_time, output

test()
