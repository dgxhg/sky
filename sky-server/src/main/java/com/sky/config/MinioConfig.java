package com.sky.config;

import com.sky.properties.MinioProperties;
import com.sky.utils.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfig {

    /**
     * 创建 MinioUtil 对象，交给 Spring 管理
     */
    @Bean
    @ConditionalOnMissingBean
    public MinioUtil minioUtil(MinioProperties minioProperties) {
        log.info("开始创建minio工具类对象:{}", minioProperties);
        return new MinioUtil(
                minioProperties.getEndpoint(),
                minioProperties.getAccessKeyId(),
                minioProperties.getAccessKeySecret(),
                minioProperties.getBucketName()
        );
    }
}