package cn.magicgone.performancetest.factory;

public interface RequestQueueFactory {
    RequestQueue getRequestQueue(RequestQueueConfig requestQueueConfig);
}
