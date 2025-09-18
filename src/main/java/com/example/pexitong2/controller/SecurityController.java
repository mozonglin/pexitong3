package com.example.pexitong2.controller;

import com.example.pexitong2.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/security")
@CrossOrigin(origins = "*")
public class SecurityController {
    
    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public ApiResponse<Map<String, Object>> getCaptcha() {
        try {
            // 生成简单的验证码（实际项目中应该生成图形验证码）
            String captchaText = generateCaptchaText();
            String captchaToken = "token_" + System.currentTimeMillis();
            
            // 模拟验证码图片（实际应该生成真实的图片）
            String captchaImage = generateMockCaptchaImage(captchaText);
            
            Map<String, Object> data = new HashMap<>();
            data.put("captchaToken", captchaToken);
            data.put("captchaImage", captchaImage);
            data.put("expiresAt", LocalDateTime.now().plusMinutes(5));
            
            return ApiResponse.success("验证码生成成功", data);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 生成验证码文本
     */
    private String generateCaptchaText() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();
        
        for (int i = 0; i < 4; i++) {
            captcha.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return captcha.toString();
    }
    
    /**
     * 生成模拟验证码图片
     */
    private String generateMockCaptchaImage(String text) {
        // 这里只是模拟，实际应该生成真实的验证码图片
        String mockImageData = "mock-captcha-image-data-for-" + text;
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(mockImageData.getBytes());
    }
} 