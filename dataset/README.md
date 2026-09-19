# BỘ DỮ LIỆU KHAI PHÁ DỮ LIỆU: UCI DIABETES 130-US HOSPITALS (1999–2008)

---

## 1. Nguồn gốc & Tính Pháp lý Uy tín của Dataset

Bộ dữ liệu chính được sử dụng trong đồ án là:
* **Tên chính thức**: **Diabetes 130-US Hospitals for Years 1999–2008**
* **Đơn vị công bố**: **UCI Machine Learning Repository** (University of California, Irvine)
* **Kho lưu trữ chính thức**: [UCI Machine Learning Repository - Dataset ID 296](https://archive.ics.uci.edu/dataset/296/diabetes+130-us+hospitals+for+years+1999-2008)
* **Xuất xứ lâm sàng**: Dữ liệu đại diện cho 10 năm điều trị nội trú (1999–2008) tại **130 bệnh viện và cơ sở y tế** trực thuộc mạng lưới y tế Health Facts (Cerner Corporation, Mỹ).

> **LƯU Ý QUAN TRỌNG:**
> Bộ dữ liệu này hoàn toàn **KHÔNG PHẢI là MIMIC-III**. Dự án sử dụng chính xác bộ dữ liệu UCI Diabetes 130-US Hospitals làm nguồn khai phá chính thức để tìm kiếm luật kết hợp đồng kê đơn thuốc điều trị tiểu đường.

---

## 2. Quy mô và Đặc tả Thuộc tính (Data Description)

* **Tổng số lượt điều trị (Encounters)**: **101,766 bản ghi**
* **Tổng số bệnh nhân duy nhất (Patients)**: **71,518 bệnh nhân**
* **Số trường thuộc tính ban đầu**: **50 thuộc tính**

### Các thuộc tính quan trọng phục vụ phân tích:
| Tên thuộc tính | Ý nghĩa lâm sàng / Kỹ thuật |
| :--- | :--- |
| `encounter_id` | Định danh duy nhất của mỗi đợt nằm viện (Được xem là đơn vị **Transaction** trong bài toán) |
| `patient_nbr` | Mã định danh duy nhất của bệnh nhân (Một bệnh nhân có thể có nhiều encounter) |
| `race` | Chủng tộc (Caucasian, AfricanAmerican, Hispanic, Asian, Other) |
| `gender` | Giới tính (Male, Female, Unknown/Invalid) |
| `age` | Nhóm tuổi theo khoảng 10 năm: `[0-10)`, `[10-20)`, ..., `[70-80)`, `[80-90)`, `[90-100)` |
| `admission_type_id` | Hình thức tiếp nhận bệnh nhân (Cấp cứu, khẩn cấp, có kế hoạch...) |
| `discharge_disposition_id` | Tình trạng xuất viện (Về nhà, chuyển viện...) |
| `time_in_hospital` | Thời gian nằm viện (tính bằng số ngày: 1 – 14 ngày) |
| `num_lab_procedures` | Số lượng xét nghiệm cận lâm sàng đã thực hiện |
| `num_procedures` | Số lượng thủ thuật y tế đã thực hiện |
| `num_medications` | Tổng số thuốc khác nhau được sử dụng trong suốt đợt điều trị |
| `number_outpatient` | Số lần khám ngoại trú trong năm trước đợt điều trị |
| `number_emergency` | Số lần cấp cứu trong năm trước đợt điều trị |
| `number_inpatient` | Số lần nằm viện nội trú trong năm trước đợt điều trị |
| `diag_1`, `diag_2`, `diag_3` | Ba chẩn đoán chính ban đầu theo chuẩn phân loại ICD-9-CM |

---

## 3. Danh mục 24 Cột Thuốc Điều Trị

Trong bộ dữ liệu UCI, có 24 cột thuốc chuyên biệt cho điều trị đái tháo đường:
1. `metformin` (Biguanide)
2. `repaglinide` (Meglitinide)
3. `nateglinide` (Meglitinide)
4. `chlorpropamide` (Sulfonylurea thế hệ 1)
5. `glimepiride` (Sulfonylurea thế hệ 2)
6. `acetohexamide` (Sulfonylurea thế hệ 1)
7. `glipizide` (Sulfonylurea thế hệ 2)
8. `glyburide` (Sulfonylurea thế hệ 2)
9. `tolbutamide` (Sulfonylurea thế hệ 1)
10. `pioglitazone` (Thiazolidinedione - TZD)
11. `rosiglitazone` (Thiazolidinedione - TZD)
12. `acarbose` (Alpha-glucosidase inhibitor)
13. `miglitol` (Alpha-glucosidase inhibitor)
14. `troglitazone` (Thiazolidinedione - TZD)
15. `tolazamide` (Sulfonylurea thế hệ 1)
16. `examide`
17. `citoglipton`
18. `insulin` (Các loại insulin)
19. `glyburide-metformin` (Dạng phối hợp liều cố định)
20. `glipizide-metformin` (Dạng phối hợp liều cố định)
21. `glimepiride-pioglitazone` (Dạng phối hợp liều cố định)
22. `metformin-rosiglitazone` (Dạng phối hợp liều cố định)
23. `metformin-pioglitazone` (Dạng phối hợp liều cố định)

---

## 4. Quy ước Xử lý Giá trị Thuốc & Xây dựng Giao dịch (Transaction Definition)

Trong file CSV `diabetic_data.csv`, mỗi cột thuốc mang 1 trong 4 giá trị:
* `No`: Thuốc **không được kê đơn hoặc không được sử dụng** trong đợt nằm viện này.
* `Steady`: Thuốc được kê đơn và duy trì ở mức liều ổn định.
* `Up`: Thuốc được kê đơn và có chỉ định tăng liều trong đợt điều trị.
* `Down`: Thuốc được kê đơn và có chỉ định giảm liều trong đợt điều trị.

### Quy ước trích xuất thuốc:
$$\text{Trạng thái thuốc} \in \{\text{"Steady"}, \text{"Up"}, \text{"Down}\} \implies \text{Thuốc CÓ sử dụng}$$
$$\text{Trạng thái thuốc} = \text{"No"} \implies \text{Thuốc KHÔNG sử dụng (Bỏ qua)}$$

### Xây dựng Transaction:
* Mỗi `encounter_id` đại diện cho một **Transaction** $T$.
* Tập mục $I(T)$ chứa danh sách các thuốc có trạng thái sử dụng trong encounter đó.
* **Quy tắc lọc dữ liệu bài toán luật kết hợp**:
  * Chỉ các Transaction có **kích thước $\ge 2$ thuốc** ($\|I(T)\| \ge 2$) mới được nạp vào bài toán khai phá luật kết hợp đồng sử dụng thuốc.
  * Các transaction có 0 hoặc 1 thuốc được thống kê riêng trong phần Data Understanding nhưng không đưa vào khai phá luật kết hợp nhiều thuốc.

---

## 5. Thống kê Kết quả Tiền Xử Lý Thực Tế trên Tập Dữ Liệu Đầy Đủ

| Chỉ số thống kê | Giá trị từ mã nguồn Java thực thi |
| :--- | :--- |
| **Tổng số dòng đọc vào** | 101,766 dòng |
| **Số dòng hợp lệ (Valid Rows)** | 101,766 dòng (100%) |
| **Số bệnh nhân duy nhất (Unique Patients)** | 71,518 bệnh nhân |
| **Số lượt khám/nhập viện (Unique Encounters)** | 101,766 lượt |
| **Số thuốc xuất hiện thực tế** | 21 loại thuốc |
| **Số transaction $\ge 2$ thuốc** | **31,049 transactions** (30.51% tổng số encounter) |
| **Số transaction có 1 thuốc** | 47,848 transactions (47.02%) |
| **Số transaction không có thuốc nào** | 22,869 transactions (22.47%) |
| **Số thuốc trung bình / encounter** | 1.18 thuốc |
| **Số thuốc tối đa / encounter** | 6 thuốc |

---

## 6. Cấu trúc Tệp trong Thư mục `dataset/`

```text
dataset/
├── README.md                  # Tài liệu đặc tả này
├── diabetic_data.csv          # Toàn bộ dataset UCI đầy đủ (101,766 dòng, 19.1 MB)
├── diabetic_data_sample.csv   # Mẫu 2,000 dòng phục vụ test nhanh và CI/CD (371 KB)
├── IDs_mapping.csv            # Bảng giải mã mã nhập viện, xuất viện và nguồn tiếp nhận
└── dataset_diabetes.zip       # Tệp zip gốc tải về từ UCI Machine Learning Repository
```
