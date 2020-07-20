package cn.magicgone.performancetest;

import lombok.Data;

@Data
public class TestConfig {
    private String url;
    private Integer totalTime;
    private Integer concurrentNumber;
}
