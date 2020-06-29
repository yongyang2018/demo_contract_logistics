package com.yongyang.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tdf.common.util.HexBytes;

import java.util.List;

// 快递单
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    // 快递单号
    private String id;
    // 寄件人在链上的地址
    private HexBytes sender;
    // 寄件人寄件的位置
    private String from;
    // 收件人的位置
    private String to;
    // 上链高度
    private long height;
    // 上链区块哈希
    private HexBytes transactionHash;
    // 时间戳列表
    private List<Long> timestamps;
    // 时间戳对应描述
    private List<String> description;
}
