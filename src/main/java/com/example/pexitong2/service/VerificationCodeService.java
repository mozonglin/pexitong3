package com.example.pexitong2.service;

import com.example.pexitong2.entity.VerificationCode;
import com.example.pexitong2.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class VerificationCodeService {
    
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;
    
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int MAX_DAILY_SEND = 5;
    private static final int SEND_INTERVAL_SECONDS = 60;
    
    /**
     * 发送验证码
     */
    public void sendVerificationCode(String phone, String type) {
        // 检查发送频率限制
        checkSendLimits(phone);
        
        // 生成6位随机验证码
        String code = generateRandomCode();
        
        // 设置过期时间
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);
        
        // 将之前未使用的验证码标记为失效
        List<VerificationCode> oldCodes = verificationCodeRepository
            .findByPhoneAndTypeAndIsUsedFalse(phone, VerificationCode.CodeType.valueOf(type));
        for (VerificationCode oldCode : oldCodes) {
            oldCode.setIsUsed(true);
            verificationCodeRepository.save(oldCode);
        }
        
        // 保存新验证码
        VerificationCode verificationCode = new VerificationCode(
            phone, code, VerificationCode.CodeType.valueOf(type), expiresAt);
        verificationCodeRepository.save(verificationCode);
        
        // 模拟发送短信（实际项目中应调用短信服务商API）
        System.out.println("发送验证码到 " + phone + ": " + code);
    }
    
    /**
     * 验证验证码
     */
    public boolean verifyCode(String phone, String code, String type) {
        return verificationCodeRepository
            .findByPhoneAndCodeAndTypeAndIsUsedFalseAndExpiresAtAfter(
                phone, code, VerificationCode.CodeType.valueOf(type), LocalDateTime.now())
            .map(verificationCode -> {
                // 标记为已使用
                verificationCode.setIsUsed(true);
                verificationCode.setUsedAt(LocalDateTime.now());
                verificationCodeRepository.save(verificationCode);
                return true;
            })
            .orElse(false);
    }
    
    /**
     * 检查发送限制
     */
    private void checkSendLimits(String phone) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        
        // 检查每日发送次数
        long dailyCount = verificationCodeRepository.countByPhoneAndCreatedAtAfter(phone, startOfDay);
        if (dailyCount >= MAX_DAILY_SEND) {
            throw new RuntimeException("今日验证码发送次数已达上限");
        }
        
        // 检查发送间隔（简化版本，实际应该检查最近一次发送时间）
    }
    
    /**
     * 生成随机验证码
     */
    private String generateRandomCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
} 