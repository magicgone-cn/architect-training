package cn.magicgone.performancetest.factory;

import lombok.Data;

@Data
public class RequestQueueConfig {
    private String url;
    private Integer length;
}
