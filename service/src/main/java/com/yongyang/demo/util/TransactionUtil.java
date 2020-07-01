package com.yongyang.demo.util;

import com.yongyang.demo.ExpressConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tdf.common.util.HexBytes;
import org.tdf.sunflower.types.CryptoContext;
import org.tdf.sunflower.types.Transaction;

@Service
@RequiredArgsConstructor
public class TransactionUtil {
    private final ExpressConfig expressConfig;

    public void sign(Transaction tx){
        byte[] sig = CryptoContext.sign(expressConfig.getPrivateKey().getBytes(), tx.getSignaturePlain());
        tx.setSignature(HexBytes.fromBytes(sig));
    }
}
