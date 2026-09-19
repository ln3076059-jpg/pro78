# BÁO CÁO KẾT QUẢ THỰC NGHIỆM DATA MINING (EXPERIMENT RESULTS REPORT)

---

## 1. Thông tin Tập Dữ liệu Thực nghiệm

Toàn bộ kết quả thực nghiệm trong báo cáo này được đo lường và tính toán tự động bằng chương trình thuần Java từ bộ dữ liệu thực tế:

* **Tên bộ dữ liệu**: **Diabetes 130-US Hospitals for Years 1999–2008** (UCI Machine Learning Repository).
* **Nguồn tệp dữ liệu**: `dataset/diabetic_data.csv` (Dung lượng: 19.1 MB, 101,766 dòng).
* **Nguồn gốc chính thức (Provenance)**: UCI Machine Learning Repository (Dataset ID 296, Strack et al., 2014).
* **Môi trường đo lường**: Java 21 LTS, Spring Boot 3.3.4, hệ điều hành Windows x64.
* **Quy ước tính toán**: 100% dữ liệu thật từ bộ dữ liệu y tế, không giả lập, không hard-code bất kỳ chỉ số nào.
* **Phương pháp đo lường (Methodology)**: Thực hiện 2 lần chạy warm-up để ổn định JVM JIT Compiler, sau đó đo lường lặp 5 lần chính thức để lấy giá trị trung vị (Median Runtime), giảm thiểu tối đa sai số từ bộ dọn rác JVM Garbage Collector và điều phối OS.
* **Quy ước đo bộ nhớ**: Chỉ số bộ nhớ (RAM - MB) là phép ước lượng độ chênh lệch Heap JVM (Approximate JVM Heap Delta) giữa trước và sau quá trình khai phá.

---

## 2. Kết quả Tiền Xử Lý Dữ Liệu (Preprocessing Summary)

Từ 101,766 lượt nằm viện (encounters) trong tệp dữ liệu gốc `diabetic_data.csv`:

| Thuộc tính thống kê | Giá trị đo lường thực tế |
| :--- | :--- |
| **Tổng số dòng đọc vào ban đầu** | 101,766 dòng |
| **Số dòng hợp lệ (Valid records)** | 101,766 dòng (100%) |
| **Số bản ghi bị loại (Invalid records)** | 0 dòng |
| **Số lượng bệnh nhân duy nhất (Unique Patients)** | **71,518 bệnh nhân** |
| **Số đợt nằm viện duy nhất (Unique Encounters)** | **101,766 lượt** |
| **Số cột hoạt chất thuốc cụ thể từ UCI** | **23 cột thuốc** (ngoài 2 thuộc tính quản trị: `diabetesMed` và `change`) |
| **Số loại thuốc xuất hiện thực tế** | **21 loại thuốc** |
| **Số giao dịch hợp lệ ($\ge 2$ thuốc đưa vào Data Mining)** | **31,049 transactions** (chiếm 30.51%) |
| **Số giao dịch có 1 thuốc** | 47,848 encounters (chiếm 47.02% - không thể tạo luật kết hợp $\ge 2$ thuốc) |
| **Số giao dịch không có thuốc** | 22,869 encounters (chiếm 22.47% - không có thuốc nào active) |
| **Số thuốc trung bình / encounter** | 1.18 thuốc |
| **Số thuốc ít nhất / encounter** | 0 thuốc |
| **Số thuốc nhiều nhất / encounter** | 6 thuốc |

---

## 3. Bảng So Sánh Hiệu Năng Đối Đầu: Apriori vs FP-Growth

Thực nghiệm đo lường chuẩn hóa đa lượt (2 lượt warm-up + 5 lượt đo lường chính thức lấy **Median Runtime** và **StdDev**) trên toàn bộ **31,049 transactions** với cố định ngưỡng $\text{minConfidence} = 0.30$ ($30\%$), $\text{minLift} = 1.0$, $\text{maxItemsetSize} = 5$ qua các ngưỡng $\text{minSupport}$ khác nhau:

