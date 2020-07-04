package cn.magicgone;

public interface DistributedCacheScore {

    default double score(){
        int[] dataset = getScoreDataset();

        // 计算标准差
        int m=dataset.length;
        double sum=0;
        for (int value : dataset) {//求和
            sum += value;
        }
        double dAve=sum/m;//求平均值
        double dVar=0;
        for (int value : dataset) {//求方差
            dVar += (value - dAve) * (value - dAve);
        }
        return Math.sqrt(dVar/m);

    }

    int[] getScoreDataset();
}
