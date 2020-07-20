package cn.magicgone.performancetest.execute;

import cn.magicgone.performancetest.factory.RequestQueue;

import java.util.List;

public interface RequestQueueExecutor {
    List<Response> execute(RequestQueue requestQueue, int concurrentNumber);
}
