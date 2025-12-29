package com.smartmoldguard.ai.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class InfluxDBConfig {

    @Value("${influx.url}")
    private String url;

    @Value("${influx.token}")
    private String token;

    @Value("${influx.org}")
    private String org;

    @Value("${influx.bucket}")
    private String bucket;

    @Bean
    @SuppressWarnings("null")
    public InfluxDBClient influxDBClient() {
        String effectiveUrl = Objects.requireNonNull(url, "influx.url 不能为空");
        String effectiveToken = Objects.requireNonNull(token, "influx.token 不能为空");
        String effectiveOrg = Objects.requireNonNull(org, "influx.org 不能为空");
        String effectiveBucket = Objects.requireNonNull(bucket, "influx.bucket 不能为空");
        return InfluxDBClientFactory.create(effectiveUrl, effectiveToken.toCharArray(), effectiveOrg, effectiveBucket);
    }
}
