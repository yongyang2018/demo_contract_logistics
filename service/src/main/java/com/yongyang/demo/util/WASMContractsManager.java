package com.yongyang.demo.util;

import com.yongyang.demo.ExpressConfig;
import com.yongyang.demo.WASMContracts;
import com.yongyang.demo.type.WASMContract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tdf.common.util.HexBytes;
import org.tdf.sunflower.consensus.poa.PoAConstants;
import org.tdf.sunflower.types.Transaction;

import javax.annotation.PostConstruct;

// 启动时部署wasm智能合约
@Service
@RequiredArgsConstructor
@Slf4j(topic = "wasm")
public class WASMContractsManager {
    private final HttpUtil httpUtil;
    private final WASMContracts wasmContracts;
    private final ExpressConfig expressConfig;
    private final TransactionUtil transactionUtil;
    private final ASCWrapper ascWrapper;

    @PostConstruct
    public void init() {
        if(wasmContracts.values().isEmpty() ||
                wasmContracts
                        .values()
                        .stream()
                        .allMatch(x -> x.getAddress() != null && !x.getAddress().isEmpty())
        ){
            return;
        }
        long startNonce =
                httpUtil.getLatestNonce(expressConfig.getAddress().toHex())
                        + 1;
        for (WASMContract contract : wasmContracts.values()) {
            if (contract.getAddress() != null && !contract.getAddress().isEmpty()) {
                continue;
            }
            HexBytes address = deploy(startNonce, contract.getSrc());
            contract.setAddress(address.toHex());
            startNonce++;
        }
    }

    // 部署合约 返回合约地址
    public HexBytes deploy(long nonce, String file) {
        Transaction tx = new Transaction(
                PoAConstants.TRANSACTION_VERSION,
                Transaction.Type.CONTRACT_DEPLOY.code,
                System.currentTimeMillis() / 1000,
                nonce,
                expressConfig.getPublicKey(),
                0, 0,
                HexBytes.EMPTY,
                HexBytes.EMPTY,
                HexBytes.EMPTY
        );

        byte[] payload = ascWrapper.compile(file);
        tx.setPayload(HexBytes.fromBytes(payload));
        transactionUtil.sign(tx);
        log.info("deploy contract " + file + " address = " + tx.createContractAddress());
        log.info(httpUtil.sendTransaction(tx));
        return tx.createContractAddress();
    }
}
