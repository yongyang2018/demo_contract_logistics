package com.yongyang.demo.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yongyang.demo.Constants;
import com.yongyang.demo.DemoConfig;
import com.yongyang.demo.Start;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.tdf.sunflower.types.Transaction;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class HttpUtil {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final DemoConfig demoConfig;

    /**
     * 发送 get 请求
     *
     * @param httpHeaders 头部
     * @param uri         uri
     * @param parameters  问号后面跟的参数
     * @return 响应体
     */
    public String get(HttpHeaders httpHeaders, @NonNull String uri, Map<String, String> parameters) {
        return request(HttpMethod.GET, httpHeaders, uri, parameters, "");
    }

    /**
     * 发送 post 请求
     *
     * @param httpHeaders 头部
     * @param uri         uri
     * @param parameters  问号后面跟的参数
     * @return 响应体
     */
    public String post(HttpHeaders httpHeaders, @NonNull String uri, Map<String, String> parameters, String body) {
        return request(HttpMethod.POST, httpHeaders, uri, parameters, body);
    }

    /**
     * 发起一次 http 请求
     *
     * @param method      请求方法 get/post/put/patch/delete
     * @param httpHeaders 头部
     * @param uri         uri
     * @param query       问号后面跟的参数
     * @param body        请求体
     * @return 响应体
     */
    public String request(
            HttpMethod method,
            HttpHeaders httpHeaders,
            String uri,
            Map<String, String> query,
            String body
    ) {
        RequestEntity<String> req = RequestEntity
                .method(method, buildURI(uri, query))
                .headers(httpHeaders)
                .body(body);

        return restTemplate
                .exchange(req, String.class)
                .getBody();
    }

    public URI buildURI(String uri, Map<String, String> query) {
        Map<String, List<String>> multiMap = new HashMap<>();
        query.forEach((x, y) -> multiMap.put(x, Collections.singletonList(y)));

        UriComponentsBuilder builder = UriComponentsBuilder
                .newInstance()
                .uri(URI.create(uri))
                .queryParams(new LinkedMultiValueMap<>(multiMap));
        return builder.build().toUri();
    }

    @SneakyThrows
    public String sendTransaction(Transaction transaction) {
        return post(
                Start.JSON_CONTENT_TYPE,
                Constants.getEntryPoint() + "/rpc/transaction",
                Collections.emptyMap(),
                objectMapper.writeValueAsString(transaction)
        );
    }

    @SneakyThrows
    public long getLatestNonce(String address) {
        JsonNode n = objectMapper.readValue(get(
                HttpHeaders.EMPTY,
                Constants.getEntryPoint() + "/rpc/account/" + address,
                Collections.emptyMap()
        ), JsonNode.class);
        if (n.get("code").asInt() >= 400)
            throw new RuntimeException(n.get("message").asText());

        return n.get("data").get("nonce").asLong();
    }

    @SneakyThrows
    public <T> T parseData(String resp, Class<T> clazz) {
        JsonNode n = objectMapper.readValue(resp, JsonNode.class);
        if (n.get("code").asInt() >= 400)
            throw new RuntimeException(n.get("message").asText());

        return objectMapper.convertValue(n.get("data"), clazz);
    }
}

