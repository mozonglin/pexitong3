@echo off
chcp 65001 >nul
rem 校园体育管理系统API测试脚本 (Windows版)
set BASE_URL=http://localhost:8080

echo === 校园体育管理系统后端API测试 ===
echo.

rem 1. 健康检查
echo 1. 健康检查...
curl -s "%BASE_URL%/test/health"
echo.
echo.

rem 2. 测试JWT生成
echo 2. 测试JWT功能...
curl -s "%BASE_URL%/test/jwt"
echo.
echo.

rem 3. 测试密码加密
echo 3. 测试密码加密...
curl -s "%BASE_URL%/test/password"
echo.
echo.

rem 4. 测试验证码生成
echo 4. 测试验证码生成...
curl -s "%BASE_URL%/security/captcha"
echo.
echo.

rem 5. 测试发送验证码
echo 5. 测试发送验证码...
curl -s -X POST "%BASE_URL%/auth/send-verification-code" ^
  -H "Content-Type: application/json" ^
  -d "{\"phone\": \"13812345678\", \"type\": \"register\"}"
echo.
echo.

echo === 注册测试 ===
echo 注意：请查看控制台输出的验证码，然后使用以下命令进行注册测试：
echo.
echo curl -X POST %BASE_URL%/auth/register \
echo   -H "Content-Type: application/json" \
echo   -d "{\"realName\": \"李骏铭\", \"userType\": \"student\", \"school\": \"济南校区\", \"studentId\": \"2021-4\", \"phone\": \"13812345678\", \"verificationCode\": \"控制台显示的验证码\"}"
echo.

echo === 登录测试 ===
echo 注册成功后，使用返回的用户名和密码进行登录测试：
echo.
echo curl -X POST %BASE_URL%/auth/login \
echo   -H "Content-Type: application/json" \
echo   -d "{\"username\": \"注册返回的用户名\", \"password\": \"注册返回的密码\", \"userType\": \"student\"}"
echo.

echo === 认证测试 ===
echo 登录成功后，使用返回的Token进行认证测试：
echo.
echo curl -X GET %BASE_URL%/user/profile \
echo   -H "Authorization: Bearer 登录返回的Token"
echo.

echo 测试完成！如果所有接口都正常返回JSON响应，说明系统运行正常。
pause 