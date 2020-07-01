package com.yongyang.demo.util;

import com.yongyang.demo.ExpressConfig;
import com.yongyang.demo.WasmContracts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tdf.common.util.HexBytes;
import org.tdf.sunflower.consensus.poa.PoAConstants;
import org.tdf.sunflower.types.Transaction;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "wasm")
public class WasmContractsManager {
    private final HttpUtil httpUtil;
    private final WasmContracts wasmContracts;
    private final ExpressConfig expressConfig;
    private final TransactionUtil transactionUtil;
    private final ASCWrapper ascWrapper;

    @PostConstruct
    public void init() {
        long startNonce =
                httpUtil.getLatestNonce(expressConfig.getAddress().toHex())
                + 1;
        for (String contract : wasmContracts.values()) {
            deploy(startNonce, contract);
            startNonce++;
        }
    }

    public void deploy(long nonce, String file) {
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
        log.info("deploy contract " + file);
        log.info(httpUtil.sendTransaction(tx));
    }
}
