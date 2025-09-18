@echo off
REM PE统计功能API测试脚本（Windows版本）
REM 使用前请先设置正确的服务器地址和JWT Token

REM 配置
set BASE_URL=http://localhost:8080/pe/admin/statistics
REM 请替换为实际的JWT Token
set SCHOOL_ADMIN_TOKEN=your_school_admin_jwt_token_here
set COLLEGE_ADMIN_TOKEN=your_college_admin_jwt_token_here

echo === PE统计功能API测试 ===
echo.

REM 1. 测试设置PE积分指标（校级管理员）
echo 1. 测试设置PE积分指标（校级管理员权限）
echo 请求: POST %BASE_URL%/targets
curl -X POST "%BASE_URL%/targets" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %SCHOOL_ADMIN_TOKEN%" ^
  -d "{\"weeklyTarget\": 10, \"monthlyTarget\": 40, \"totalTarget\": 100}" ^
  -w "状态码: %%{http_code}" ^
  -s
echo.
echo ---

REM 2. 测试获取学校统计数据（校级管理员）
echo 2. 测试获取学校统计数据（校级管理员权限）
echo 请求: GET %BASE_URL%/school
curl -X GET "%BASE_URL%/school" ^
  -H "Authorization: Bearer %SCHOOL_ADMIN_TOKEN%" ^
  -w "状态码: %%{http_code}" ^
  -s
echo.
echo ---

REM 3. 测试获取院系统计数据（校级管理员）
echo 3. 测试获取院系统计数据（校级管理员权限，应返回全校所有班级）
echo 请求: GET %BASE_URL%/college
curl -X GET "%BASE_URL%/college" ^
  -H "Authorization: Bearer %SCHOOL_ADMIN_TOKEN%" ^
  -w "状态码: %%{http_code}" ^
  -s
echo.
echo ---

REM 4. 测试获取院系统计数据（院级管理员）
echo 4. 测试获取院系统计数据（院级管理员权限，应只返回本院班级）
echo 请求: GET %BASE_URL%/college
curl -X GET "%BASE_URL%/college" ^
  -H "Authorization: Bearer %COLLEGE_ADMIN_TOKEN%" ^
  -w "状态码: %%{http_code}" ^
  -s
echo.
echo ---

REM 5. 测试权限控制（院级管理员尝试设置指标）
echo 5. 测试权限控制（院级管理员尝试设置指标，应该失败）
echo 请求: POST %BASE_URL%/targets
curl -X POST "%BASE_URL%/targets" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %COLLEGE_ADMIN_TOKEN%" ^
  -d "{\"weeklyTarget\": 15, \"monthlyTarget\": 50, \"totalTarget\": 120}" ^
  -w "状态码: %%{http_code}" ^
  -s
echo.
echo ---

echo === 测试完成 ===
echo.
echo 注意事项：
echo 1. 请先替换脚本中的JWT Token为实际有效的Token
echo 2. 确保服务器地址正确
echo 3. 确保数据库已执行更新脚本 pe_database_update.sql
echo 4. 校级管理员Token用于测试设置指标和查看学校统计
echo 5. 院级管理员Token用于测试查看院系统计

pause
