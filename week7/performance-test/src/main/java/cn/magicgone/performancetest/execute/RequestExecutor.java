package cn.magicgone.performancetest.execute;

import cn.magicgone.performancetest.factory.Request;

public interface RequestExecutor {
    Response execute(Request request);
}
