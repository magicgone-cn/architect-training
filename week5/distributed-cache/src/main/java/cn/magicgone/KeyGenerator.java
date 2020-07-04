package cn.magicgone;

import java.util.List;

/**
 * 生成测试数据
 */
public interface KeyGenerator {
    List<String> get(int n);
}
