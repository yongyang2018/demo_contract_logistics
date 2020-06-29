package com.yongyang.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tdf.common.util.HexBytes;

// 寄件人
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sender {
    // 地址
    private HexBytes address;

    // 物品类型
    private String type;

    // 真实姓名
    private String name;

    // 身份证号
    private String id;

    // 电话号码
    private String phone;

    // 简单说明
    private String description;
}
