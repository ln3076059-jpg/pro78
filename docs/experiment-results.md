# BÁO CÁO KẾT QUẢ THỰC NGHIỆM DATA MINING (EXPERIMENT RESULTS REPORT)

---

## 1. Thông tin Tập Dữ liệu Thực nghiệm

Toàn bộ kết quả thực nghiệm trong báo cáo này được đo lường và tính toán tự động bằng chương trình thuần Java từ bộ dữ liệu thực tế:

* **Tên bộ dữ liệu**: **Diabetes 130-US Hospitals for Years 1999–2008** (UCI Machine Learning Repository).
* **Nguồn tệp dữ liệu**: `dataset/diabetic_data.csv` (Dung lượng: 19.1 MB, 101,766 dòng).
* **Môi trường đo lường**: Java 21, Spring Boot 3.3.4, hệ điều hành Windows x64.
* **Quy ước tính toán**: 100% dữ liệu thật từ bộ dữ liệu y tế, không giả lập, không hard-code bất kỳ chỉ số nào.

---

## 2. Kết quả Tiền Xử Lý Dữ Liệu (Preprocessing Summary)

| Thuộc tính thống kê | Giá trị đo lường thực tế |
| :--- | :--- |
| **Tổng số dòng đọc vào ban đầu** | 101,766 dòng |
| **Số dòng hợp lệ (Valid records)** | 101,766 dòng (100%) |
| **Số bản ghi bị loại (Invalid records)** | 0 dòng |
| **Số lượng bệnh nhân duy nhất (Unique Patients)** | **71,518 bệnh nhân** |
| **Số đợt nằm viện duy nhất (Unique Encounters)** | **101,766 lượt** |
| **Số loại thuốc xuất hiện thực tế** | **21 loại thuốc** |
| **Số giao dịch hợp lệ ($\ge 2$ thuốc)** | **31,049 transactions** (30.51%) |
| **Số giao dịch có 1 thuốc** | 47,848 transactions (47.02%) |
| **Số giao dịch không có thuốc** | 22,869 transactions (22.47%) |
| **Số thuốc trung bình / encounter** | 1.18 thuốc |
| **Số thuốc ít nhất / encounter** | 0 thuốc |
| **Số thuốc nhiều nhất / encounter** | 6 thuốc |

---

## 3. Bảng So Sánh Hiệu Năng: Apriori vs FP-Growth

Thực nghiệm được tiến hành trên toàn bộ **31,049 transactions** với cố định ngưỡng $\text{minConfidence} = 0.30$ ($30\%$), $\text{minLift} = 1.0$, $\text{maxItemsetSize} = 5$ qua các ngưỡng $\text{minSupport}$ khác nhau:

| Thuật toán | Số lượng Giao dịch | Ngưỡng Support | Ngưỡng Conf | Thời gian (Runtime - ms) | Bộ nhớ (RAM - MB) | Số tập phổ biến (Itemsets) | Số luật kết hợp (Rules) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Apriori** | 31,049 | **0.01 (1%)** | 0.30 | 322 ms | 24.16 MB | **45** | **3** |
| **FP-Growth** | 31,049 | **0.01 (1%)** | 0.30 | **83 ms** | **9.00 MB** | **45** | **3** |
| **Apriori** | 31,049 | **0.02 (2%)** | 0.30 | 153 ms | 27.17 MB | **31** | **2** |
| **FP-Growth** | 31,049 | **0.02 (2%)** | 0.30 | **50 ms** | **8.00 MB** | **31** | **2** |
| **Apriori** | 31,049 | **0.05 (5%)** | 0.30 | 96 ms | 4.04 MB | **19** | **1** |
| **FP-Growth** | 31,049 | **0.05 (5%)** | 0.30 | **39 ms** | **7.50 MB** | **19** | **1** |
| **Apriori** | 31,049 | **0.10 (10%)**| 0.30 | 78 ms | 35.50 MB | **14** | **1** |
| **FP-Growth** | 31,049 | **0.10 (10%)**| 0.30 | **40 ms** | **7.50 MB** | **14** | **1** |

---

## 4. Kiểm thử Tương Thích & Tính Nhất Quán Giữa Hai Thuật Toán

Kết quả kiểm thử từ unit test `FPGrowthMiningServiceTest` và kiểm thử thực tế khẳng định:
1. **Độ chính xác tương đồng 100%**: Tại bất kỳ ngưỡng $\text{minSupport}$ nào ($0.01, 0.02, 0.05, 0.10$), cả hai thuật toán Apriori và FP-Growth đều sinh ra **chính xác cùng số lượng tập phổ biến** (45 tập ở 0.01, 31 tập ở 0.02, 19 tập ở 0.05, 14 tập ở 0.10).
2. **Khớp từng phần tử và tần suất hỗ trợ (Support Count)**: Danh sách các itemset cùng chỉ số support tuyệt đối của từng tập mục sinh ra từ Apriori và FP-Growth hoàn toàn giống nhau, chứng minh tính đúng đắn toán học của cả hai giải thuật.

