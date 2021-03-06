package com.yongyang.demo.type;

import lombok.Data;

import java.util.List;

@Data
public class OrderPost {
    private String id;
    private String from;
    private String to;
    private List<Long> timestamps;
    private List<String> descriptions;
}
