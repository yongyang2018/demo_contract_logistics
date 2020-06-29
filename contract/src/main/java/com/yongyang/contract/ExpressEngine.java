package com.yongyang.contract;

import com.fasterxml.jackson.databind.JsonNode;
import org.tdf.common.util.HexBytes;
import org.tdf.sunflower.consensus.poa.PoA;

import java.util.Collections;
import java.util.Properties;

public class ExpressEngine extends PoA {
    private final ExpressContract expressContract;

    public ExpressEngine() {
        super();
        expressContract = new ExpressContract();
        getPreBuiltContracts()
                .addAll(Collections.singletonList(expressContract));
    }

    @Override
    public void init(Properties properties) {
        super.init(properties);
        expressContract.setAccountTrie(getAccountTrie());
        expressContract.setContractStorageTrie(getContractStorageTrie());
    }

    @Override
    public Object rpcQuery(HexBytes address, JsonNode body) {
        Object ret = super.rpcQuery(address, body);
        if(ret != UNRESOLVED)
            return ret;
        byte[] stateRoot = getSunflowerRepository().getBestBlock().getStateRoot().getBytes();
        String method = body.get("method").asText();
        switch (method){
            case "sender":
                return expressContract.getSender(stateRoot).orElse(null);
            case "order":
                return expressContract.getOrder(stateRoot).orElse(null);
        }
        return null;
    }

    @Override
    public String getName() {
        return "express-engine";
    }
}
