package com.yongyang.demo.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yongyang.demo.Constants;
import com.yongyang.demo.util.HttpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class ProxyController {
    private final HttpUtil httpUtil;
    private final ObjectMapper objectMapper;

    @PostMapping("/proxy")
    public String proxy(@RequestBody ProxyRequest req) throws Exception {
        String path = req.getPath();
        if (path == null || path.isEmpty())
            path = "/";
        if (!path.startsWith("/"))
            path = "/" + path;

        return httpUtil.request(
                req.getHttpMethod(),
                req.getHttpHeaders(),
                Constants.getEntryPoint() + path,
                req.getQuery() == null ? Collections.emptyMap() : Collections.emptyMap(),
                req.getBody() == null ? "" :
                        objectMapper.writeValueAsString(req.getBody())
        );
    }
}
