package com.yongyang.demo.util;

import java.util.HashMap;
import java.util.Map;

public class CommonUtil {
    public static <K, V> Map<K, V> singletonMap(K k, V v) {
        Map<K, V> ret = new HashMap<>();
        ret.put(k, v);
        return ret;
    }
}
