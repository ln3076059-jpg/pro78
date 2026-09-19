# ĐỒ ÁN MÔN HỌC: NHẬP MÔN KHAI PHÁ DỮ LIỆU VÀ MÁY HỌC

---

# 1. TÊN ĐỀ TÀI

**Ứng dụng kỹ thuật khai phá luật kết hợp tích hợp vào hệ thống quản lý bệnh viện trên công nghệ Java để giải quyết bài toán kết hợp thuốc điều trị**

* **Môn học**: Nhập môn Khai phá Dữ liệu và Máy học (Introduction to Data Mining and Machine Learning)
* **Công nghệ nền tảng**: Java 17 / 21, Spring Boot 3.3.4, Spring MVC, Spring Data JPA, Spring Security 6, Thymeleaf, Bootstrap 5, MySQL, Maven, JUnit 5, Chart.js.
* **Môi trường thực thi**: **100% thuần Java Runtime** (Không sử dụng Python runtime, không gọi Python script từ Spring Boot, không sử dụng API AI/LLM bên ngoài để giả lập).

---

# 2. GIỚI THIỆU

Trong thực hành lâm sàng tại các cơ sở y tế và bệnh viện đa khoa, việc quản lý và điều trị các bệnh mãn tính phức tạp như đái tháo đường (Diabetes Mellitus) thường đòi hỏi phác đồ điều trị đa mô thức (polypharmacy). Bác sĩ phải phối hợp nhiều nhóm hoạt chất cùng lúc (ví dụ: Biguanide kết hợp Sulfonylurea hoặc phối hợp cùng Insulin) nhằm kiểm soát chỉ số HbA1c và ngăn ngừa biến chứng tim mạch, thận.

Tuy nhiên, với lượng hồ sơ bệnh án lên tới hàng chục, hàng trăm nghìn lượt điều trị tích lũy theo thời gian, việc nhận diện thủ công các mẫu đồng kê đơn (co-prescription patterns) là hoàn toàn bất khả thi đối với con người. 

Hệ thống **Hospital Drug Association Mining** được xây dựng nhằm giải quyết bài toán này bằng cách ứng dụng các kỹ thuật khai phá luật kết hợp mạnh mẽ, trích xuất tri thức từ dữ liệu bệnh viện thực tế và tích hợp trực tiếp vào phân hệ kê đơn lâm sàng để hỗ trợ bác sĩ ra quyết định.

---

# 3. MỤC TIÊU

1. **Sử dụng nguồn dữ liệu y tế thực tế, có nguồn gốc rõ ràng và minh bạch**: Bộ dữ liệu chuẩn quốc tế *Diabetes 130-US Hospitals (1999–2008)* từ UCI Machine Learning Repository với hơn 100.000 lượt điều trị.
2. **Khai phá luật kết hợp đồng sử dụng thuốc**: Cài đặt độc lập 2 giải thuật cốt lõi **Apriori** và **FP-Growth** bằng 100% mã nguồn Java.
3. **Đánh giá kết quả bằng các chỉ số toán học chuẩn xác**: Đo lường $\text{Support}$, $\text{Confidence}$, $\text{Lift}$ cùng thời gian thực thi (Runtime) và bộ nhớ tiêu thụ (Memory).
4. **So sánh thực nghiệm và lựa chọn mô hình có căn cứ khoa học**: Dựa trên kết quả đo đạc thực tế từ chương trình để kết luận thuật toán tối ưu.
5. **Tích hợp tri thức khai phá vào chức năng kê đơn**: Hỗ trợ bác sĩ nhận gợi ý thuốc phối hợp ngay khi thêm thuốc vào đơn, tích hợp cảnh báo tương tác thuốc FDA DDI và khuyến cáo lâm sàng bắt buộc.
6. **Thể hiện đầy đủ quy trình Data Mining/Machine Learning chuyên nghiệp**: Tuân thủ chuẩn quốc tế CRISP-DM.

---

# 4. BÀI TOÁN DATA MINING

