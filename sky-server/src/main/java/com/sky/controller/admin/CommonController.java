package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.MinioUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private MinioUtil minioUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file.getOriginalFilename());

        try {
            // 1. 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 2. 生成唯一文件名
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            log.info("suffix:{}", suffix);
            String objectName = UUID.randomUUID() + suffix;

            // 3. 上传文件（调用 MinioUtil，和 AliOSS 用法完全一致）
            String filePath = minioUtil.upload(file.getBytes(), objectName);
            log.info("filePath:{}",filePath);
            return Result.success(filePath);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}