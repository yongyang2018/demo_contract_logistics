package com.yongyang.demo;

import com.yongyang.demo.type.WASMContract;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@ConfigurationProperties(prefix = "demo-config.wasm-contracts")
@Component
public class WASMContracts extends HashMap<String, WASMContract> {
}
