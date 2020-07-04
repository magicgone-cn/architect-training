package cn.magicgone;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

@Slf4j
public class DistributedCacheTest {

    private final int KEY_NUM = (int) Math.pow(10,6); // key数量
    private final int NODE_NUM = 10; // 节点数
    private final int VIRTUAL_NODE_NUM = 200; // 虚拟节点数

    @Test
    public void score() {

        // 生成测试数据
        log.info("生成测试数据");
        KeyGenerator keyGenerator = new GuidKeyGenerator();
        List<String> keys = keyGenerator.get(KEY_NUM);
        log.info("测试数据完成，{}",keys.size());

        // 将key装载到Cache中
        log.info("装载cache");
        DistributedCache distributedCache = new DistributedCacheImpl(NODE_NUM,VIRTUAL_NODE_NUM);
        distributedCache.put(keys);
        log.info("装载完成");

        // 计算得分
        DistributedCacheScore distributedCacheScore = (DistributedCacheScore) distributedCache;
        log.info("结果集: {}",distributedCacheScore.getScoreDataset());
        log.info("标准差: {}",distributedCacheScore.score());
    }
}
