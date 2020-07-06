package com.yongyang.demo.type;

import lombok.Data;
import org.tdf.common.util.HexBytes;

@Data
public class Donor {
    // 链上分配的地址
    private HexBytes chainAddress;
    // 捐赠姓名
    private String name;
    // 捐赠内容
    private String content;
    // 捐赠数量
    private long quantity;
    // 简介信息
    private String info;
    // 捐赠地址
    private String address;
    // 受益人
    private String get;
    // 捐赠机构
    private String donor;
    // 上链高度
    private long height;
    // 上链事务的哈希
    private HexBytes hash;
    // 上链的时间戳
    private long timestamp;
}