Mục tiêu chính: **Tìm các thuốc điều trị thường xuất hiện cùng nhau trong cùng một encounter (lượt điều trị) của bệnh nhân.**

* Mỗi lượt điều trị nội trú/ngoại trú của bệnh nhân được định danh bởi một `encounter_id`, và được xem là một giao dịch (**Transaction**).
* Tập mục trong giao dịch là tập hợp các thuốc được chỉ định sử dụng trong đợt điều trị đó:
  * Ví dụ:
    * $\text{Encounter } 1001 \to T_{1001} = \{\text{Metformin}, \text{Insulin}, \text{Glipizide}\}$
    * $\text{Encounter } 1002 \to T_{1002} = \{\text{Insulin}, \text{Glyburide}\}$
    * $\text{Encounter } 1003 \to T_{1003} = \{\text{Metformin}, \text{Insulin}\}$
    * $\text{Encounter } 1004 \to T_{1004} = \{\text{Metformin}, \text{Glipizide}, \text{Insulin}\}$
* Bài toán khai phá sẽ tìm các luật kết hợp dạng:
  $$\{\text{Metformin}\} \implies \{\text{Insulin}\}$$
  $$\{\text{Glipizide}\} \implies \{\text{Insulin}\}$$
  $$\{\text{Metformin}, \text{Glipizide}\} \implies \{\text{Insulin}\}$$

---

# 5. NGUỒN DỮ LIỆU CHÍNH

Dự án tuyệt đối không sử dụng dữ liệu tự tạo làm kết quả chính. Nguồn dữ liệu khai phá chính thức là:

**Diabetes 130-US Hospitals for Years 1999–2008 – UCI Machine Learning Repository**

> **TUYÊN BỐ MINH BẠCH VỀ NGUỒN DỮ LIỆU:**  
> Bộ dữ liệu này là dữ liệu lâm sàng đái tháo đường của mạng lưới 130 bệnh viện Mỹ từ UCI Machine Learning Repository, hoàn toàn **KHÔNG PHẢI là MIMIC-III**. Dự án trình bày chính xác tên và bản chất của bộ dữ liệu này.

---

# 6. UCI DIABETES 130-US HOSPITALS

