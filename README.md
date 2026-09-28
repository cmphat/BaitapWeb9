# BÀI TẬP WEB 9 – SPRING BOOT 3 & SPRING SECURITY 6

> **Môn học:** Lập trình Web  
> **Repository:** [https://github.com/cmphat/BaitapWeb9.git](https://github.com/cmphat/BaitapWeb9.git)  
> **Sinh viên thực hiện:** Senior Java Spring Boot Developer Implementation  
> **Ngôn ngữ:** Java 21/22 | Spring Boot 3.3.0 | Spring Security 6  

---

## 1. GIỚI THIỆU BÀI TẬP

Dự án triển khai toàn diện chuyên đề **Spring Security 6** theo giáo trình giảng viên và tài liệu hướng dẫn thực hành, bao gồm:
1. **Ví dụ 1 (`vidu1-basic`)**: Cấu hình Spring Security cơ bản với `InMemoryUserDetailsManager`, mã hóa `BCryptPasswordEncoder`, phân quyền `@EnableMethodSecurity`, `@PreAuthorize("hasAuthority('ROLE_...')")`.
2. **Ví dụ 2 (`vidu2-database`)**: Tích hợp xác thực với CSDL SQL Server, entity `UserInfo`, custom `UserDetailsService`, `DaoAuthenticationProvider`, quản lý tài khoản vô hiệu hóa (`enabled = false`), cơ chế seed dữ liệu idempotent.
3. **Ví dụ 3 (`vidu3-thymeleaf`)**: Giao diện Thymeleaf hoàn chỉnh tích hợp `thymeleaf-extras-springsecurity6`, Navbar động hiển thị theo vai trò (`sec:authorize`), dashboard riêng biệt cho USER và ADMIN, trang báo lỗi `403 Access Denied`.
4. **Ứng dụng thực hành tổng hợp (`app-tonghop`)**: Ứng dụng hoàn chỉnh tích hợp CSDL SQL Server (`users`, `roles`, `otp_tokens`, `products`), xác thực OTP Email 6 chữ số có mã hóa băm SHA-256 (thời hạn 5 phút, giới hạn 5 lần nhập sai, cooldown 60 giây), phân quyền chặt chẽ ở backend (chống sửa xóa sản phẩm người khác, chống leo thang đặc quyền), quản lý người dùng & sản phẩm (CRUD, phân trang, tìm kiếm), upload ảnh Cloudinary có cơ chế fallback lưu trữ cục bộ, DTO mapping qua MapStruct.

---

## 2. CÔNG NGHỆ CHÍNH

- **Ngôn ngữ & Nền tảng:** Java 21 / 22, Spring Boot 3.3.0
- **Bảo mật:** Spring Security 6, BCrypt, Session Fixation Migration, CSRF Protection, Method Security
- **Truy xuất CSDL:** Spring Data JPA, Hibernate ORM 6, Microsoft JDBC Driver for SQL Server
- **Cơ sở dữ liệu:** Microsoft SQL Server (Hỗ trợ H2 In-Memory cho kiểm thử tự động)
- **Giao diện (Frontend):** Thymeleaf, Bootstrap 5.3, Bootstrap Icons, Thymeleaf Security Dialect 6
- **Build tool:** Apache Maven (Mô hình Multi-Module độc lập)
- **Mapper:** MapStruct 1.5.5.Final
- **Thư viện bổ trợ:** Project Lombok, Cloudinary HTTP5 SDK, Spring Mail (Jakarta Mail)

---

## 3. CẤU TRÚC PROJECT (MAVEN MULTI-MODULE)

Hệ thống được thiết kế theo kiến trúc Maven Multi-Module chuẩn doanh nghiệp. Mỗi ví dụ và ứng dụng tổng hợp là một module độc lập hoàn toàn, có cổng (port) riêng, cấu hình `SecurityFilterChain` riêng biệt, không xung đột Bean:

```
BaitapWeb9/
│
├── pom.xml                                   # Parent POM quản lý dependencies và plugins
├── .gitignore                                # Cấu hình loại trừ file nhạy cảm và build artifact
├── .env.example                              # File mẫu cấu hình biến môi trường
├── run-vidu1.bat                             # Script 1-click chạy Ví dụ 1 (Port 8081)
├── run-vidu2.bat                             # Script 1-click chạy Ví dụ 2 (Port 8082)
├── run-vidu3.bat                             # Script 1-click chạy Ví dụ 3 (Port 8083)
├── run-app.bat                               # Script 1-click chạy Ứng dụng tổng hợp (Port 8080)
│
├── database/
│   └── init-sqlserver.sql                    # Script DDL và Seed dữ liệu cho SQL Server
│
├── docs/
│   ├── BAO_CAO_BAI_9.md                      # Báo cáo chi tiết kỹ thuật nộp bài
│   └── screenshots/                          # Ảnh chụp minh chứng thực tế từ ứng dụng đang chạy
│       ├── vidu1_hello.png
│       ├── vidu1_login.png
│       ├── vidu2_login.png
│       ├── vidu2_customer_all.png
│       ├── vidu3_home.png
│       ├── vidu3_login.png
│       ├── vidu3_user_dashboard.png
│       ├── vidu3_admin_dashboard.png
│       ├── vidu3_403.png
│       ├── 01_app_home.png
│       ├── 02_app_login.png
│       ├── 03_app_register.png
│       ├── 04_app_verify_otp.png
│       ├── 05_app_forgot_password.png
│       ├── 06_app_access_denied.png
│       ├── 07_app_admin_dashboard.png
│       ├── 08_app_admin_users.png
│       ├── 09_app_products.png
│       ├── 10_app_product_form.png
│       ├── 11_app_user_dashboard.png
│       └── 12_app_forbidden_403.png
│
├── vidu1-basic/                              # MODULE VÍ DỤ 1 (Cổng 8081)
│   ├── pom.xml
│   └── src/
│       ├── main/java/vn/iotstar/vidu1/
│       │   ├── config/SecurityConfig.java    # InMemoryUserDetailsManager, BCrypt
│       │   ├── controller/CustomerController.java # @PreAuthorize, GET /hello, /customer, /admin
│       │   ├── model/Customer.java
│       │   └── Vidu1Application.java
│       └── test/java/vn/iotstar/vidu1/       # 10 Unit & Integration Tests (100% Pass)
│
├── vidu2-database/                           # MODULE VÍ DỤ 2 (Cổng 8082)
│   ├── pom.xml
│   └── src/
│       ├── main/java/vn/iotstar/vidu2/
│       │   ├── config/SecurityConfig.java    # DaoAuthenticationProvider, UserDetailsService
│       │   ├── config/DataInitializer.java   # Seed idempotent dữ liệu SQL Server
│       │   ├── entity/UserInfo.java          # Entity user_info (email, password, roles, enabled)
│       │   ├── repository/UserInfoRepository.java
│       │   ├── service/UserInfoService.java
│       │   ├── service/UserInfoUserDetails.java
│       │   ├── controller/UserController.java # POST /user/new, GET /customer/all
│       │   └── Vidu2Application.java
│       └── test/java/vn/iotstar/vidu2/       # 10 Unit & Integration Tests (100% Pass)
│
├── vidu3-thymeleaf/                          # MODULE VÍ DỤ 3 (Cổng 8083)
│   ├── pom.xml
│   └── src/
│       ├── main/java/vn/iotstar/vidu3/
│       │   ├── config/SecurityConfig.java    # Form login, Custom AccessDeniedHandler
│       │   ├── controller/AppController.java # /, /login, /user/dashboard, /admin/dashboard, /403
│       │   └── ...
│       ├── main/resources/templates/         # Thymeleaf templates sử dụng sec:authorize
│       │   ├── layout/base.html
│       │   ├── index.html
│       │   ├── login.html
│       │   ├── user-dashboard.html
│       │   ├── admin-dashboard.html
│       │   └── 403.html
│       └── test/java/vn/iotstar/vidu3/       # 10 Unit & Integration Tests (100% Pass)
│
└── app-tonghop/                              # MODULE THỰC HÀNH TỔNG HỢP (Cổng 8080)
    ├── pom.xml
    └── src/
        ├── main/java/vn/iotstar/
        │   ├── config/                       # SecurityConfig, WebConfig, DataInitializer
        │   ├── entity/                       # User, Role, OtpToken, Product
        │   ├── repository/                   # UserRepository, RoleRepository, OtpTokenRepository, ProductRepository
        │   ├── dto/                          # RegisterDto, ProductDto, UserDto, ResetPasswordDto...
        │   ├── mapper/                       # UserMapper, ProductMapper (MapStruct)
        │   ├── service/                      # UserService, ProductService, OtpService, EmailService, CloudinaryService
        │   ├── security/                     # CustomUserDetails, CustomUserDetailsService
        │   ├── controller/                   # AuthController, HomeController, ProductController, AdminUserController...
        │   └── AppTongHopApplication.java
        ├── main/resources/
        │   ├── application.properties        # Hỗ trợ biến môi trường .env
        │   └── templates/                    # Giao diện Bootstrap 5 tiếng Việt
        │       ├── layout/navbar.html
        │       ├── index.html
        │       ├── auth/ (login, register, verify-otp, forgot-password, reset-password)
        │       ├── user/ (dashboard)
        │       ├── admin/ (dashboard, users-list, user-form)
        │       ├── products/ (list, form, detail)
        │       └── error/ (403, 404, 500)
        └── test/java/vn/iotstar/             # 17 Integration Tests toàn diện (100% Pass)
```

---

## 4. HƯỚNG DẪN CÀI ĐẶT MÔI TRƯỜNG

### 4.1. Yêu cầu hệ thống
- **Java Development Kit (JDK):** JDK 21 hoặc JDK 22 (Đã cấu hình `--release 21`).
- **Apache Maven:** 3.8+ (khuyên dùng Maven 3.9+ đi kèm).
- **Microsoft SQL Server:** SQL Server 2017/2019/2022 hoặc SQL Server Express (chạy cổng mặc định 1433).

### 4.2. Cấu hình CSDL SQL Server
1. Mở **SQL Server Management Studio (SSMS)** hoặc **Azure Data Studio**.
2. Đăng nhập bằng tài khoản `sa` (hoặc tài khoản Windows Authentication).
3. Mở file `database/init-sqlserver.sql` và chạy toàn bộ script (Execute `F5`).
4. Script sẽ tự động:
   - Tạo CSDL `SpringBootLoginRole` cho Ví dụ 2 và Ví dụ 3.
   - Tạo CSDL `BaitapWeb9_Security` cho Ứng dụng thực hành tổng hợp.
   - Tạo cấu trúc bảng: `roles`, `users`, `otp_tokens`, `products`.
   - Chèn sẵn dữ liệu mẫu ban đầu (admin, user, roles, sản phẩm mẫu).

### 4.3. Cấu hình biến môi trường (`.env`)
Dự án cung cấp file mẫu `.env.example`. Nếu bạn muốn ghi đè cấu hình, hãy tạo file `.env` hoặc thiết lập biến môi trường hệ điều hành:

```env
# Server
SERVER_PORT=8080

# CSDL SQL Server
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=BaitapWeb9_Security;encrypt=false;trustServerCertificate=true;characterEncoding=UTF-8
DB_USERNAME=sa
DB_PASSWORD=your_sql_password

# Spring Mail (Gmail SMTP - Cần App Password nếu muốn gửi email thật)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password

# Cloudinary (Cần API Key nếu muốn upload ảnh trực tiếp lên Cloudinary CDN)
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

> **Lưu ý quan trọng về Mock/Fallback:**
> - Nếu **chưa có Gmail App Password**: Hệ thống sẽ tự động bắt ngoại lệ và ghi mã OTP trực tiếp vào console log (`System.out` / logger) dạng hộp thông báo nổi bật để người dùng dễ dàng copy/paste kích hoạt ngay trên giao diện mà không bị nghẽn luồng.
> - Nếu **chưa có Cloudinary API Key**: Hệ thống sẽ tự động kích hoạt cơ chế Fallback lưu trữ ảnh vào thư mục cục bộ `uploads/products/` và phục vụ qua endpoint HTTP tĩnh `/uploads/**`, đảm bảo tính năng thêm/sửa sản phẩm kèm ảnh hoạt động 100%.

---

## 5. HƯỚNG DẪN KHỞI CHẠY TỪNG VÍ DỤ

Bạn có thể chạy nhanh bằng các file `.bat` 1-click hoặc qua terminal Maven:

### 5.1. Ví dụ 1 – Spring Security cơ bản
- **Cách 1 (Khuyên dùng):** Nháy đúp chuột vào file `run-vidu1.bat`.
- **Cách 2 (Terminal):**
  ```bash
  mvn spring-boot:run -pl vidu1-basic
  ```
- **Địa chỉ truy cập:** `http://localhost:8081`
- **Tài khoản demo:**
  - `user` / `123456` (Quyền: `ROLE_USER`)
  - `admin` / `123456` (Quyền: `ROLE_ADMIN`)

### 5.2. Ví dụ 2 – Spring Security với CSDL SQL Server
- **Cách 1:** Nháy đúp chuột vào file `run-vidu2.bat`.
- **Cách 2 (Terminal):**
  ```bash
  mvn spring-boot:run -pl vidu2-database
  ```
- **Địa chỉ truy cập:** `http://localhost:8082`
- **Tài khoản demo:**
  - `user` / `123456` (Quyền: `ROLE_USER`)
  - `admin` / `123456` (Quyền: `ROLE_ADMIN`)
  - `disabled_user` / `123456` (Bị khóa - Đăng nhập sẽ báo lỗi tài khoản bị vô hiệu hóa)

### 5.3. Ví dụ 3 – Spring Security với Thymeleaf
- **Cách 1:** Nháy đúp chuột vào file `run-vidu3.bat`.
- **Cách 2 (Terminal):**
  ```bash
  mvn spring-boot:run -pl vidu3-thymeleaf
  ```
- **Địa chỉ truy cập:** `http://localhost:8083`
- **Tài khoản demo:**
  - `user` / `123456` (Truy cập User Dashboard, vào Admin Dashboard bị lỗi 403)
  - `admin` / `123456` (Truy cập cả User Dashboard và Admin Dashboard)

### 5.4. Ứng dụng thực hành tổng hợp (`app-tonghop`)
- **Cách 1:** Nháy đúp chuột vào file `run-app.bat`.
- **Cách 2 (Terminal):**
  ```bash
  mvn spring-boot:run -pl app-tonghop
  ```
- **Địa chỉ truy cập:** `http://localhost:8080`
- **Tài khoản demo:**
  - Quản trị viên: `admin` (hoặc `admin@iotstar.vn`) / `123456` (Quyền: `ROLE_ADMIN`)
  - Người dùng: `user` (hoặc `user@iotstar.vn`) / `123456` (Quyền: `ROLE_USER`)
  - Người dùng 2: `user2` (hoặc `user2@iotstar.vn`) / `123456` (Quyền: `ROLE_USER`)

---

## 6. DANH SÁCH ENDPOINTS & MA TRẬN PHÂN QUYỀN

### 6.1. Bảng phân quyền Ứng dụng tổng hợp

| Endpoint | Phương thức | Quyền truy cập | Mô tả chức năng |
| :--- | :---: | :---: | :--- |
| `/` | `GET` | Public (Ai cũng vào được) | Trang chủ giới thiệu hệ thống |
| `/login` | `GET`, `POST` | Public | Đăng nhập hệ thống (Session Auth) |
| `/register` | `GET`, `POST` | Public | Đăng ký tài khoản (Tạo OTP kích hoạt) |
| `/verify-otp` | `GET`, `POST` | Public | Xác thực mã OTP 6 số kích hoạt tài khoản |
| `/resend-register-otp` | `POST` | Public | Gửi lại mã OTP đăng ký (Cooldown 60s) |
| `/forgot-password` | `GET`, `POST` | Public | Yêu cầu OTP đặt lại mật khẩu |
| `/reset-password` | `GET`, `POST` | Public | Nhập OTP và mật khẩu mới |
| `/products` | `GET` | Public | Xem danh sách sản phẩm, tìm kiếm, phân trang |
| `/products/{id}` | `GET` | Public | Xem chi tiết sản phẩm |
| `/products/new` | `GET`, `POST` | `USER`, `ADMIN` | Thêm mới sản phẩm + upload ảnh |
| `/products/edit/{id}` | `GET`, `POST` | `USER` (chính chủ), `ADMIN` | Sửa sản phẩm (Kiểm tra sở hữu ở backend) |
| `/products/delete/{id}`| `POST` | `USER` (chính chủ), `ADMIN` | Xóa sản phẩm (Kiểm tra sở hữu ở backend) |
| `/user/dashboard` | `GET` | `USER`, `ADMIN` | Trang cá nhân, thống kê sản phẩm đã đăng |
| `/admin/dashboard` | `GET` | `ADMIN` duy nhất | Bảng điều khiển quản trị tổng quan |
| `/admin/users` | `GET` | `ADMIN` duy nhất | Quản lý người dùng: tìm kiếm, phân trang |
| `/admin/users/toggle-status/{id}` | `POST` | `ADMIN` duy nhất | Khóa / Kích hoạt người dùng |
| `/admin/users/change-role/{id}` | `POST` | `ADMIN` duy nhất | Chuyển đổi vai trò người dùng |
| `/403` | `GET` | Public | Trang thông báo truy cập bị từ chối |
| `/logout` | `POST` | Đã đăng nhập | Hủy session, xóa cookie `JSESSIONID` |

---

## 7. KIỂM THỬ TỰ ĐỘNG (AUTOMATED TEST RESULTS)

Dự án bao gồm bộ kiểm thử tự động toàn diện từ Unit Test đến Integration Test sử dụng `MockMvc`, Spring Security Test và H2 Database in-memory.

Chạy toàn bộ kiểm thử bằng lệnh:
```bash
mvn clean test
```

### Kết quả kiểm thử thực tế: **47/47 Tests PASSED (100%)**
- `vidu1-basic`: **10/10 tests** passed
- `vidu2-database`: **10/10 tests** passed
- `vidu3-thymeleaf`: **10/10 tests** passed
- `app-tonghop`: **17/17 tests** passed

```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for BaitapWeb9 - Parent 1.0.0:
[INFO] 
[INFO] BaitapWeb9 - Parent ................................ SUCCESS
[INFO] Ví dụ 1 - Spring Security Cơ bản ................... SUCCESS
[INFO] Ví dụ 2 - Spring Security với Database SQL Server .. SUCCESS
[INFO] Ví dụ 3 - Spring Security với Thymeleaf ............ SUCCESS
[INFO] Ứng dụng thực hành tổng hợp - Spring Boot Security . SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

---

## 8. HÌNH ẢNH MINH HỌA THỰC TẾ (SCREENSHOTS)

Tất cả các hình ảnh dưới đây được chụp trực tiếp từ ứng dụng đang chạy trong quá trình kiểm thử thực tế (lưu tại `docs/screenshots/`):

### 8.1. Ứng dụng thực hành tổng hợp (`app-tonghop`)

#### Trang chủ hệ thống
![Trang chủ](docs/screenshots/01_app_home.png)

#### Đăng nhập hệ thống
![Đăng nhập](docs/screenshots/02_app_login.png)

#### Đăng ký tài khoản
![Đăng ký](docs/screenshots/03_app_register.png)

#### Xác thực OTP Email 6 chữ số
![Xác thực OTP](docs/screenshots/04_app_verify_otp.png)

#### Quên mật khẩu
![Quên mật khẩu](docs/screenshots/05_app_forgot_password.png)

#### Dashboard Quản trị viên (ADMIN)
![Admin Dashboard](docs/screenshots/07_app_admin_dashboard.png)

#### Quản lý người dùng (Admin)
![Quản trị User](docs/screenshots/08_app_admin_users.png)

#### Danh sách sản phẩm (Tìm kiếm & Phân trang)
![Danh sách sản phẩm](docs/screenshots/09_app_products.png)

#### Form thêm mới sản phẩm & Upload ảnh
![Form sản phẩm](docs/screenshots/10_app_product_form.png)

#### Dashboard Người dùng (USER)
![User Dashboard](docs/screenshots/11_app_user_dashboard.png)

#### Trang từ chối truy cập 403 (Khi User cố vượt quyền vào Admin)
![403 Forbidden](docs/screenshots/12_app_forbidden_403.png)

---

### 8.2. Các ví dụ minh họa

| Ví dụ 1 - Public Hello | Ví dụ 1 - Form Login |
| :---: | :---: |
| ![Vidu1 Hello](docs/screenshots/vidu1_hello.png) | ![Vidu1 Login](docs/screenshots/vidu1_login.png) |

| Ví dụ 2 - SQL Server Login | Ví dụ 2 - Protected Customer All |
| :---: | :---: |
| ![Vidu2 Login](docs/screenshots/vidu2_login.png) | ![Vidu2 Protected](docs/screenshots/vidu2_customer_all.png) |

| Ví dụ 3 - Trang chủ Thymeleaf | Ví dụ 3 - User Dashboard |
| :---: | :---: |
| ![Vidu3 Home](docs/screenshots/vidu3_home.png) | ![Vidu3 User](docs/screenshots/vidu3_user_dashboard.png) |

---

## 9. BÁO CÁO CHI TIẾT
Chi tiết báo cáo kiến trúc hệ thống, cơ chế an toàn bảo mật, quy trình OTP và kiểm thử được ghi nhận đầy đủ tại file:
👉 **[docs/BAO_CAO_BAI_9.md](docs/BAO_CAO_BAI_9.md)**
