package cn.magicgone;

/**
 * 分布式缓存
 */
public interface DistributedCache {
    void put(Iterable<String> iterable);
}
