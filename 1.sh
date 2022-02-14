for prune_ration in 0.1 0.2 0.3 0.4 0.5 0.52 0.54 0.56 0.58 0.60 0.62 0.64 0.66 0.68 0.7 0.72 0.74 0.76 0.78 0.80 0.82 0.84 0.86 0.88 0.90
do
    echo "prune_ration" $prune_ration >>./result.txt
    python prune.py --model model_best.pth.tar --save pruned.pth.tar --percent $prune_ration | grep "set: Accuracy" >> ./result.txt 
done

~

