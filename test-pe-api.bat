@echo off
echo 测试PE管理API功能
echo =====================

set BASE_URL=http://localhost:9999/api/pe
set TOKEN=Bearer_YOUR_TOKEN_HERE

echo.
echo 1. 测试获取活动列表...
curl -X GET "%BASE_URL%/activities?page=1&pageSize=10" ^
     -H "Authorization: %TOKEN%" ^
     -H "Content-Type: application/json"

echo.
echo.
echo 2. 测试获取用户列表...
curl -X GET "%BASE_URL%/users?page=1&pageSize=10" ^
     -H "Authorization: %TOKEN%" ^
     -H "Content-Type: application/json"

echo.
echo.
echo 3. 测试获取早操活动列表...
curl -X GET "%BASE_URL%/morning-exercises?page=1&pageSize=10" ^
     -H "Authorization: %TOKEN%" ^
     -H "Content-Type: application/json"

echo.
echo.
echo 4. 测试获取签到记录列表...
curl -X GET "%BASE_URL%/attendance-records?page=1&pageSize=10" ^
     -H "Authorization: %TOKEN%" ^
     -H "Content-Type: application/json"

echo.
echo.
echo 5. 测试获取统计数据...
curl -X GET "%BASE_URL%/statistics" ^
     -H "Authorization: %TOKEN%" ^
     -H "Content-Type: application/json"

echo.
echo.
echo 测试完成！
echo.
echo 使用说明：
echo 1. 启动应用：mvn spring-boot:run
echo 2. 登录获取管理员Token
echo 3. 将上方的 Bearer_YOUR_TOKEN_HERE 替换为真实Token
echo 4. 重新运行此脚本进行测试
pause
