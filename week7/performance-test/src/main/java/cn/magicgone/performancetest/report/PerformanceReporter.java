package cn.magicgone.performancetest.report;

import cn.magicgone.performancetest.execute.Response;

import java.util.List;

public interface PerformanceReporter {
    PerformanceReport report(List<Response> responseList);
}
