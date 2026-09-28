@echo off
chcp 65001 > nul
set "JAVA_HOME=C:\Program Files\Java\jdk-22"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo ==========================================================
echo Đang khởi chạy Ví dụ 1 - Spring Security Cơ bản (Cổng 8081)
echo Truy cập trình duyệt: http://localhost:8081
echo Tài khoản demo: user / 123456 hoặc admin / 123456
echo ==========================================================
mvn spring-boot:run -pl vidu1-basic
pause
