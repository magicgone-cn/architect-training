package cn.magicgone.performancetest;

import cn.magicgone.performancetest.execute.RequestExecutor;
import cn.magicgone.performancetest.execute.RequestExecutorImpl;
import cn.magicgone.performancetest.execute.RequestQueueExecutor;
import cn.magicgone.performancetest.execute.RequestQueueExecutorImpl;
import cn.magicgone.performancetest.factory.ConcurrentRequestQueueFactory;
import cn.magicgone.performancetest.factory.RequestQueueFactory;
import cn.magicgone.performancetest.report.PerformanceReporter;
import cn.magicgone.performancetest.report.PerformanceReporterImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public RequestQueueFactory requestQueueFactory(){
        return new ConcurrentRequestQueueFactory();
    }

    @Bean
    public RequestQueueExecutor requestQueueExecutor(){
        RequestExecutor requestExecutor = new RequestExecutorImpl();
        return new RequestQueueExecutorImpl(requestExecutor);
    }

    @Bean
    public PerformanceReporter performanceReporter(){
        return new PerformanceReporterImpl();
    }

    @Bean
    public PerformanceTester performanceTester(RequestQueueFactory requestQueueFactory, RequestQueueExecutor requestQueueExecutor, PerformanceReporter performanceReporter){
        return new PerformanceTesterImpl(requestQueueFactory, requestQueueExecutor, performanceReporter);
    }

}
