package com.lll_cxt.marketplace.secondhand.controller;

import com.lll_cxt.marketplace.secondhand.dto.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 上传单个图片
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            // 创建上传目录
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.error("文件名无效");
            }

            // 获取文件扩展名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 生成新的文件名（UUID + 扩展名）
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = Paths.get(uploadDir, newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 返回文件访问URL
            String fileUrl = "/api/uploads/" + newFilename;
            return Result.success("上传成功", fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传多个图片
     */
    @PostMapping("/images")
    public Result<List<String>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("上传文件不能为空");
        }

        List<String> fileUrls = new ArrayList<>();

        try {
            // 创建上传目录
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                // 获取原始文件名
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null) {
                    continue;
                }

                // 获取文件扩展名
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

                // 生成新的文件名（UUID + 扩展名）
                String newFilename = UUID.randomUUID().toString() + extension;

                // 保存文件
                Path filePath = Paths.get(uploadDir, newFilename);
                Files.copy(file.getInputStream(), filePath);

                // 添加文件访问URL
                String fileUrl = "/api/uploads/" + newFilename;
                fileUrls.add(fileUrl);
            }

            return Result.success("上传成功", fileUrls);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
