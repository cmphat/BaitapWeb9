@echo off
chcp 65001 > nul
set "JAVA_HOME=C:\Program Files\Java\jdk-22"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo ==========================================================
echo Đang khởi chạy Ứng Dụng Thực Hành Tổng Hợp (Cổng 8080)
echo Cơ sở dữ liệu: SQL Server - BaitapWeb9_Security
echo Truy cập trình duyệt: http://localhost:8080
echo Tài khoản demo:
echo   - ADMIN: admin@gmail.com / 123456
echo   - USER : user@gmail.com  / 123456
echo ==========================================================
mvn spring-boot:run -pl app-tonghop
pause