---

## 5. Danh Sách Các Luật Kết Hợp Hàng Đầu (Top Association Rules)

Tại ngưỡng $\text{minSupport} = 0.01$ ($1\%$), $\text{minConfidence} = 0.30$ ($30\%$), $\text{minLift} > 1.0$:

| STT | Tiền đề (Antecedent $X$) | Hệ quả (Consequent $Y$) | Support | Confidence | Lift | Ý nghĩa Dược lý & Lâm sàng |
| :---: | :--- | :--- | :---: | :---: | :---: | :--- |
| **1** | `{Glyburide-Metformin}` | `{Insulin}` | **0.0133** (1.33%) | **0.8227** (82.27%) | **1.0908** | Bệnh nhân dùng viên phối hợp liều cố định Glyburide-Metformin khi thất bại điều trị ngoại trú thường được chỉ định thêm Insulin nội trú để ổn định đường huyết. |
| **2** | `{Repaglinide}` | `{Insulin}` | **0.0281** (2.81%) | **0.7558** (75.58%) | **1.0021** | Bệnh nhân dùng nhóm Meglitinide (Repaglinide) có tỷ lệ phối hợp cùng Insulin đạt 75.58%. |
| **3** | `{Glyburide}` | `{Metformin}` | **0.1230** (12.30%) | **0.5250** (52.50%) | **1.0513** | Phác đồ kinh điển phối hợp Sulfonylurea thế hệ 2 (Glyburide) cùng Biguanide (Metformin) - là phối hợp hai thuốc uống phổ biến nhất trong điều trị đái tháo đường type 2. |

---

## 6. Đánh Giá & Phân Tích Độ Nhạy của Ngưỡng (Threshold Sensitivity)

1. **Khi giảm $\text{minSupport}$ từ $0.10 \to 0.01$**:
   * Số lượng tập phổ biến tăng từ 14 lên 45 tập.
   * Số lượng luật kết hợp tìm được tăng từ 1 lên 3 luật.
   * Thời gian chạy của Apriori tăng từ 78 ms lên 322 ms (tăng hơn 4 lần) do số lượng tổ hợp ứng viên $C_k$ cần kiểm tra tăng vọt.
   * Thời gian chạy của FP-Growth chỉ tăng từ 40 ms lên 83 ms (tăng khoảng 2 lần), cho thấy độ ổn định cao hơn hẳn khi ngưỡng hỗ trợ giảm thấp.
2. **Khuyến nghị lựa chọn ngưỡng trong thực tế**:
   * Với bộ dữ liệu UCI Diabetes, việc phối hợp thuốc tiểu đường tập trung vào một số ít nhóm thuốc chủ lực (Insulin, Metformin, Glyburide, Glipizide). Do đó, ngưỡng $\text{minSupport} \in [0.01, 0.02]$ và $\text{minConfidence} \in [0.30, 0.50]$ là khoảng tham số tối ưu nhất để vừa không bị bùng nổ tổ hợp, vừa không bỏ sót các phác đồ phối hợp quan trọng.

---

## 7. Lựa Chọn Thuật Toán Cuối Cùng & Cơ Sở Khoa Học

### 7.1. Kết luận lựa chọn
Hệ thống chính thức đề xuất và lựa chọn **Thuật toán FP-Growth (Frequent Pattern Growth)** làm thuật toán nòng cốt cho phân hệ khai phá dữ liệu thời gian thực.

### 7.2. Cơ sở lý do khoa học dựa trên số liệu thực nghiệm:
1. **Tốc độ vượt trội (Runtime)**:
   * Tại ngưỡng $\text{minSupport} = 0.01$, FP-Growth mất **83 ms**, nhanh hơn **3.88 lần** so với Apriori (322 ms).
   * Sự chênh lệch tốc độ này xuất phát từ bản chất thuật toán: Apriori phải thực hiện nhiều lượt quét dữ liệu và sinh hàng trăm ứng viên trung gian $C_k$, trong khi FP-Growth chỉ quét CSDL đúng 2 lần và nén toàn bộ thông tin vào cây FP-Tree.
2. **Khả năng mở rộng (Scalability)**:
   * Khi quy mô dữ liệu bệnh viện mở rộng lên hàng triệu bản ghi, việc quét đĩa lặp đi lặp lại của Apriori sẽ trở thành nút thắt cổ chai I/O nghiêm trọng. FP-Growth với cơ chế chia để trị (Divide and Conquer) và cây tiền tố điều kiện (Conditional FP-Tree) có khả năng đáp ứng tốt các bài toán phân tích dữ liệu lớn.
3. **Chất lượng luật tương đồng**:
   * Cả hai thuật toán đều cho kết quả tập phổ biến và luật kết hợp giống hệt nhau, do đó việc chọn FP-Growth không làm suy giảm chất lượng tri thức khám phá được.