* **Quy mô bộ dữ liệu**: 101,766 bản ghi điều trị (encounters), bao gồm 71,518 bệnh nhân duy nhất trong 10 năm (1999–2008).
* **Đơn vị công bố**: Center for Clinical and Translational Research, Virginia Commonwealth University & UCI ML Repository.
* **Liên kết kho lưu trữ**: [UCI Dataset #296](https://archive.ics.uci.edu/dataset/296/diabetes+130-us+hospitals+for+years+1999-2008).
* **Các thuộc tính quan trọng**:
  * Định danh: `encounter_id`, `patient_nbr`.
  * Nhân khẩu học: `race`, `gender`, `age` (chia nhóm theo thập kỷ: `[0-10)`, ..., `[70-80)`, `[80-90)`...).
  * Nhập viện/xuất viện: `admission_type_id`, `discharge_disposition_id`, `admission_source_id`.
  * Chỉ số lâm sàng: `time_in_hospital`, `num_lab_procedures`, `num_procedures`, `num_medications`, `number_outpatient`, `number_emergency`, `number_inpatient`.
  * Chẩn đoán ICD-9: `diag_1`, `diag_2`, `diag_3`.
  * **24 cột thuốc điều trị tiểu đường**: `metformin`, `repaglinide`, `nateglinide`, `chlorpropamide`, `glimepiride`, `acetohexamide`, `glipizide`, `glyburide`, `tolbutamide`, `pioglitazone`, `rosiglitazone`, `acarbose`, `miglitol`, `troglitazone`, `tolazamide`, `examide`, `citoglipton`, `insulin`, `glyburide-metformin`, `glipizide-metformin`, `glimepiride-pioglitazone`, `metformin-rosiglitazone`, `metformin-pioglitazone`.

---

# 7. BÀI BÁO THAM KHẢO

Dự án tham khảo công trình nghiên cứu:

> **Tatonetti, N. P., Patrick, P. B., Daneshjou, R., & Altman, R. B. (2012). Data-driven prediction of drug effects and interactions. Science Translational Medicine, 4(125), 125ra31.**  
> DOI: [10.1126/scitranslmed.3003377](https://doi.org/10.1126/scitranslmed.3003377)

### Ý nghĩa khoa học và liên hệ với đề tài:
1. Nghiên cứu của Tatonetti và các cộng sự tại Đại học Stanford đã tiên phong chứng minh rằng: dữ liệu lâm sàng thực tế quy mô lớn (EHR / observational healthcare data) có thể được khai thác theo hướng **định hướng dữ liệu (data-driven)** để phát hiện các tín hiệu sử dụng thuốc (signals), tác dụng phụ và mối tương quan thuốc trên diện rộng.
2. Hướng nghiên cứu này tạo động lực khoa học vững chắc cho đồ án trong việc khai phá các mẫu đồng kê đơn thuốc từ lịch sử điều trị bệnh viện.
3. **Lưu ý phương pháp luận**: Đồ án vận dụng kỹ thuật Khai phá Luật Kết hợp (Association Rule Mining: Apriori & FP-Growth). Phương pháp của đồ án **không tuyên bố là tái hiện hoàn toàn mô hình hồi quy và kiểm định dược cảnh giác của Tatonetti**, mà kế thừa tư tưởng khai phá dữ liệu y tế định hướng thực nghiệm của tác giả.

---

# 8. QUY TRÌNH DMML (CRISP-DM)

Quy trình phát triển tuân thủ đầy đủ 6 giai đoạn của chuẩn công nghiệp **CRISP-DM**:
1. **Business Understanding**: Đặt ra mục tiêu hỗ trợ bác sĩ kê đơn dựa trên mẫu đồng sử dụng thuốc lịch sử.
2. **Data Understanding**: Đọc tệp dữ liệu, phân tích phân phối độ tuổi, số thuốc/encounter, top chẩn đoán ICD-9, top thuốc xuất hiện.
3. **Data Preparation**: Xử lý streaming CSV, làm sạch, quy đổi trạng thái thuốc, loại bỏ missing/invalid record, lọc transaction $\ge 2$ thuốc và lưu CSDL.
4. **Modeling**: Cài đặt thuần Java cho hai thuật toán Apriori và FP-Growth.
5. **Evaluation**: Đánh giá bằng $\text{Support}$, $\text{Confidence}$, $\text{Lift}$, thời gian chạy, RAM tiêu thụ; đối chiếu độ trùng khớp 100% giữa 2 thuật toán.
6. **Deployment**: Tích hợp các luật kết hợp vào giao diện kê đơn thuốc của bác sĩ với cơ chế xếp hạng gợi ý và đối chiếu cảnh báo tương tác thuốc FDA DDI.

---

# 9. DATA UNDERSTANDING

Phân hệ thống kê dữ liệu (`/mining/dataset`) tự động phân tích và hiển thị trực quan:
* **Tổng số encounter**: 101,766 lượt.
* **Số bệnh nhân duy nhất**: 71,518 người.
* **Số thuốc được xem xét**: 24 cột thuốc chuẩn UCI (có 21 thuốc xuất hiện thực tế).
* **Số transaction có $\ge 2$ thuốc**: 31,049 (30.51%).
* **Số transaction có 1 thuốc**: 47,848 (47.02%).
* **Số transaction không có thuốc**: 22,869 (22.47%).
* **Số thuốc trung bình / encounter**: 1.18 thuốc (Tối thiểu: 0, Tối đa: 6 thuốc).
* **Top 5 thuốc xuất hiện nhiều nhất**: `Insulin` (54,383), `Metformin` (20,245), `Glipizide` (12,776), `Glyburide` (10,698), `Pioglitazone` (7,363).
* **Phân phối độ tuổi**: Nhóm `[70-80)` tuổi chiếm cao nhất (25.6%), tiếp theo là `[60-70)` (22.1%) và `[50-60)` (16.9%).
* **Top chẩn đoán**: Bệnh lý tuần hoàn (ICD-9: 390–459), Đái tháo đường (ICD-9: 250.xx), Bệnh lý hô hấp (ICD-9: 460–519).

---

# 10. DATA PREPROCESSING

Module tiền xử lý được kiến trúc hóa qua 3 dịch vụ chuyên trách:
* `DatasetImportService`: Quản lý nạp tệp CSV từ giao diện upload hoặc từ cấu hình `dataset.path`.
* `DataPreprocessingService`: Bộ phân tích dữ liệu theo luồng (Streaming CSV Parser qua Apache Commons CSV), loại bỏ bản ghi không hợp lệ, lưu trữ thống kê Data Understanding.
* `TransactionBuilderService`: Gom các thuốc theo `encounter_id`, loại bỏ các encounter có $< 2$ thuốc, lưu batch vào MySQL.

### Quy trình tiền xử lý:
$$\text{CSV File} \longrightarrow \text{Validation} \longrightarrow \text{Cleaning} \longrightarrow \text{Medicine Extraction} \longrightarrow \text{Transaction Building} \longrightarrow \text{MySQL Persistence}$$

---

# 11. CÁCH XÁC ĐỊNH THUỐC TRONG TRANSACTION

Trong dữ liệu gốc UCI Diabetes, mỗi cột thuốc mang 1 trong 4 giá trị:
* `No`: Thuốc không được kê/không được sử dụng trong lượt điều trị này.
* `Steady`: Thuốc được chỉ định sử dụng và duy trì liều ổn định.
* `Up`: Thuốc được chỉ định sử dụng và có tăng liều.
* `Down`: Thuốc được chỉ định sử dụng và có giảm liều.

### Quy ước tiền xử lý:
$$\text{Giá trị} = \text{"No"} \implies \text{Không đưa vào Transaction}$$
$$\text{Giá trị} \in \{\text{"Steady"}, \text{"Up"}, \text{"Down"}\} \implies \text{Đưa thuốc vào Transaction}$$

*Ví dụ*:
* $\text{metformin} = \text{Steady}$, $\text{insulin} = \text{Up}$, $\text{glipizide} = \text{No}$
* $\implies \text{Transaction} = \{\text{Metformin}, \text{Insulin}\}$ (Không có Glipizide).

---

# 12. THUẬT TOÁN APRIORI

Thuật toán Apriori được cài đặt thuần Java trong `AprioriMiningService` theo đúng interface `AssociationMiningService`:

```java
public interface AssociationMiningService {
    MiningResult mine(List<Set<String>> transactions, MiningParameters parameters);
    MiningResult mineWithIds(List<Set<Long>> transactions, MiningParameters parameters);
}
```

* **Nguyên lý Apriori (Apriori Property)**: Mọi tập con của một tập phổ biến bắt buộc phải là tập phổ biến. Nếu một tập mục có độ hỗ trợ nhỏ hơn $\text{minSupport}$, toàn bộ các tập cha mở rộng của nó đều bị loại bỏ ngay lập tức (Apriori Pruning).
* **Quy trình thực thi**:
  1. $L_1$: Đếm và lọc các 1-itemset phổ biến.
  2. Sinh ứng viên $C_k$ từ $L_{k-1} \Join L_{k-1}$ với bước kiểm tra tập con (Has Infrequent Subset Check).
  3. Quét tập giao dịch để tính $\text{Support}$ của $C_k$, lọc ra $L_k$.
  4. Lặp lại cho tới khi không sinh thêm được tập phổ biến mới hoặc đạt `maxItemsetSize`.

---

# 13. THUẬT TOÁN FP-GROWTH

Thuật toán FP-Growth (Frequent Pattern Growth) được cài đặt thuần Java trong `FPGrowthMiningService`:
* **Khắc phục nhược điểm của Apriori**: Hoàn toàn **không sinh tập ứng viên trung gian** $C_k$ và **chỉ quét cơ sở dữ liệu đúng 2 lần**.
* **Cấu trúc cây FP-Tree**: Nén toàn bộ giao dịch vào cây tiền tố (Prefix Tree) có Header Table trỏ đến các nút cùng tên qua con trỏ liên kết (node links).
* **Khai phá đệ quy**: Xây dựng cơ sở mẫu điều kiện (Conditional Pattern Base) từ các nhánh con, hình thành cây FP-Tree điều kiện và trích xuất trực tiếp các tập mục phổ biến.

---

# 14. CHỈ SỐ SUPPORT (ĐỘ HỖ TRỢ)

$$\text{Support}(X \implies Y) = \frac{\text{count}(X \cup Y)}{\text{Total Transactions}}$$

* **Ý nghĩa**: Tỷ lệ phần trăm các lượt điều trị có sử dụng đồng thời cả tập thuốc $X$ và tập thuốc $Y$.
* **Ví dụ**: Trong 31,049 giao dịch, có 3,819 giao dịch kê đồng thời `{Glyburide, Metformin}`:
  $$\text{Support}(\{\text{Glyburide}\} \implies \{\text{Metformin}\}) = \frac{3,819}{31,049} \approx 0.1230 \text{ (12.30\%)}$$

---

# 15. CHỈ SỐ CONFIDENCE (ĐỘ TIN CẬY)

$$\text{Confidence}(X \implies Y) = \frac{\text{Support}(X \cup Y)}{\text{Support}(X)} = \frac{\text{count}(X \cup Y)}{\text{count}(X)}$$

* **Ý nghĩa**: Xác suất có điều kiện bác sĩ chỉ định thêm thuốc $Y$ khi bệnh nhân đã được chỉ định sử dụng thuốc $X$.
* **Ví dụ**: `{Glyburide}` xuất hiện trong 7,274 giao dịch, `{Glyburide, Metformin}` xuất hiện trong 3,819 giao dịch:
  $$\text{Confidence}(\{\text{Glyburide}\} \implies \{\text{Metformin}\}) = \frac{3,819}{7,274} \approx 0.5250 \text{ (52.50\%)}$$

---

# 16. CHỈ SỐ LIFT (ĐỘ NÂNG)

$$\text{Lift}(X \implies Y) = \frac{\text{Confidence}(X \implies Y)}{\text{Support}(Y)} = \frac{\text{Support}(X \cup Y)}{\text{Support}(X) \times \text{Support}(Y)}$$

* **Ý nghĩa thống kê**:
  * $\text{Lift} > 1$: Thuốc $X$ và thuốc $Y$ có xu hướng **đồng xuất hiện thực tế cao hơn so với mức ngẫu nhiên độc lập**. Mối quan hệ kết hợp dương tính.
  * $\text{Lift} = 1$: Việc kê đơn thuốc $X$ và $Y$ hoàn toàn độc lập với nhau.
  * $\text{Lift} < 1$: Thuốc $X$ và $Y$ ít khi xuất hiện cùng nhau hơn so với kỳ vọng ngẫu nhiên (có thể là thay thế nhau hoặc kiêng kỵ).

> **CẢNH BÁO NGUYÊN TẮC LÂM SÀNG:**  
> $\text{Lift} > 1$ **hoàn toàn KHÔNG đồng nghĩa với việc hai thuốc này an toàn khi kết hợp!** Chỉ số Lift chỉ phản ánh thực tế thống kê trong dữ liệu lịch sử, không chứng minh mối quan hệ nhân quả (causality) hay độ an toàn y khoa.

---

# 17. ĐÁNH GIÁ MÔ HÌNH (MODEL EVALUATION)

Module `MiningEvaluationService` thực hiện benchmark đối đầu giữa Apriori và FP-Growth trên cùng một tập giao dịch và cùng bộ tham số.

### Bảng Kết Quả Đo Lường Thực Tế từ Dataset Đầy Đủ (31,049 Transactions):

| Thuật toán | Giao dịch | Support | Confidence | Thời gian chạy (Runtime) | Bộ nhớ RAM | Số tập phổ biến | Số luật kết hợp |
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

# 18. LỰA CHỌN THUẬT TOÁN (ALGORITHM SELECTION)

Dựa trên kết quả đo lường thực nghiệm khoa học, hệ thống chính thức lựa chọn **FP-Growth** làm thuật toán khuyến nghị cốt lõi:
1. **Thời gian thực thi**: FP-Growth nhanh hơn Apriori **3.88 lần** ở $\text{minSupport}=0.01$ (83 ms so với 322 ms).
2. **Khả năng mở rộng (Scalability)**: FP-Growth chỉ quét CSDL 2 lần và không sinh hàng nghìn tập ứng viên trung gian $C_k$, giúp tiết kiệm bộ nhớ và tránh nghẽn I/O khi dữ liệu tăng trưởng.
3. **Độ chính xác hoàn hảo**: Cả 2 thuật toán đều sinh ra số lượng tập phổ biến và luật kết hợp **trùng khớp 100%**.

---

# 19. CƠ SỞ DỮ LIỆU (DATABASE SCHEMA)

CSDL MySQL gồm 21 bảng được chuẩn hóa:
* Quản trị: `users`, `roles`, `user_roles`.
* Lâm sàng: `patients`, `doctors`, `medical_visits`, `medicines`, `prescriptions`, `prescription_items`.
* Tiền xử lý & Nạp dữ liệu: `dataset_imports`, `dataset_statistics`.
* Giao dịch: `transactions`, `transaction_items`.
* Khai phá dữ liệu: `mining_runs`, `frequent_itemsets`, `frequent_itemset_items`, `association_rules`, `association_rule_antecedents`, `association_rule_consequents`.
* Đánh giá & Tương tác: `algorithm_benchmarks`, `drug_interactions`.

> **Thiết kế tiền tố / hệ quả chuẩn hóa**: `association_rule_antecedents` và `association_rule_consequents` lưu từng `medicine_id` độc lập (thay vì chuỗi gộp), giúp tìm kiếm luật bằng Indexing cực nhanh theo thời gian thực khi bác sĩ kê đơn.

---

# 20. KIẾN TRÚC HỆ THỐNG (SYSTEM ARCHITECTURE)

Mô hình phân tầng chuẩn mực:
* **Presentation Layer**: Thymeleaf, HTML5/CSS3, Bootstrap 5, Chart.js tương tác không đồng bộ (AJAX Fetch API).
* **Controller Layer**: Xử lý Web MVC và REST API (`MiningApiController`, `RecommendationApiController`).
* **Service Layer**: Tách biệt rõ giữa phân hệ Khai phá Dữ liệu (`com.hospital.datamining.service.mining.*`) và phân hệ Nghiệp vụ Lâm sàng (`com.hospital.datamining.service.prescription.*`).
* **Persistence Layer**: Spring Data JPA & Hibernate kết nối MySQL.

---

# 21. TÍCH HỢP VÀO CHỨC NĂNG KÊ ĐƠN (PRESCRIPTION INTEGRATION)

Đây là chức năng ứng dụng thực tiễn then chốt của đồ án:
1. Bác sĩ mở màn hình khám bệnh (`/prescriptions/create`) và chọn một thuốc khởi đầu (ví dụ: `Metformin`).
2. Giao diện gửi yêu cầu AJAX tới `DrugRecommendationService`.
3. Thuật toán tìm kiếm luật thực hiện 2 tầng:
   * **Tầng 1 (Ưu tiên)**: Tìm các luật đa tiền tố (Multi-antecedent) khớp chính xác toàn bộ danh sách thuốc hiện có trong đơn.
   * **Tầng 2 (Fallback)**: Tìm các luật đơn tiền tố (Single-antecedent) tương ứng với từng thuốc đã chọn nếu tầng 1 không có kết quả.
4. Lọc bỏ các thuốc đã có trong đơn thuốc.
5. Sắp xếp thứ bậc ưu tiên: $\text{Lift} \downarrow \implies \text{Confidence} \downarrow \implies \text{Support} \downarrow$.
6. Hiển thị bảng gợi ý trực quan với huy hiệu chỉ số và nút "Thêm vào đơn".

---

# 22. PHÂN HỆ TƯƠNG TÁC THUỐC (OPTIONAL DDI CHECK)

Hệ thống tích hợp dữ liệu cảnh báo tương tác thuốc từ các nguồn y khoa uy tín (FDA DDI / DailyMed) qua bảng `drug_interactions` và `DrugInteractionService`:
* Khi gợi ý một thuốc kết hợp (ví dụ: `{Metformin}` kết hợp `{Insulin}`), hệ thống tự động đối chiếu chéo cặp thuốc này trong cơ sở dữ liệu tương tác.
* Trạng thái tương tác:
  * `KNOWN`: Đã ghi nhận tương tác trong danh mục FDA.
  * `NOT FOUND`: Chưa ghi nhận cảnh báo tương tác trong danh mục hiện tại.
* Mức độ cảnh báo: `Major`, `Moderate`, `Minor`.
* **Phân tách rạch ròi**:
  * *Khai phá luật kết hợp* $\to$ Cho biết các thuốc thường được kê cùng nhau trong thực tế.
  * *FDA DDI* $\to$ Cho biết cặp thuốc có tương tác bất lợi hay không.
  * Hệ thống tuyệt đối không suy diễn "thường đi cùng nhau là an toàn".

---

# 23. KHUYẾN CÁO MIỄN TRỪ TRÁCH NHIỆM Y KHOA (DISCLAIMER)

Tại giao diện kê đơn thuốc của bác sĩ, hệ thống bắt buộc hiển thị cảnh báo lâm sàng:

> **Gợi ý này dựa trên các mẫu đồng sử dụng thuốc trong dữ liệu lịch sử và chỉ dùng để tham khảo. Hệ thống không tự động xác nhận hiệu quả hoặc độ an toàn của phối hợp thuốc. Quyết định kê đơn cuối cùng thuộc về bác sĩ.**

---

# 24. HƯỚNG DẪN CÀI ĐẶT (INSTALLATION)

### Yêu cầu môi trường:
* **Java Development Kit (JDK)**: Java 17 hoặc Java 21 LTS.
* **Apache Maven**: Phiên bản 3.8+ (đã tích hợp sẵn `mvn.cmd`).
* **Hệ quản trị CSDL**: MySQL 8.0+ (hoặc chạy H2 in-memory profile để thử nghiệm nhanh).

### Khởi tạo cơ sở dữ liệu MySQL:
1. Đăng nhập vào MySQL:
   ```bash
   mysql -u root -p
   ```
2. Tạo CSDL:
   ```sql
   CREATE DATABASE hospital_drug_mining CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Nạp cấu trúc bảng và dữ liệu mẫu:
   ```bash
   mysql -u root -p hospital_drug_mining < database/schema.sql
   mysql -u root -p hospital_drug_mining < database/sample-data.sql
   ```

---

# 25. CẤU HÌNH HỆ THỐNG (CONFIGURATION)

Sao chép file cấu hình mẫu:
```bash
cp application-example.properties src/main/resources/application.properties
```

Nội dung cấu hình chính trong `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_drug_mining?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Đường dẫn tới bộ dữ liệu UCI Diabetes
dataset.path=dataset/diabetic_data.csv
dataset.sample-path=dataset/diabetic_data_sample.csv
```

---

# 26. KHỞI CHẠY DỰ ÁN (RUN PROJECT)

### Bước 1: Kiểm thử mã nguồn
```bash
mvn clean test
```
*(Kết quả: 24 unit tests PASS 100%).*

### Bước 2: Đóng gói dự án thành file JAR thực thi
```bash
mvn clean package
```

### Bước 3: Khởi chạy ứng dụng Spring Boot
```bash
mvn spring-boot:run
```
hoặc chạy trực tiếp file JAR:
```bash
java -jar target/hospital-drug-association-mining-1.0.0.jar
```

Truy cập ứng dụng tại trình duyệt: **`http://localhost:8080`**

---

# 27. TÀI KHOẢN TRẢI NGHIỆM DEMO

| Vai trò | Tên đăng nhập | Mật khẩu | Quyền hạn truy cập |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin` | `admin123` | Quản trị Dataset, Tiền xử lý, Chạy Apriori, Chạy FP-Growth, Xem Luật, Chạy Benchmark, Xem Lịch sử |
| **Bác sĩ lâm sàng (Doctor)** | `doctor` | `doctor123` | Quản lý bệnh nhân, Lập lượt khám, Kê đơn thuốc, Nhận gợi ý luật kết hợp thời gian thực & Cảnh báo DDI |

---

# 28. HẠN CHẾ CỦA ĐỀ TÀI (LIMITATIONS)

1. **Phạm vi đối tượng bệnh nhân**: Bộ dữ liệu UCI Diabetes 130-US tập trung chủ yếu vào bệnh nhân mắc đái tháo đường nằm viện nội trú, do đó các luật khai phá được không đại diện cho tất cả các nhóm bệnh lý khác hoặc mọi mô hình bệnh viện.
2. **Bản chất của Luật Kết hợp**: Các luật kết hợp chỉ phản ánh mối tương quan **đồng xuất hiện mang tính thống kê (co-occurrence)**, hoàn toàn không chứng minh quan hệ nhân quả (causality) hay tính an toàn dược lý (clinical safety).
3. **Mức độ chi tiết của dữ liệu**: Các trường thuốc trong dữ liệu UCI chỉ phản ánh trạng thái sử dụng (`Steady`, `Up`, `Down`) mà không có thông tin chi tiết về liều lượng miligram cụ thể theo giờ, đường dùng chi tiết hoặc tương tác thời gian phức tạp như các hệ thống kê đơn chuyên sâu.

---

# 29. HƯỚNG PHÁT TRIỂN (FUTURE WORK)

1. **Mở rộng nguồn dữ liệu đa trung tâm**: Tích hợp các tập dữ liệu lâm sàng chuyên sâu khác như MIMIC-IV, eICU Collaborative Research Database để mở rộng phạm vi chẩn đoán đa chuyên khoa.
2. **Làm giàu tri thức Dược lý**: Kết nối API thời gian thực với DrugBank, RxNorm, ATC (Anatomical Therapeutic Chemical Classification) và cơ sở dữ liệu biến cố có hại FDA FAERS.
3. **Đồ thị tri thức Y tế (DDI Knowledge Graph)**: Xây dựng Knowledge Graph liên kết bệnh lý - hoạt chất - tương tác thuốc để tối ưu hóa hệ thống hỗ trợ ra quyết định lâm sàng (Clinical Decision Support System - CDSS).

---

# 30. TÀI LIỆU THAM KHẢO (REFERENCES)

1. **Tatonetti, N. P., Patrick, P. B., Daneshjou, R., & Altman, R. B. (2012)**. *Data-driven prediction of drug effects and interactions*. Science Translational Medicine, 4(125), 125ra31.
2. **Strack, B., DeShazo, J. P., Gennings, C., et al. (2014)**. *Impact of HbA1c Measurement on Hospital Readmission Rates: Analysis of 70,000 Clinical Database Patient Records*. BioMed Research International, 2014, 781670. (Bài báo gốc của bộ dữ liệu UCI Diabetes 130-US Hospitals).
3. **Agrawal, R., & Srikant, R. (1994)**. *Fast algorithms for mining association rules in large databases*. Proceedings of the 20th International Conference on Very Large Data Bases (VLDB), 487–499.
4. **Han, J., Pei, J., & Yin, Y. (2000)**. *Mining frequent patterns without candidate generation*. ACM SIGMOD Record, 29(2), 1–12.
5. **UCI Machine Learning Repository**: *Diabetes 130-US Hospitals for Years 1999–2008 Dataset*. https://archive.ics.uci.edu/dataset/296/diabetes+130-us+hospitals+for+years+1999-2008.
