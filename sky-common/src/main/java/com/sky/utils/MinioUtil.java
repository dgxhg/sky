package com.sky.utils;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class MinioUtil {

    private String endpoint;
    private String accessKeyId;
    private String secretKey;
    private String bucketName;

    /**
     * 文件上传（和 AliOssUtil 入参、出参、结构完全一致）
     * @param bytes 文件字节数组
     * @param objectName 文件名
     * @return 可访问的文件URL
     */
    public String upload(byte[] bytes, String objectName) {
        // 创建 MinioClient 实例（兼容7.x/8.x版本）
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKeyId, secretKey)
                .build();


        try {
            // 上传文件：使用旧版 PutObjectArgs，完美兼容所有版本
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO文件上传失败：{}", e.getMessage(), e);
            throw new RuntimeException("MinIO文件上传失败", e);
        }

        // 拼接文件访问路径（和阿里云格式保持一致）
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(endpoint)
                .append("/")
                .append(bucketName)
                .append("/")
                .append(objectName);
        log.info("文件上传到：{}", stringBuilder.toString());
        return stringBuilder.toString();
    }
}