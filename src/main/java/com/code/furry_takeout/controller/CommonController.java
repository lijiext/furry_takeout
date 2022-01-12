package com.code.furry_takeout.controller;

import com.code.furry_takeout.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("common")
public class CommonController {
    @Value("${furry_takeout.path}")
    private String StoragePath;

    @PostMapping("upload")
    public R<String> UploadFile(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        assert originalFileName != null;
        String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + suffix;
        File dir = new File(StoragePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        multipartFile.transferTo(new File(StoragePath + newFileName));
        log.info("上传文件成功，文件名{}，文件路径{}", newFileName, StoragePath);
        return R.success(newFileName);
    }

    @GetMapping("download")
    public void DownloadFile(String name, HttpServletResponse httpServletResponse) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(StoragePath + name);
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        byte[] bytes = FileCopyUtils.copyToByteArray(fileInputStream);
        httpServletResponse.setContentType("application/octet-stream");
        servletOutputStream.write(bytes, 0, bytes.length);
        servletOutputStream.close();
        log.info("下载文件成功，文件名{}", name);
    }
}
