#!/bin/bash

# 校园体育管理系统API测试脚本
BASE_URL="http://192.168.1.105:9999"

echo "=== 校园体育管理系统后端API测试 ==="
echo ""

# 1. 健康检查
echo "1. 健康检查..."
curl -s "$BASE_URL/test/health" | jq '.'
echo ""

# 2. 测试JWT生成
echo "2. 测试JWT功能..."
curl -s "$BASE_URL/test/jwt" | jq '.'
echo ""

# 3. 测试密码加密
echo "3. 测试密码加密..."
curl -s "$BASE_URL/test/password" | jq '.'
echo ""

# 4. 测试验证码生成
echo "4. 测试验证码生成..."
curl -s "$BASE_URL/security/captcha" | jq '.'
echo ""

# 5. 测试发送验证码
echo "5. 测试发送验证码..."
curl -s -X POST "$BASE_URL/auth/send-verification-code" \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13812345678",
    "type": "register"
  }' | jq '.'
echo ""

echo "=== 注册测试 ==="
echo "注意：请查看控制台输出的验证码，然后使用以下命令进行注册测试："
echo ""
echo "curl -X POST $BASE_URL/auth/register \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{"
echo "    \"realName\": \"李骏铭\","
echo "    \"userType\": \"student\","
echo "    \"school\": \"济南校区\","
echo "    \"studentId\": \"2021-4\","
echo "    \"phone\": \"13812345678\","
echo "    \"verificationCode\": \"控制台显示的验证码\""
echo "  }'"
echo ""

echo "=== 登录测试 ==="
echo "注册成功后，使用返回的用户名和密码进行登录测试："
echo ""
echo "curl -X POST $BASE_URL/auth/login \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{"
echo "    \"username\": \"注册返回的用户名\","
echo "    \"password\": \"注册返回的密码\","
echo "    \"userType\": \"student\""
echo "  }'"
echo ""

echo "=== 认证测试 ==="
echo "登录成功后，使用返回的Token进行认证测试："
echo ""
echo "curl -X GET $BASE_URL/user/profile \\"
echo "  -H \"Authorization: Bearer 登录返回的Token\""
echo ""

echo "测试完成！如果所有接口都正常返回JSON响应，说明系统运行正常。" 