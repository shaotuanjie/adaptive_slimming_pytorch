# pytorch-slimming


## Baseline 

```shell
python main.py
```

## Trained with Sparsity

```shell
python main.py -sr --s 0.0001
```

## Pruned

```shell
python prune.py --model model_best.pth.tar --save pruned.pth.tar --percent 0.7
```

## Fine-tuned

```shell
python main.py --refine pruned.pth.tar --epochs 40
```

## Reference
