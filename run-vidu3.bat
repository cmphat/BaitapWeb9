@echo off
chcp 65001 > nul
set "JAVA_HOME=C:\Program Files\Java\jdk-22"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo ==========================================================
echo Đang khởi chạy Ví dụ 3 - Spring Security với Thymeleaf (Cổng 8083)
echo Cơ sở dữ liệu: SQL Server - SpringBootLoginRole
echo Truy cập trình duyệt: http://localhost:8083
echo Tài khoản demo: user / 123456 hoặc admin / 123456
echo ==========================================================
mvn spring-boot:run -pl vidu3-thymeleaf
pause
