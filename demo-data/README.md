# Hướng Dẫn Sử Dụng Dữ Liệu MIMIC-III (Medical Information Mart for Intensive Care)

Thư mục này chứa các tệp dữ liệu mẫu (**Demo / Sample Data**) được trích xuất và mô phỏng theo cấu trúc chuẩn của cơ sở dữ liệu y tế **MIMIC-III Clinical Database v1.4**.

---

## 1. Thông Tin Dataset Nguồn

- **Tên cơ sở dữ liệu**: MIMIC-III Clinical Database
- **Phiên bản**: v1.4
- **Đơn vị phát triển**: MIT Laboratory for Computational Physiology (MIT-LCP)
- **Nguồn phân phối chính thức**: PhysioNet (Physiological Data Network)
- **Địa chỉ truy cập**: [https://physionet.org/content/mimiciii/1.4/](https://physionet.org/content/mimiciii/1.4/)
- **License**: PhysioNet Credentialed Data Use Agreement (yêu cầu hoàn thành khóa học CITI Data or Specimens Only Research).

---

## 2. Quy Trình Xin Quyền Truy Cập Bản Đầy Đủ (Full Dataset)

Để sử dụng bộ dữ liệu đầy đủ quy mô lớn (hơn 4 triệu dòng đơn thuốc):

1. **Đăng ký tài khoản PhysioNet**: Truy cập [physionet.org](https://physionet.org/) và tạo tài khoản cá nhân.
2. **Hoàn thành chứng chỉ CITI Program**:
   - Khóa học: *"Data or Specimens Only Research"* hoặc *"Conflicts of Interest"*.
   - Tải chứng chỉ (dạng PDF/ID) lên hệ thống PhysioNet Profile.
3. **Ký cam kết DUA (Data Use Agreement)**:
   - Cam kết không tái định danh bệnh nhân (De-identification).
   - Cam kết không chia sẻ dữ liệu nguyên bản cho bên thứ ba không có quyền.
   - Không commit dữ liệu MIMIC-III bản đầy đủ lên GitHub hoặc các nền tảng công cộng.
4. **Tải dữ liệu**: Sau khi được PhysioNet phê duyệt (thường mất 2–4 ngày làm việc), tải các file CSV nén `.csv.gz`.

---

## 3. Danh Mục Các Tệp Dữ Liệu Được Sử Dụng & Mục Đích

| Tên Tệp CSV | Tệp Demo Tương Ứng | Mục Đích Sử Dụng Trong Dự Án |
|---|---|---|
| `PRESCRIPTIONS.csv` | `PRESCRIPTIONS_sample.csv`<br>`PRESCRIPTIONS_large_mimic.csv` | **Nguồn quan trọng nhất**: Chứa thông tin đơn thuốc nội trú (`HADM_ID`, `DRUG_NAME_GENERIC`, `DRUG`, liều dùng, đường dùng). Dùng để gom giỏ hàng điều trị HADM_ID &rarr; các tập thuốc nhằm khai phá luật kết hợp. |
| `ADMISSIONS.csv` | `ADMISSIONS_sample.csv` | Lưu thông tin các đợt nhập viện của bệnh nhân (`HADM_ID`, `SUBJECT_ID`, thời gian nhập/xuất viện, loại hình cấp cứu, chẩn đoán sơ bộ). |
| `PATIENTS.csv` | `PATIENTS_sample.csv` | Chứa thông tin nhân khẩu học của bệnh nhân (`SUBJECT_ID`, giới tính, ngày sinh, tình trạng sinh tồn). |
| `DIAGNOSES_ICD.csv` | `DIAGNOSES_ICD_sample.csv` | Liên kết giữa lượt nhập viện `HADM_ID` và mã chẩn đoán ICD-9. Phục vụ thống kê Data Understanding và mở rộng bài toán Diagnosis &rarr; Drug. |
| `D_ICD_DIAGNOSES.csv` | `D_ICD_DIAGNOSES_sample.csv` | Bảng từ điển ánh xạ mã bệnh `ICD9_CODE` sang tên bệnh tiếng Anh đầy đủ (ví dụ: `4111` &rarr; Cơn đau thắt ngực không ổn định). |
| `PROCEDURES_ICD.csv` | *(Tùy chọn)* | Danh mục thủ thuật/phẫu thuật y khoa trong các lần nhập viện. |

---

## 4. Cách Cấu Hình Đường Dẫn Dữ Liệu Ngoài (External Dataset)

Nhằm đảm bảo tuân thủ nghiêm ngặt **PhysioNet License**, dự án tuyệt đối **KHÔNG commit tệp dữ liệu đầy đủ lên Git**.

Hệ thống cung cấp cơ chế linh hoạt để người dùng cấu hình đường dẫn tới thư mục dataset bên ngoài:

### Cách 1: Cấu hình trong `application.properties`
Mở file `src/main/resources/application.properties` và sửa đường dẫn:
```properties
app.mimic.demo-data-dir=D:/mimic-iii-clinical-database-1.4
app.mimic.upload-dir=uploads/mimic
```

### Cách 2: Biến môi trường hệ thống (Environment Variable)
Khai báo biến môi trường:
```bash
export MIMIC_DATA_DIR=/path/to/full/mimic-iii/
```
Hoặc trên Windows PowerShell:
```powershell
$env:MIMIC_DATA_DIR="D:\Datasets\MIMIC-III"
```

### Cách 3: Nạp trực tiếp từ giao diện Admin
Truy cập màn hình `Quản Lý Dataset` (`/mining/dataset`), chọn tệp `PRESCRIPTIONS.csv` từ máy tính cá nhân và nhấn **Tải Lên & Tiền Xử Lý**. Hệ thống sẽ nạp theo luồng streaming không lo tràn RAM.
