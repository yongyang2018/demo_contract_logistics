package com.yongyang.demo.controller;

import com.yongyang.demo.Constants;
import com.yongyang.demo.ExpressConfig;
import com.yongyang.demo.Start;
import com.yongyang.demo.WASMContracts;
import com.yongyang.demo.type.Confirm;
import com.yongyang.demo.type.Donor;
import com.yongyang.demo.util.CommonUtil;
import com.yongyang.demo.util.HttpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.tdf.common.util.HexBytes;
import org.tdf.rlp.RLPCodec;
import org.tdf.sunflower.consensus.poa.PoAConstants;
import org.tdf.sunflower.types.CryptoContext;
import org.tdf.sunflower.types.Transaction;

@RestController
@RequestMapping("/api/welfare")
@RequiredArgsConstructor
public class WelfareController {
    private final HttpUtil httpUtil;
    private final WASMContracts wasmContracts;
    private final ExpressConfig expressConfig;

    // 这里返回公益合约的地址
    public String getContractAddress() {
        String ret = wasmContracts.get("welfare").getAddress();
        if (ret == null || ret.isEmpty())
            throw new RuntimeException("welfare contract has not deployed yet");
        return ret;
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

    // 在链上查询捐赠人是否已经被红十字会确认
    @GetMapping("/confirm")
    public Confirm getConfirm() {
        String resp = httpUtil.get(
                Start.JSON_CONTENT_TYPE,
                Constants.getEntryPoint() + "/rpc/contract/" + getContractAddress(),
                CommonUtil.getParameter("getConfirm")
        );
        resp = httpUtil.parseData(resp, String.class);
        return RLPCodec.decode(HexBytes.decode(resp), Confirm.class);
    }

    // 当红十字会在页面上点击确认后，构造确认事务发送到链上
    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String createDonor(@RequestBody Confirm confirm) {
        Transaction tx = createTransaction();
        tx.setPayload(CommonUtil.createPayload("saveConfirm", RLPCodec.encode(confirm)));
        sign(tx);
        return httpUtil.sendTransaction(tx);
    }

    // 查询捐赠人填写的信息
    @GetMapping("/donor")
    public Donor getDonor() {
        String resp = httpUtil.get(
                Start.JSON_CONTENT_TYPE,
                Constants.getEntryPoint() + "/rpc/contract/" + getContractAddress(),
                CommonUtil.getParameter("getDonor")
        );
        resp = httpUtil.parseData(resp, String.class);
        return RLPCodec.decode(HexBytes.decode(resp), Donor.class);
    }

    // 当捐赠人提交表单后，构造事务调用合约，将捐赠人的表单保存到链上
    @PostMapping(value = "/donor", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String createDonor(@RequestBody Donor donor) {
       Transaction tx = createTransaction();
       tx.setPayload(CommonUtil.createPayload("saveDonor", RLPCodec.encode(donor)));
       sign(tx);
       return httpUtil.sendTransaction(tx);
    }

    // 重置所有状态
    @PostMapping(value = "/reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String reset() {
        Transaction tx = createTransaction();
        tx.setPayload(CommonUtil.createPayload("reset", HexBytes.EMPTY_BYTES));
        sign(tx);
        return httpUtil.sendTransaction(tx);
    }

    private void sign(Transaction tx) {
        byte[] sig = CryptoContext.sign(expressConfig.getPrivateKey().getBytes(), tx.getSignaturePlain());
        tx.setSignature(HexBytes.fromBytes(sig));
    }
}
