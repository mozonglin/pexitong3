package com.example.pexitong2.service;

import com.example.pexitong2.dto.LoginRequest;
import com.example.pexitong2.dto.RegisterRequest;
import com.example.pexitong2.entity.User;
import com.example.pexitong2.repository.UserRepository;
import com.example.pexitong2.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CheckUserService checkUserService;
    
    @Autowired
    private VerificationCodeService verificationCodeService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 用户注册
     */
    public Map<String, Object> register(RegisterRequest request) {
        // 1. 验证验证码
        if (!verificationCodeService.verifyCode(request.getPhone(), 
                request.getVerificationCode(), "register")) {
            throw new RuntimeException("验证码错误或已过期");
        }
        
        // 2. 根据用户类型验证身份信息
        String departmentName = null;
        if ("student".equals(request.getUserType())) {
            CheckUserService.CheckStudentInfo checkStudent = checkUserService
                .validateStudent(request.getStudentId(), request.getSchool(), request.getRealName())
                .orElseThrow(() -> new RuntimeException("学生身份信息验证失败"));
            
            departmentName = checkStudent.getCollege();
            
            // 标记为已使用
            checkUserService.markStudentAsUsed(checkStudent.getStudentid());
            
        } else if ("teacher".equals(request.getUserType())) {
            CheckUserService.CheckTeacherInfo checkTeacher = checkUserService
                .validateTeacher(request.getStudentId(), request.getSchool(), request.getRealName())
                .orElseThrow(() -> new RuntimeException("教师身份信息验证失败"));
            
            departmentName = checkTeacher.getCollege();
            
            // 标记为已使用
            checkUserService.markTeacherAsUsed(checkTeacher.getTeacherid());
        } else {
            throw new RuntimeException("无效的用户类型");
        }
        
        // 3. 检查用户是否已存在
        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new RuntimeException("该学工号已注册");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("该手机号已注册");
        }
        
        // 4. 生成用户名和初始密码
        String username = generateUsername(request.getRealName(), request.getStudentId());
        String initialPassword = generateInitialPassword();
        
        // 5. 创建用户
        String userId = ("student".equals(request.getUserType()) ? "stu_" : "tea_") + 
                       UUID.randomUUID().toString().substring(0, 8);
        
        User user = new User(userId, username, passwordEncoder.encode(initialPassword),
                           request.getRealName(), request.getStudentId(),
                           User.UserType.valueOf(request.getUserType()), request.getSchool());
        user.setDepartmentName(departmentName);
        user.setPhone(request.getPhone());
        
        userRepository.save(user);
        
        // 6. 模拟发送短信
        String smsMessage = String.format("您的用户名: %s, 初始密码: %s, 请及时登录修改密码", 
                                         username, initialPassword);
        System.out.println("发送短信到 " + request.getPhone() + ": " + smsMessage);
        
        // 7. 返回注册结果
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", username);
        result.put("userType", request.getUserType());
        result.put("realName", request.getRealName());
        result.put("school", request.getSchool());
        result.put("department", departmentName);
        result.put("initialPassword", initialPassword);
        result.put("passwordSent", true);
        result.put("smsMessage", smsMessage);
        
        return result;
    }
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(LoginRequest request) {
        // 1. 查找用户
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("用户名错误"));
        
        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 3. 验证用户类型
        if (!user.getUserType().name().equals(request.getUserType())) {
            throw new RuntimeException("用户类型不匹配");
        }
        
        // 4. 检查用户状态
        if (user.getStatus() != User.UserStatus.active) {
            throw new RuntimeException("账户已被停用");
        }
        
        // 5. 更新登录信息
        user.setLastLoginAt(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);
        
        // 6. 生成Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), 
                                           user.getUserType().name(), request.getRememberMe());
        
        // 7. 计算Token过期时间
        LocalDateTime expiresAt = jwtUtil.getExpirationDateTime(token);
        
        // 8. 构建用户信息响应
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("userType", user.getUserType().name());
        userInfo.put("school", user.getSchool());
        userInfo.put("departmentId", null); // 不使用departmentId
        userInfo.put("departmentName", user.getDepartmentName());
        userInfo.put("studentId", user.getStudentId());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("status", user.getStatus().name());
        userInfo.put("lastLoginAt", user.getLastLoginAt());
        userInfo.put("loginCount", user.getLoginCount());
        userInfo.put("isFirstLogin", user.getIsFirstLogin());
        
        // 9. 构建登录响应
        Map<String, Object> result = new HashMap<>();
        result.put("user", userInfo);
        result.put("token", token);
        result.put("expiresAt", expiresAt);
        
        return result;
    }
    
    /**
     * 生成用户名
     */
    private String generateUsername(String realName, String studentId) {
        // 简化逻辑：姓名拼音简写 + 部分学工号
        String namePrefix = realName.length() > 0 ? 
            realName.substring(0, Math.min(3, realName.length())).toLowerCase() : "user";
        String idSuffix = studentId.length() > 4 ? 
            studentId.substring(studentId.length() - 4) : studentId;
        return namePrefix + idSuffix;
    }
    
    /**
     * 生成初始密码
     */
    private String generateInitialPassword() {
        return "TempPass" + (new Random().nextInt(900) + 100);
    }
} 