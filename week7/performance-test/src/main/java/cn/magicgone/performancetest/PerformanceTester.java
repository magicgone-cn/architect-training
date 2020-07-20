package cn.magicgone.performancetest;

import cn.magicgone.performancetest.execute.Response;
import cn.magicgone.performancetest.factory.RequestQueue;
import cn.magicgone.performancetest.factory.RequestQueueConfig;
import cn.magicgone.performancetest.report.PerformanceReport;

import java.util.List;

public abstract class PerformanceTester {

    public PerformanceReport test(TestConfig testConfig){

        // 构造请求队列
        RequestQueueConfig requestQueueConfig = new RequestQueueConfig();
        requestQueueConfig.setUrl(testConfig.getUrl());
        requestQueueConfig.setLength(testConfig.getTotalTime());
        RequestQueue requestQueue = getRequestQueue(requestQueueConfig);

        // 执行请求队列
        List<Response> responseList = execute(requestQueue, testConfig.getConcurrentNumber());

        // 生成测试报告
        return report(responseList);

    }

    /**
     * 构造请求队列
     * @param requestQueueConfig
     * @return
     */
    protected abstract RequestQueue getRequestQueue(RequestQueueConfig requestQueueConfig);

    /**
     * 执行请求队列
     * @param requestQueue
     * @param concurrentNumber
     * @return
     */
    protected abstract List<Response> execute(RequestQueue requestQueue, int concurrentNumber);

    /**
     * 生成测试报告
     * @param responseList
     * @return
     */
    protected abstract PerformanceReport report(List<Response> responseList);


}
