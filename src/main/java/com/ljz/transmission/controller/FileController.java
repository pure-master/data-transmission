package com.ljz.transmission.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@Slf4j
public class FileController {

    @Value("${customConfig.file.path}")
    private String fileStoragePath;

    @Autowired
    private Lock lock;

    /**
     * download file
     * @param filename
     * @return
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws UnsupportedEncodingException {
        // 从服务器文件服务获取文件
        Resource file = getFileFromServer(filename);

        // 设置下载响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        String encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        headers.setContentDispositionFormData("attachment", encodedFilename);

        // 返回响应实体
        log.info("file:{} downloaded!",encodedFilename);
        return ResponseEntity.ok()
                .headers(headers)
                .body(file);
    }

    /**
     * upload file
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            lock.lock();
            // 获取文件的字节数组并保存到指定路径
            byte[] bytes = file.getBytes();
            Path path = Paths.get(fileStoragePath + file.getOriginalFilename());
            Files.write(path, bytes);
            return "File uploaded successfully";
        } catch (IOException e) {
            e.printStackTrace();
            return "File upload failed";
        }finally {
            lock.unlock();
        }
    }

    public Resource getFileFromServer(String filename) {
        try {
            // 构建文件路径
            Path filePath = Paths.get(fileStoragePath).resolve(filename);

            Resource fileResource = new FileSystemResource(filePath);

            if (fileResource.exists() && fileResource.isReadable()) {
                return fileResource;
            } else {
                throw new FileNotFoundException("File not found or cannot be accessed: " + filename);
            }
        } catch (Exception e) {
            log.info("Failed to access file: " + filename);
            throw new RuntimeException();
        }
    }
}
