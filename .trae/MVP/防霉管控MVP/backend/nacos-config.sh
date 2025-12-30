#!/bin/bash

# Default values
NACOS_ADDR="http://localhost:8848"
ENV_MODE="docker" # Default to docker mode

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --local)
            ENV_MODE="local"
            shift
            ;;
        --docker)
            ENV_MODE="docker"
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: ./nacos-config.sh [--local | --docker]"
            exit 1
            ;;
    esac
done

echo "Configuring Nacos for environment: $ENV_MODE"

# Set service addresses based on environment
if [ "$ENV_MODE" == "local" ]; then
    DB_HOST="localhost"
    KAFKA_HOST="localhost"
    REDIS_HOST="localhost"
    INFLUX_HOST="localhost"
    NACOS_HOST="localhost"
else
    DB_HOST="smg-postgres"
    KAFKA_HOST="smg-kafka"
    REDIS_HOST="smg-redis"
    INFLUX_HOST="smg-influxdb"
    NACOS_HOST="smg-nacos"
fi

# Function to publish config
publish_config() {
    data_id=$1
    group=$2
    content=$3
    echo "Publishing $data_id to $group..."
    curl -X POST "${NACOS_ADDR}/nacos/v1/cs/configs" \
        -d "dataId=${data_id}" \
        -d "group=${group}" \
        -d "content=${content}" \
        -d "type=yaml"
    echo ""
}

# shared-datasource.yaml
read -r -d '' CONTENT << EOM
spring:
  datasource:
    username: smg_admin
    password: smg_password_2025
    driver-class-name: org.postgresql.Driver
EOM
publish_config "shared-datasource.yaml" "DEFAULT_GROUP" "$CONTENT"

# shared-kafka.yaml
read -r -d '' CONTENT << EOM
spring:
  kafka:
    consumer:
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.add.type.headers: false
EOM
publish_config "shared-kafka.yaml" "DEFAULT_GROUP" "$CONTENT"

# gateway-service.yaml
read -r -d '' CONTENT << EOM
server:
  port: 9999
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: device-service
          uri: lb://device-service
          predicates:
            - Path=/api/v1/devices/**, /api/v1/asset-compensate/**, /api/v1/work-orders/**, /api/v1/alarms/**, /api/v1/provisioning/**, /api/v1/diagnostics/**
        - id: ai-service
          uri: lb://ai-service
          predicates:
            - Path=/api/v1/risk-prediction/**, /api/v1/prediction-feedback/**, /api/v1/risk/**, /api/v1/health-fingerprint/**, /api/v1/climate-configs/**
        - id: control-service
          uri: lb://control-service
          predicates:
            - Path=/api/v1/auto-mold-strategy/**, /api/v1/plans/**, /api/v1/control/**, /api/v1/spaces/**
        - id: subscription-service
          uri: lb://subscription-service
          predicates:
            - Path=/api/v1/subscription/**, /api/v1/subscriptions/**, /api/v1/points/**
        - id: report-service
          uri: lb://report-service
          predicates:
            - Path=/api/v1/reports/**, /api/v1/dashboard/**
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
EOM
publish_config "gateway-service.yaml" "DEFAULT_GROUP" "$CONTENT"

# device-service.yaml
read -r -d '' CONTENT << EOM
server:
  port: 8081
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/device_db
  kafka:
    bootstrap-servers: ${KAFKA_HOST}:9092
    consumer:
      group-id: device-group
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.smartmoldguard.device.domain.model
  configuration:
    map-underscore-to-camel-case: true
EOM
publish_config "device-service.yaml" "DEFAULT_GROUP" "$CONTENT"

# ai-service.yaml
read -r -d '' CONTENT << EOM
server:
  port: 8083
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/ai_db
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  kafka:
    bootstrap-servers: ${KAFKA_HOST}:9092
    consumer:
      group-id: ai-service-group
      auto-offset-reset: latest
influx:
  url: http://${INFLUX_HOST}:8086
  token: smg_influx_token_2025
  bucket: telemetry
  org: smartmoldguard
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
EOM
publish_config "ai-service.yaml" "DEFAULT_GROUP" "$CONTENT"

# control-service.yaml
read -r -d '' CONTENT << EOM
server:
  port: 8084
spring:
  kafka:
    bootstrap-servers: ${KAFKA_HOST}:9092
    consumer:
      group-id: control-service-group
      auto-offset-reset: latest
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
EOM
publish_config "control-service.yaml" "DEFAULT_GROUP" "$CONTENT"

# subscription-service.yaml
read -r -d '' CONTENT << EOM
server:
  port: 8085
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/subscription_db
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  kafka:
    bootstrap-servers: ${KAFKA_HOST}:9092
    consumer:
      group-id: subscription-service-group
      auto-offset-reset: latest
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
EOM
publish_config "subscription-service.yaml" "DEFAULT_GROUP" "$CONTENT"

# report-service.yaml
read -r -d '' CONTENT << EOM
server:
  port: 8087
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/report_db
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  kafka:
    bootstrap-servers: ${KAFKA_HOST}:9092
    consumer:
      group-id: report-service-group
      auto-offset-reset: latest
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
EOM
publish_config "report-service.yaml" "DEFAULT_GROUP" "$CONTENT"
