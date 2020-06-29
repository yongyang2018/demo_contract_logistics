package com.yongyang.demo;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.tdf.common.util.HexBytes;
import org.tdf.sunflower.state.Address;
import org.tdf.sunflower.types.CryptoContext;

@ConfigurationProperties(prefix = "demo-config.express")
@Data
@Component
public class ExpressConfig {
    private String senderPrivateKey;


    public HexBytes getPrivateKey() {
        return HexBytes.fromHex(senderPrivateKey);
    }

    public HexBytes getPublicKey() {
        return HexBytes.fromBytes(CryptoContext.getPkFromSk(HexBytes.decode(senderPrivateKey)));
    }

    public HexBytes getAddress() {
        return Address.fromPublicKey(getPublicKey());
    }
}
