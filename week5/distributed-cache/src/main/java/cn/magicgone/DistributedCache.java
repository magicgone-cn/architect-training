package cn.magicgone;

/**
 * 分布式缓存
 */
public interface DistributedCache {
    void put(String key, Object value);
}