| Thuật toán | Số lượng Giao dịch | Ngưỡng Support | Ngưỡng Conf | Thời gian trung vị (Median Runtime) | Bộ nhớ ước lượng (Heap Delta) | Số tập phổ biến (Itemsets) | Số luật kết hợp (Rules) | Rule Jaccard | Itemset Jaccard |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Apriori** | 31,049 | **0.01 (1%)** | 0.30 | 243 ms (±33.5 ms) | ~8.02 MB | **45** | **3** | **100.0%** | **100.0%** |
| **FP-Growth** | 31,049 | **0.01 (1%)** | 0.30 | **46 ms (±5.2 ms)** | **~8.00 MB** | **45** | **3** | **100.0%** | **100.0%** |
| **Apriori** | 31,049 | **0.02 (2%)** | 0.30 | 142 ms (±11.9 ms) | ~21.01 MB | **31** | **2** | **100.0%** | **100.0%** |
| **FP-Growth** | 31,049 | **0.02 (2%)** | 0.30 | **43 ms (±3.5 ms)** | **~8.00 MB** | **31** | **2** | **100.0%** | **100.0%** |
| **Apriori** | 31,049 | **0.05 (5%)** | 0.30 | 98 ms (±8.3 ms) | ~38.00 MB | **19** | **1** | **100.0%** | **100.0%** |
| **FP-Growth** | 31,049 | **0.05 (5%)** | 0.30 | **39 ms (±2.8 ms)** | **~7.50 MB** | **19** | **1** | **100.0%** | **100.0%** |
| **Apriori** | 31,049 | **0.10 (10%)**| 0.30 | 86 ms (±3.4 ms) | ~35.50 MB | **14** | **1** | **100.0%** | **100.0%** |
| **FP-Growth** | 31,049 | **0.10 (10%)**| 0.30 | **35 ms (±1.1 ms)** | **~7.50 MB** | **14** | **1** | **100.0%** | **100.0%** |

---

## 4. Kiểm thử Tương Thích & Tính Nhất Quán Giữa Hai Thuật Toán

Kết quả kiểm thử đối chiếu tuyệt đối từ `ExactEquivalenceTest` và `MiningEvaluationServiceTest` khẳng định:
1. **Độ tương đồng tập luật Jaccard đạt 100%**: Tại mọi ngưỡng thử nghiệm, chỉ số Rule Jaccard Similarity và Itemset Jaccard Similarity giữa hai tập sinh bởi Apriori và FP-Growth đều đạt mức $1.0$ ($100.0\%$).
2. **Khớp từng phần tử và tần suất hỗ trợ (Support Count)**: Danh sách các itemset cùng chỉ số support count tuyệt đối của từng tập mục sinh ra từ Apriori và FP-Growth hoàn toàn trùng khớp chính xác từng phần tử.
3. **Độ tin cậy toán học**: Cả 3 chỉ số $\text{Support}$, $\text{Confidence}$, $\text{Lift}$ giữa các luật đối ứng đều bằng nhau trong giới hạn sai số số thực $\epsilon = 10^{-6}$.

---

## 5. Danh Sách Các Luật Kết Hợp Khai Phá Được (Top Association Rules)

Tại ngưỡng $\text{minSupport} = 0.01$ ($1\%$), $\text{minConfidence} = 0.30$ ($30\%$), $\text{minLift} > 1.0$:

| STT | Tiền đề (Antecedent $X$) | Hệ quả (Consequent $Y$) | Support | Confidence | Lift | Diễn giải Thống kê Khách quan |
| :---: | :--- | :--- | :---: | :---: | :---: | :--- |
| **1** | `{Glyburide-Metformin}` | `{Insulin}` | **0.0133** (1.33%) | **0.8227** (82.27%) | **1.0908** | Trong dữ liệu bệnh nhân nằm viện, với các lượt điều trị có ghi nhận viên kết hợp Glyburide-Metformin, có tới 82.27% lượt điều trị đồng thời ghi nhận sử dụng Insulin ngoại sinh (độ nâng Lift = 1.09, vượt kỳ vọng đồng xuất hiện độc lập). |
| **2** | `{Repaglinide}` | `{Insulin}` | **0.0281** (2.81%) | **0.7558** (75.58%) | **1.0021** | Trong các đợt nằm viện có sử dụng Repaglinide, 75.58% ghi nhận đồng xuất hiện cùng Insulin (Lift = 1.002). |
| **3** | `{Glyburide}` | `{Metformin}` | **0.1230** (12.30%) | **0.5250** (52.50%) | **1.0513** | Trong các đợt nằm viện có dùng Glyburide, 52.50% ghi nhận đồng thời sử dụng Metformin (độ phổ biến đạt 12.30% trên toàn bộ giao dịch đa thuốc, Lift = 1.05). |

---

## 6. Đánh Giá Độ Nhạy của Ngưỡng (Threshold Sensitivity)

