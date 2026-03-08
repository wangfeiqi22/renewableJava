package com.renewable.ai.controller;

import com.renewable.ai.entity.OrderPhoto;
import com.renewable.ai.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<OrderPhoto> uploadFile(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("orderId") Long orderId,
                                                 @RequestParam("nodeType") String nodeType) {
        return ResponseEntity.ok(fileStorageService.storeFile(file, orderId, nodeType));
    }
}
