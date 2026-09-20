# THÔNG TIN TÀI KHOẢN & HƯỚNG DẪN KHỞI CHẠY HỆ THỐNG
## Hospital Drug Association Mining System

---

## 1. Danh Sách Tài Khoản Đăng Nhập Mặc Định

Dữ liệu tài khoản được hệ thống tự động khởi tạo (seed) khi ứng dụng khởi động lần đầu:

| STT | Tên tài khoản (Username) | Mật khẩu (Password) | Vai trò (Role) | Chức vụ / Quyền hạn chính |
| :---: | :---: | :---: | :---: | :--- |
| **1** | **`admin`** | **`admin123`** | `ROLE_ADMIN` | **Quản trị viên & Kỹ sư Khai phá dữ liệu**<br>- Toàn quyền quản trị hệ thống<br>- Nạp và tiền xử lý bộ dữ liệu CSV (UCI Diabetes)<br>- Khởi chạy thuật toán Apriori & FP-Growth<br>- Chạy đối đầu Benchmark đo thời gian/bộ nhớ/Jaccard<br>- Chỉ định mô hình hoạt động (**Active Model**)<br>- Quản lý người dùng, bệnh nhân, danh mục thuốc |
| **2** | **`doctor`** | **`doctor123`** | `ROLE_DOCTOR` | **Bác sĩ điều trị & Kê đơn thuốc**<br>- Quản lý khám bệnh và lập đơn thuốc lâm sàng (`/prescriptions/create`)<br>- Nhận gợi ý thuốc kết hợp tự động từ **Active Model**<br>- Tra cứu cảnh báo tương tác thuốc DDI hai chiều<br>- Quyết định thêm hoặc bỏ qua (Add / Ignore) thuốc gợi ý |

---

## 2. Hướng Dẫn Khởi Chạy Ứng Dụng (2 Cách)

### Yêu cầu môi trường tối thiểu:
* **Java**: Phiên bản 17 LTS (hoặc Java 21)
* **Maven**: 3.8+ (Dự án đã tích hợp sẵn wrapper `mvn.cmd`, không cần cài Maven rời)
* **Cổng mạng (Port)**: Mặc định cổng `8080` còn trống

---

### Cách 1: Khởi chạy nhanh bằng H2 In-Memory Database (Khuyên Dùng khi Chấm / Demo)
> **Ưu điểm**: Không cần cài đặt hay cấu hình MySQL. Cơ sở dữ liệu và dữ liệu mẫu tự động khởi tạo trong bộ nhớ RAM.

#### Bước 1: Mở terminal tại thư mục gốc dự án:
```powershell
cd d:\.idea\it61
```

#### Bước 2: Thực thi lệnh khởi chạy Spring Boot với profile `h2`:
```powershell
.\mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

*Hoặc chạy trực tiếp từ file JAR đã đóng gói sẵn:*
```powershell
java -jar target\hospital-drug-association-mining-1.0.0.jar --spring.profiles.active=h2
```

#### Bước 3: Truy cập trình duyệt Web:
* **Địa chỉ hệ thống**: [http://localhost:8080](http://localhost:8080)
* **Trang quản trị H2 Console** (nếu cần xem DB): [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  * *JDBC URL*: `jdbc:h2:mem:hospital_drug_mining`
  * *User Name*: `sa`
  * *Password*: *(để trống)*

---

### Cách 2: Khởi chạy với Cơ sở Dữ liệu MySQL (XAMPP / MySQL Server)

#### Bước 1: Chuẩn bị CSDL MySQL
Đảm bảo dịch vụ MySQL đang chạy (ví dụ trên XAMPP cổng 3306), sau đó tạo database và nạp dữ liệu:
```powershell
# Tạo database chuẩn
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS hospital_drug_mining CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Nạp cấu trúc bảng và dữ liệu mẫu
mysql -u root -p hospital_drug_mining < database/schema.sql
mysql -u root -p hospital_drug_mining < database/sample-data.sql
```

#### Bước 2: Kiểm tra cấu hình kết nối trong `src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_drug_mining?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
```

#### Bước 3: Khởi chạy ứng dụng
```powershell
.\mvn.cmd spring-boot:run
```
Hoặc:
```powershell
java -jar target\hospital-drug-association-mining-1.0.0.jar
```

---

## 3. Các Lệnh Build & Kiểm Thử Thường Dùng

| Mục đích | Lệnh thực thi trong terminal |
| :--- | :--- |
| **Chạy toàn bộ 34 bài kiểm thử** | `.\mvn.cmd test` |
| **Biên dịch & đóng gói file JAR** | `.\mvn.cmd clean package` |
| **Đóng gói bỏ qua test để build nhanh** | `.\mvn.cmd clean package -DskipTests` |
| **Tải bộ dataset UCI chính thức** | `powershell -ExecutionPolicy Bypass -File scripts\download_dataset.ps1` |

---

## 4. Danh Mục Đường Dẫn (URLs) Phân Hệ Chính

| Phân hệ | Đường dẫn Web (URL) | Quyền truy cập |
| :--- | :--- | :---: |
| **Trang đăng nhập** | [http://localhost:8080/login](http://localhost:8080/login) | Public |
| **Bảng điều khiển (Dashboard)** | [http://localhost:8080/dashboard](http://localhost:8080/dashboard) | Admin, Doctor |
| **Kê đơn & Gợi ý kết hợp thuốc** | [http://localhost:8080/prescriptions/create](http://localhost:8080/prescriptions/create) | Doctor, Admin |
| **Quản lý danh sách đơn thuốc** | [http://localhost:8080/prescriptions](http://localhost:8080/prescriptions) | Doctor, Admin |
| **Nạp & Xem thống kê Dataset** | [http://localhost:8080/mining/dataset](http://localhost:8080/mining/dataset) | Admin, Doctor |
| **Khai phá giải thuật Apriori** | [http://localhost:8080/mining/apriori](http://localhost:8080/mining/apriori) | Admin, Doctor |
| **Khai phá giải thuật FP-Growth** | [http://localhost:8080/mining/fpgrowth](http://localhost:8080/mining/fpgrowth) | Admin, Doctor |
| **Đối đầu Benchmark Apriori vs FP-Growth** | [http://localhost:8080/mining/benchmark](http://localhost:8080/mining/benchmark) | Admin, Doctor |
| **Lịch sử đợt chạy & Chọn Active Model** | [http://localhost:8080/mining/history](http://localhost:8080/mining/history) | Admin, Doctor |
| **Danh mục luật kết hợp đã lưu** | [http://localhost:8080/mining/rules](http://localhost:8080/mining/rules) | Admin, Doctor |
| **Danh mục 26 nhóm thuốc chuẩn hóa** | [http://localhost:8080/medicines](http://localhost:8080/medicines) | Admin, Doctor |
| **Quản lý hồ sơ bệnh nhân** | [http://localhost:8080/patients](http://localhost:8080/patients) | Admin, Doctor |
