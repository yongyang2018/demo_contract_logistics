package com.yongyang.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yongyang.contract.ExpressContract;
import com.yongyang.contract.Order;
import com.yongyang.contract.OrderPayload;
import com.yongyang.contract.Sender;
import com.yongyang.demo.Constants;
import com.yongyang.demo.ExpressConfig;
import com.yongyang.demo.Start;
import com.yongyang.demo.WasmContracts;
import com.yongyang.demo.type.OrderPost;
import com.yongyang.demo.type.SenderPost;
import com.yongyang.demo.util.CommonUtil;
import com.yongyang.demo.util.HttpUtil;
import com.yongyang.demo.util.WasmContractsManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.tdf.common.util.HexBytes;
import org.tdf.rlp.RLPCodec;
import org.tdf.sunflower.consensus.poa.PoAConstants;
import org.tdf.sunflower.types.CryptoContext;
import org.tdf.sunflower.types.Transaction;

import java.util.ArrayList;
import java.util.Collections;

@RestController
@RequestMapping("/api/express")
@RequiredArgsConstructor
@Slf4j(topic = "express-controller")
public class ExpressController {
    private final HttpUtil httpUtil;
    private final ObjectMapper objectMapper;
    private final ExpressConfig expressConfig;
    private final WasmContractsManager manager;
    private final WasmContracts wasmContracts;
    private String contractAddress;

    @PostMapping
    public void init(){
        this.contractAddress = wasmContracts.get("express").getAddress();
    }

    private Transaction createCallExpressTransaction(){
        return new Transaction(
                PoAConstants.TRANSACTION_VERSION,
                Transaction.Type.CONTRACT_CALL.code,
                System.currentTimeMillis() / 1000,
                httpUtil.getLatestNonce(expressConfig.getAddress().toHex()) + 1,
                expressConfig.getPublicKey(),
                0, 0,
                HexBytes.EMPTY,
                HexBytes.fromHex(contractAddress),
                HexBytes.EMPTY
        );
    }

    private Transaction createTransaction() {
        return new Transaction(
                PoAConstants.TRANSACTION_VERSION,
                Transaction.Type.CONTRACT_CALL.code,
                System.currentTimeMillis() / 1000,
                httpUtil.getLatestNonce(expressConfig.getAddress().toHex()) + 1,
                expressConfig.getPublicKey(),
                0, 0,
                HexBytes.EMPTY,
                ExpressContract.CONTRACT_ADDRESS,
                HexBytes.EMPTY
        );
    }

    private void sign(Transaction tx) {
        byte[] sig = CryptoContext.sign(expressConfig.getPrivateKey().getBytes(), tx.getSignaturePlain());
        tx.setSignature(HexBytes.fromBytes(sig));
    }

    // 查看寄件人，如果寄件人没上链返回 null
    @GetMapping("/sender")
    @SneakyThrows
    public Sender getSender() {

        String resp = httpUtil.get(
                Start.JSON_CONTENT_TYPE,
                Constants.getEntryPoint() + "/rpc/contract/" + contractAddress ,
                CommonUtil.getParameter("getSender")
        );
        resp = httpUtil.parseData(resp, String.class);
        return RLPCodec.decode(HexBytes.decode(resp), Sender.class);
    }

    // 注册寄件人
    @PostMapping("/sender")
    @SneakyThrows
    public String createSender(@RequestBody SenderPost senderPost) {
        Sender sender = new Sender(
                expressConfig.getAddress(),
                senderPost.getType(),
                senderPost.getName(),
                senderPost.getId(),
                senderPost.getPhone(),
                senderPost.getDescription()
        );

        Transaction tx = createTransaction();
        tx.setPayload(HexBytes.fromHex("00").concat(HexBytes.fromBytes(RLPCodec.encode(sender))));
        sign(tx);
        return httpUtil.sendTransaction(tx);
    }

    // 查询物流单
    // 如果物流单没上链返回 null
    @GetMapping("/order")
    @SneakyThrows
    public Order getOrder() {
        String resp = httpUtil.post(
                Start.JSON_CONTENT_TYPE,
                Constants.getEntryPoint() + "/rpc/contract/" + ExpressContract.CONTRACT_ADDRESS,
                Collections.emptyMap(),
                objectMapper.writeValueAsString(
                        CommonUtil.singletonMap("method", "order")
                )
        );
        return httpUtil.parseData(resp, Order.class);
    }

    // 创建物流单
    @PostMapping("/order")
    @SneakyThrows
    public String createOrder(@RequestBody OrderPost post) {
        Order o = new Order(
                post.getId(),
                expressConfig.getAddress(),
                post.getFrom(),
                post.getTo(),
                0,
                HexBytes.empty(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        Transaction tx = createTransaction();
        tx.setPayload(HexBytes.fromHex("01").concat(HexBytes.fromBytes(RLPCodec.encode(o))));
        sign(tx);
        return httpUtil.sendTransaction(tx);
    }

    // 创建物流单
    @PatchMapping("/order")
    @SneakyThrows
    public String createOrder(@RequestBody String body) {
        OrderPayload op = new OrderPayload("id", System.currentTimeMillis() / 1000, body);
        Transaction tx = createTransaction();
        tx.setPayload(HexBytes.fromHex("02").concat(HexBytes.fromBytes(RLPCodec.encode(op))));
        sign(tx);
        return httpUtil.sendTransaction(tx);
    }

    // 重置
    @PostMapping("/reset")
    public String reset() {
        Transaction tx = createTransaction();
        tx.setPayload(HexBytes.fromHex("03"));
        sign(tx);
        return httpUtil.sendTransaction(tx);
    }
}
