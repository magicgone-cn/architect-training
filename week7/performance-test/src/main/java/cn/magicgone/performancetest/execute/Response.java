package cn.magicgone.performancetest.execute;

import lombok.Data;

import java.util.Date;

@Data
public class Response {
    private String url;
    private Date startTime;
    private Date endTime;
    private Integer responseTime;
}
