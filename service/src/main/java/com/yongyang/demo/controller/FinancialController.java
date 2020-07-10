package com.yongyang.demo.controller;

import com.yongyang.demo.Constants;
import com.yongyang.demo.ExpressConfig;
import com.yongyang.demo.Start;
import com.yongyang.demo.WASMContracts;
import com.yongyang.demo.type.Supplier;
import com.yongyang.demo.util.CommonUtil;
import com.yongyang.demo.util.HttpUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.tdf.common.util.HexBytes;
import org.tdf.rlp.RLPCodec;
import org.tdf.sunflower.consensus.poa.PoAConstants;
import org.tdf.sunflower.types.CryptoContext;
import org.tdf.sunflower.types.Transaction;

@RestController
@RequestMapping("/api/financial")
@RequiredArgsConstructor
public class FinancialController {
    private final WASMContracts wasmContracts;
    private final ExpressConfig expressConfig;
    private final HttpUtil httpUtil;

    public String getContractAddress() {
        String ret = wasmContracts.get("financial").getAddress();
        if (ret == null || ret.isEmpty())
            throw new RuntimeException("express contract has not deployed yet");
        return ret;
    }

    @Data
    public static class Confirm{
        // 事件戳
        private long timestamp;
        // 区块高度
        private long height;
        // 事务哈希
        private HexBytes hash;
    }


    // 这是构造事务的公共方法，根据不同的共识在事务中填写区块链相关的参数
    // 这里选择的共识机制是 POA
    private Transaction createTransaction() {
        return new Transaction(
                PoAConstants.TRANSACTION_VERSION,
                Transaction.Type.CONTRACT_CALL.code,
                System.currentTimeMillis() / 1000,
                httpUtil.getLatestNonce(expressConfig.getAddress().toHex()) + 1,
                expressConfig.getPublicKey(),
                0, 0,
                HexBytes.EMPTY,
                HexBytes.fromHex(getContractAddress()),
                HexBytes.EMPTY
        );
    }

    // 在链上查询供应商的信息
    @GetMapping("/supplier")
    public Supplier getSupplier() {
        String resp = httpUtil.get(
                Start.JSON_CONTENT_TYPE,
                Constants.getEntryPoint() + "/rpc/contract/" + getContractAddress(),
                CommonUtil.getParameter("getSupplier")
        );
        resp = httpUtil.parseData(resp, String.class);
        return RLPCodec.decode(HexBytes.decode(resp), Supplier.class);
    }

    private void sign(Transaction tx) {
        byte[] sig = CryptoContext.sign(expressConfig.getPrivateKey().getBytes(), tx.getSignaturePlain());
        tx.setSignature(HexBytes.fromBytes(sig));
    }

    // 当供应商提交表单后，构造事务调用合约，将供应商的表单信息保存到链上
    @PostMapping(value = "/supplier", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String saveSupplier(@RequestBody Supplier supplier) {
        Transaction t = createTransaction();
        t.setPayload(CommonUtil.createPayload("saveSupplier", RLPCodec.encode(supplier)));
        sign(t);
        return httpUtil.sendTransaction(t);
    }

    // 当核心企业在核心企业页面上点击确认后，构造确认事务发送到链上，完成确认
    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String saveConfirm(@RequestBody Confirm c){
        Transaction t = createTransaction();
        t.setPayload(CommonUtil.createPayload("saveConfirm", RLPCodec.encode(c)));
        sign(t);
        return httpUtil.sendTransaction(t);
    }

    @PostMapping(value = "/reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String reset(){
        Transaction t = createTransaction();
        t.setPayload(CommonUtil.createPayload("reset", HexBytes.EMPTY_BYTES));
        sign(t);
        return httpUtil.sendTransaction(t);
    }

    // 查看供应商的表单是否已经被核心企业认证
    @GetMapping(value = "/confirm")
    public Confirm getConfirm(){
        String resp = httpUtil.get(
                Start.JSON_CONTENT_TYPE,
                Constants.getEntryPoint() + "/rpc/contract/" + getContractAddress(),
                CommonUtil.getParameter("getConfirm")
        );
        resp = httpUtil.parseData(resp, String.class);
        return RLPCodec.decode(HexBytes.decode(resp), Confirm.class);
    }
}
