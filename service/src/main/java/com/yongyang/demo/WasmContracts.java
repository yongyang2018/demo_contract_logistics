package com.yongyang.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@ConfigurationProperties(prefix = "demo-config.wasm-contracts")
@Component
public class WasmContracts extends HashMap<String, String> {
}
