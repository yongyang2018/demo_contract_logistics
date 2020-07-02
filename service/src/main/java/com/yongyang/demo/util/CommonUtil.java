package com.yongyang.demo.util;

import org.tdf.common.util.HexBytes;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CommonUtil {
    public static <K, V> Map<K, V> singletonMap(K k, V v) {
        Map<K, V> ret = new HashMap<>();
        ret.put(k, v);
        return ret;
    }

    public static Map<String, String> getParameter(String method){
        Map<String, String> m = new HashMap<>();
        byte[] bytes = method.getBytes(StandardCharsets.US_ASCII);
        m.put("parameters",
                HexBytes.fromBytes(new byte[]{(byte)bytes.length})
                .concat(HexBytes.fromBytes(bytes))
                .toHex()
        );
        return m;
    }

    public HexBytes createPayload(String method, byte[] body){
        byte[] bytes = method.getBytes(StandardCharsets.US_ASCII);
        return HexBytes.fromBytes(new byte[]{(byte)bytes.length})
                .concat(HexBytes.fromBytes(bytes))
                .concat(HexBytes.fromBytes(body));
    }
}
