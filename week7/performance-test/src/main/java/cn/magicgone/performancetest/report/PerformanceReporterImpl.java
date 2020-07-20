package cn.magicgone.performancetest.report;

import cn.magicgone.performancetest.execute.Response;

import java.util.Comparator;
import java.util.List;

public class PerformanceReporterImpl implements PerformanceReporter {
    @Override
    public PerformanceReport report(List<Response> responseList) {
        PerformanceReport performanceReport = new PerformanceReport();
        performanceReport.setResponseList(responseList);

        // 计算平均响应时长
        Long sum = 0L;
        for(Response response : responseList){
            sum += response.getResponseTime();
        }
        performanceReport.setAverageResponseTime( (float) sum / responseList.size() );

        // 计算95%响应时长
        responseList.sort(Comparator.comparingInt(Response::getResponseTime));
        int pointer = (int) (responseList.size()*0.95);
        performanceReport.setPercent95ResponseTime(responseList.get(pointer).getResponseTime());

        return performanceReport;
    }
}
