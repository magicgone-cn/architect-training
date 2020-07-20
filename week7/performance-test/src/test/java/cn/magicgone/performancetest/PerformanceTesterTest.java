package cn.magicgone.performancetest;

import cn.magicgone.performancetest.report.PerformanceReport;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class PerformanceTesterTest {

    @Autowired
    private PerformanceTester performanceTester;

    @Test
    void test1() {
        TestConfig testConfig = new TestConfig();
        testConfig.setUrl("https://www.baidu.com");
        testConfig.setTotalTime(1000);
        testConfig.setConcurrentNumber(10);
        PerformanceReport performanceReport = performanceTester.test(testConfig);
        log.info("平均响应时间:{}",performanceReport.getAverageResponseTime());
        log.info("95%响应时间:{}",performanceReport.getPercent95ResponseTime());
    }
}
