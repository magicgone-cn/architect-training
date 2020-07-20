package cn.magicgone.performancetest.report;

import cn.magicgone.performancetest.execute.Response;
import lombok.Data;

import java.util.List;

@Data
public class PerformanceReport {
    private Float averageResponseTime;
    private Integer percent95ResponseTime;
    private List<Response> responseList;
}