1. **Khi giảm $\text{minSupport}$ từ $0.10 \to 0.01$**:
   * Số lượng tập phổ biến tăng từ 14 lên 45 tập.
   * Số lượng luật kết hợp tìm được tăng từ 1 lên 3 luật.
   * Thời gian chạy của Apriori tăng từ 86 ms lên 243 ms (tăng gần 3 lần, độ phân tán thời gian tăng với StdDev ±33.5 ms) do số lượng tổ hợp ứng viên $C_k$ cần kiểm tra tăng vọt.
   * Thời gian chạy của FP-Growth chỉ tăng từ 35 ms lên 46 ms (chỉ tăng 1.3 lần, StdDev rất thấp ±5.2 ms), cho thấy độ ổn định cao hơn hẳn khi ngưỡng hỗ trợ giảm thấp.
2. **Khoảng ngưỡng khuyến nghị trong thực tế**:
   * Với bộ dữ liệu UCI Diabetes, việc phối hợp thuốc tập trung vào một số nhóm thuốc chủ lực (Insulin, Metformin, Glyburide, Glipizide). Ngưỡng $\text{minSupport} \in [0.01, 0.02]$ và $\text{minConfidence} \in [0.30, 0.50]$ là khoảng tham số phù hợp để vừa khám phá được các mẫu đồng sử dụng thuốc có ý nghĩa thực tế, vừa tránh bùng nổ tổ hợp ứng viên.

---

## 7. Lựa Chọn Thuật Toán Cuối Cùng & Cơ Sở Khoa Học

### 7.1. Kết luận lựa chọn
Hệ thống chính thức đề xuất và lựa chọn **Thuật toán FP-Growth (Frequent Pattern Growth)** làm mô hình hoạt động (Active Model) nòng cốt cho phân hệ khai phá dữ liệu thời gian thực.

### 7.2. Cơ sở lý do khoa học dựa trên thực nghiệm:
1. **Tốc độ vượt trội (Runtime)**:
   * Tại ngưỡng $\text{minSupport} = 0.01$, FP-Growth (46 ms) nhanh hơn **5.3 lần** so với Apriori (243 ms).
   * Bản chất: Apriori sinh tổ hợp ứng viên $C_k$ khổng lồ và quét dữ liệu nhiều lượt; FP-Growth chỉ quét 2 lần và nén thông tin vào cây FP-Tree.
2. **Khả năng mở rộng (Scalability)**:
   * Khi quy mô dữ liệu kê đơn bệnh viện mở rộng lên hàng triệu bản ghi, FP-Growth với cơ chế cây tiền tố điều kiện (Conditional FP-Tree) giúp duy trì hiệu năng cao mà không gây nghên I/O quét dữ liệu.
3. **Chất lượng luật tương đương**:
   * Rule Jaccard và Itemset Jaccard Similarity đều đạt 100.0%, kết quả tập phổ biến hoàn toàn trùng khớp giữa 2 thuật toán.

---

## 8. Giới Hạn Nghiên Cứu & Cảnh Báo Lâm Sàng (Limitations & Clinical Disclaimer)

1. **Tính chất bộ dữ liệu**: Tập dữ liệu UCI Diabetes 130-US Hospitals chỉ tập trung vào bệnh nhân đái tháo đường nằm viện từ năm 1999 đến 2008 tại 130 cơ sở y tế Hoa Kỳ, không mang tính đại diện cho toàn bộ bệnh nhân của mọi chuyên khoa hoặc mô hình bệnh tật khác.
2. **Tương quan không phải nhân quả (Correlation $\ne$ Causation)**: Các luật kết hợp khám phá được thể hiện mối quan hệ **đồng xuất hiện trong lịch sử điều trị** (co-occurrence), tuyệt đối không chứng minh quan hệ nguyên nhân - kết quả.
3. **Không chứng minh tính an toàn hoặc hiệu quả lâm sàng**: Chỉ số $\text{Lift} > 1$ chỉ thể hiện hai hoạt chất thường được kê cùng nhau với tần suất cao hơn kỳ vọng độc lập ngẫu nhiên; không đồng nghĩa với việc phối hợp hai thuốc này là an toàn hoặc có hiệu quả điều trị vượt trội.
4. **Quyền quyết định y khoa**: Mọi gợi ý phối hợp thuốc chỉ mang tính chất tham khảo cho bác sĩ điều trị. Quyết định kê đơn cuối cùng phụ thuộc hoàn toàn vào chuyên môn lâm sàng của bác sĩ dựa trên thể trạng cụ thể của bệnh nhân.
5. **Cơ sở tri thức DDI**: Cơ sở dữ liệu tương tác thuốc trong hệ thống là tập dữ liệu minh họa (Demo DDI Knowledge Base) nhằm chứng minh năng lực cảnh báo an toàn của phần mềm; không thay thế cho các cơ sở dữ liệu dược thư quốc gia chính thức.
