package com.health.platform.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${custom.upload-path}")
    private String uploadPath;

    @Value("${custom.upload-url-prefix}")
    private String uploadUrlPrefix;

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        if (file.isEmpty()) {
            result.put("code", 500);
            result.put("msg", "上传失败，请选择文件");
            return result;
        }

        String fileName = file.getOriginalFilename();
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffixName;

        File dest = new File(uploadPath + newFileName);

        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            file.transferTo(dest);
            result.put("code", 200);
            result.put("msg", "上传成功");
            result.put("url", uploadUrlPrefix + newFileName);
            return result;
        } catch (IOException e) {
            result.put("code", 500);
            result.put("msg", e.getMessage());
            return result;
        }
    }
}
