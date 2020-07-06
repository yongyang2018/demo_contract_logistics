package com.yongyang.demo.type;

import lombok.Data;
import org.tdf.common.util.HexBytes;

@Data
public class Confirm {
    private String description;
    private long timestamp;
    private long height;
    private HexBytes hash;
}
