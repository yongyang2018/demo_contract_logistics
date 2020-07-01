package com.yongyang.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yongyang.demo.util.ASCWrapper;
import com.yongyang.demo.util.Init;
import org.apache.tika.Tika;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.tdf.sunflower.util.MappingUtil;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@SpringBootApplication
public class Start {
    public static final HttpHeaders JSON_CONTENT_TYPE = new HttpHeaders();
    public static final ObjectMapper MAPPER = MappingUtil.OBJECT_MAPPER;
    public static final Tika TIKA = new Tika();

    static {
        JSON_CONTENT_TYPE.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return MAPPER;
    }

    public static void main(String[] args) {
        Init.initCryptoContext();
        SpringApplication app = new SpringApplication(Start.class);
        app.addInitializers(applicationContext -> Constants.init(applicationContext.getEnvironment()));
        app.run(args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(
                Collections.singletonList(new StringHttpMessageConverter(StandardCharsets.UTF_8))
        );
    }

    @Bean
    public ASCWrapper ascWrapper(DemoConfig demoConfig){
        return (demoConfig.getAscPath() == null || demoConfig.getAscPath().isEmpty()) ?
                new ASCWrapper() :
                new ASCWrapper(demoConfig.getAscPath());
    }
}
