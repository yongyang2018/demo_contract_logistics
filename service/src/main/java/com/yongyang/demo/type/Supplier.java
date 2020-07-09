package com.yongyang.demo.type;

import lombok.Data;
import org.tdf.common.util.HexBytes;

@Data
public class Supplier {
    // 上链的地址
    private HexBytes address;
    // 企业名称
    private String supplierName;
    // 法人姓名
    private String legalName;
    // 法人证件
    private String legalId;
    // 融资金额
    private long amount;
    // 合同编号
    private String number;
    // 上链时间戳
    private long timestamp;
    // 上链事务的哈希值
    private HexBytes hash;
    // 上链的高度
    private long height;
}
