package com.yongyang.demo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@ConfigurationProperties(prefix = "demo-config")
@Data
@Component
public class DemoConfig {
    private List<String> mappings;

    // 重定向配置
    private List<Map.Entry<String, String>> redirects;

    // 反向代理配置
    private List<Map.Entry<String, String>> proxy;

    // 静态资源目录
    private List<Map.Entry<String, String>> resources;

    @PostConstruct
    public void init() {
        if (mappings == null || mappings.isEmpty()) {
            this.proxy = Collections.emptyList();
            this.redirects = Collections.emptyList();
            this.resources = Collections.emptyList();
            return;
        }

        this.proxy = new ArrayList<>();
        this.redirects = new ArrayList<>();
        this.resources = new ArrayList<>();

        for (String mapping : mappings) {
            Type t = null;
            if (mapping.startsWith("redirect:")) {
                mapping = mapping.substring("redirect:".length());
                t = Type.REDIRECT;
            }
            if (mapping.startsWith("proxy:")) {
                mapping = mapping.substring("proxy:".length());
                t = Type.PROXY;
            }
            if (t == null) {
                t = Type.RESOURCE;
            }
            String[] kv = mapping.split(":");
            if (kv.length != 2)
                throw new RuntimeException("invalid mapping " + mapping);
            Map.Entry<String, String> e = new AbstractMap.SimpleImmutableEntry<>(
                    kv[0], kv[1]
            );
            switch (t) {
                case PROXY:
                    this.proxy.add(e);
                    break;
                case REDIRECT:
                    this.redirects.add(e);
                    break;
                case RESOURCE:
                    this.resources.add(e);
                    break;
            }
        }
    }

    private enum Type {
        REDIRECT, PROXY, RESOURCE
    }
}
