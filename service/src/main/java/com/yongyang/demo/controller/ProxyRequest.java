package com.yongyang.demo.controller;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Data
public class ProxyRequest {
    private String method;
    private JsonNode body;
    private Map<String, String> headers;
    private Map<String, String> query;
    private String path;

    @JsonIgnore
    public HttpMethod getHttpMethod() {
        return (method == null || method.isEmpty()) ?
                HttpMethod.GET :
                HttpMethod.resolve(method.toUpperCase());
    }

    @JsonIgnore
    public HttpHeaders getHttpHeaders() {
        if (headers == null)
            return HttpHeaders.EMPTY;
        HttpHeaders ret = new HttpHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            ret.add(entry.getKey(), entry.getValue());
        }
        return ret;
    }
}
