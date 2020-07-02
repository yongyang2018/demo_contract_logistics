package com.yongyang.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPayload {
    private String id;
    private long timestamp;
    private String description;
}
