package cn.magicgone.performancetest;

import cn.magicgone.performancetest.execute.RequestQueueExecutor;
import cn.magicgone.performancetest.execute.Response;
import cn.magicgone.performancetest.factory.RequestQueue;
import cn.magicgone.performancetest.factory.RequestQueueConfig;
import cn.magicgone.performancetest.factory.RequestQueueFactory;
import cn.magicgone.performancetest.report.PerformanceReport;
import cn.magicgone.performancetest.report.PerformanceReporter;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PerformanceTesterImpl extends PerformanceTester {

    private final RequestQueueFactory requestQueueFactory;
    private final RequestQueueExecutor requestQueueExecutor;
    private final PerformanceReporter performanceReporter;

    /**
     * 构造请求队列
     * @param requestQueueConfig
     * @return
     */
    @Override
    protected RequestQueue getRequestQueue(RequestQueueConfig requestQueueConfig) {
        return requestQueueFactory.getRequestQueue(requestQueueConfig);
    }

    /**
     * 执行请求队列
     * @param requestQueue
     * @param concurrentNumber
     * @return
     */
    @Override
    protected List<Response> execute(RequestQueue requestQueue, int concurrentNumber) {
        return requestQueueExecutor.execute(requestQueue, concurrentNumber);
    }

    /**
     * 生成测试报告
     * @param responseList
     * @return
     */
    @Override
    protected PerformanceReport report(List<Response> responseList) {
        return performanceReporter.report(responseList);
    }


}
