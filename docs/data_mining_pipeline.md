# TÀI LIỆU QUY TRÌNH KHAI PHÁ DỮ LIỆU & KIẾN TRÚC TÍCH HỢP

Tài liệu này thuyết minh chi tiết 6 giai đoạn theo chuẩn **CRISP-DM (Cross-Industry Standard Process for Data Mining)** được hiện thực hóa trong đồ án:

---

## 1. Business Understanding (Thấu hiểu bài toán nghiệp vụ)
* **Vấn đề**: Việc kết hợp thuốc điều trị (co-prescription) là thực tế phổ biến trong y tế để điều trị các bệnh phức tạp hoặc kiểm soát đa triệu chứng. Tuy nhiên, khối lượng dữ liệu khổng lồ khiến việc nhận biết các mẫu kê đơn đồng thời trở nên quá tải.
* **Mục tiêu**: Tự động phát hiện các mẫu đồng xuất hiện giữa các thuốc từ dữ liệu lâm sàng lịch sử để cung cấp thông tin tham khảo ngay khi bác sĩ kê đơn thuốc trên hệ thống bệnh viện.

---

## 2. Data Understanding (Thấu hiểu dữ liệu)
* Dữ liệu nguồn: Bảng `PRESCRIPTIONS` trong MIMIC-III Clinical Database.
* Các thuộc tính cốt lõi:
  * `HADM_ID`: Mã đợt nhập viện của bệnh nhân.
  * `DRUG_NAME_GENERIC`: Tên gốc chuẩn hóa của thuốc.
  * `DRUG`: Tên biệt dược hoặc tên chỉ định.
  * `ROUTE`, `DOSE_VAL_RX`, `DOSE_UNIT_RX`: Liều dùng và đường dùng.

---

## 3. Data Preparation (Chuẩn bị & Tiền xử lý dữ liệu)
* Đọc dữ liệu dạng stream để tối ưu bộ nhớ.
* Loại bỏ bản ghi thiếu `HADM_ID` hoặc tên thuốc.
* Ưu tiên tên gốc `DRUG_NAME_GENERIC`, loại bỏ khoảng trắng, chuẩn hóa Title Case.
* Gom nhóm các thuốc theo `HADM_ID` thành các giao dịch:
  $$\text{Transaction} = \text{HADM\_ID} \rightarrow \{ \text{Drug}_1, \text{Drug}_2, \dots \}$$
* Loại bỏ các giao dịch có ít hơn 2 loại thuốc.

---

## 4. Modeling (Khai phá dữ liệu)
* Thực hiện khai phá luật kết hợp bằng 2 thuật toán độc lập thuần Java:
  1. **Apriori**: Dựa trên nguyên lý tỉa ứng viên level-wise.
  2. **FP-Growth**: Dựa trên cấu trúc cây tiền tố nén FP-Tree và Header Table liên kết ngang.

---

## 5. Evaluation (Đánh giá mô hình & Luật)
* Đánh giá luật qua 3 thước đo:
  * $\text{Support} \ge \text{minSupport}$
  * $\text{Confidence} \ge \text{minConfidence}$
  * $\text{Lift} > 1.0$ (chỉ giữ lại các luật có tương quan đồng xuất hiện dương tính).
* Thực hiện Benchmark so sánh thời gian chạy (Runtime ms) và bộ nhớ (Memory MB) giữa Apriori và FP-Growth trên cùng bộ dữ liệu.

---

## 6. Deployment (Tích hợp & Triển khai)
* Tích hợp trực tiếp các luật kết hợp tốt nhất vào chức năng Kê đơn thuốc của Bác sĩ (`/prescriptions/create`).
* Khi bác sĩ thêm một thuốc vào đơn, hệ thống tự động tìm luật có Antecedent khớp và hiển thị Top N thuốc thường được kê cùng kèm cảnh báo miễn trừ trách nhiệm lâm sàng.
