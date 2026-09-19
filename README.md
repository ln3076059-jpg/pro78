# ĐỒ ÁN MÔN HỌC: NHẬP MÔN KHAI PHÁ DỮ LIỆU VÀ MÁY HỌC

---

# 1. TÊN ĐỀ TÀI

**Ứng dụng kỹ thuật khai phá luật kết hợp tích hợp vào hệ thống quản lý bệnh viện trên công nghệ Java để giải quyết bài toán kết hợp thuốc điều trị**

* **Môn học**: Nhập môn Khai phá Dữ liệu và Máy học (Introduction to Data Mining and Machine Learning)
* **Công nghệ nền tảng**: Java 17 / 21, Spring Boot 3.3.4, Spring MVC, Spring Data JPA, Spring Security 6, Thymeleaf, Bootstrap 5, MySQL, H2 Database, Maven, JUnit 5, Chart.js.
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
3. **Đánh giá kết quả bằng các chỉ số toán học chuẩn xác**: Đo lường $\text{Support}$, $\text{Confidence}$, $\text{Lift}$ cùng thời gian thực thi trung vị (Median Runtime), độ lệch chuẩn (StdDev), bộ nhớ tiêu thụ (Memory Heap Delta) và độ tương đồng Jaccard.
4. **So sánh thực nghiệm và lựa chọn mô hình có căn cứ khoa học**: Dựa trên kết quả đo đạc thực tế nhiều lượt từ chương trình để kết luận thuật toán tối ưu và chỉ định làm Active Model.
5. **Tích hợp tri thức khai phá vào chức năng kê đơn**: Hỗ trợ bác sĩ nhận gợi ý thuốc phối hợp ngay khi thêm thuốc vào đơn thông qua truy vấn bảng chuẩn hóa 2 tầng (Two-tier), tích hợp kiểm tra tương tác thuốc (Demo DDI Knowledge Base) và khuyến cáo miễn trừ lâm sàng bắt buộc.
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

# 5. NGUỒN DỮ LIỆU CHÍNH & PROVENANCE

Dự án tuyệt đối không sử dụng dữ liệu tự tạo làm kết quả chính. Nguồn dữ liệu khai phá chính thức là:

**Diabetes 130-US Hospitals for Years 1999–2008 – UCI Machine Learning Repository**

> **TUYÊN BỐ MINH BẠCH VỀ NGUỒN DỮ LIỆU:**  
> Bộ dữ liệu này là dữ liệu lâm sàng đái tháo đường của mạng lưới 130 bệnh viện Mỹ từ UCI Machine Learning Repository (Dataset ID: 296), hoàn toàn **KHÔNG PHẢI là MIMIC-III**. Dự án trình bày chính xác tên và bản chất của bộ dữ liệu này. Phần code MIMIC cũ được cô lập trong trạng thái `@Deprecated` chỉ dùng cho mục đích tương thích tham khảo mở rộng.

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
  * **23 cột thuốc điều trị đái tháo đường cụ thể**: `metformin`, `repaglinide`, `nateglinide`, `chlorpropamide`, `glimepiride`, `acetohexamide`, `glipizide`, `glyburide`, `tolbutamide`, `pioglitazone`, `rosiglitazone`, `acarbose`, `miglitol`, `troglitazone`, `tolazamide`, `examide`, `citoglipton`, `insulin`, `glyburide-metformin`, `glipizide-metformin`, `glimepiride-pioglitazone`, `metformin-rosiglitazone`, `metformin-pioglitazone`.
  * Kèm 2 thuộc tính tóm tắt tổng quát: `change` (thay đổi liều) và `diabetesMed` (có dùng thuốc tiểu đường hay không).
* **Kịch bản tải dữ liệu tự động**: `scripts/download_dataset.sh` và `scripts/download_dataset.ps1` ưu tiên tải trực tiếp file nén ZIP từ UCI ML Repository chính thức (`https://archive.ics.uci.edu/static/public/296/diabetes+130-us+hospitals+for+years+1999-2008.zip`), tự động giải nén và kiểm tra `diabetic_data.csv`, `IDs_mapping.csv`. Nếu đường truyền UCI gián đoạn, script tự động chuyển sang mirror dự phòng GitHub.

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
3. **Data Preparation**: Xử lý streaming CSV, làm sạch, quy đổi trạng thái thuốc, loại bỏ missing/invalid record, lọc transaction $\ge 2$ thuốc và lưu CSDL MySQL lâu dài.
4. **Modeling**: Cài đặt thuần Java cho hai thuật toán Apriori và FP-Growth với kiểm soát tính hợp lệ của tham số.
5. **Evaluation**: Đánh giá bằng $\text{Support}$, $\text{Confidence}$, $\text{Lift}$, thời gian chạy trung vị qua benchmark nhiều lượt (2 warm-ups, 5 measured runs), RAM tiêu thụ (Heap Delta) và Jaccard Similarity.
6. **Deployment**: Tích hợp mô hình được lựa chọn (Active Model) vào giao diện kê đơn của bác sĩ với cơ chế xếp hạng gợi ý 2 tầng và đối chiếu tương tác thuốc.

---

# 9. DATA UNDERSTANDING

Phân hệ thống kê dữ liệu (`/mining/dataset`) tự động phân tích và hiển thị trực quan:
* **Tổng số encounter**: 101,766 lượt.
* **Số bệnh nhân duy nhất**: 71,518 người.
* **Số thuốc được xem xét**: 23 cột thuốc cụ thể chuẩn UCI (có 21 thuốc xuất hiện thực tế trong các ca điều trị).
* **Số transaction có $\ge 2$ thuốc**: 31,049 (30.51%).
* **Số transaction có 1 thuốc**: 47,848 (47.02%).
* **Số transaction không có thuốc**: 22,869 (22.47%).
* **Số thuốc trung bình / encounter**: 1.18 thuốc (Tối thiểu: 0, Tối đa: 6 thuốc).
* **Top 5 thuốc xuất hiện nhiều nhất**: `Insulin` (54,383), `Metformin` (20,245), `Glipizide` (12,776), `Glyburide` (10,698), `Pioglitazone` (7,363).
* **Phân phối độ tuổi**: Nhóm `[70-80)` tuổi chiếm cao nhất (25.6%), tiếp theo là `[60-70)` (22.1%) và `[50-60)` (16.9%).
* **Top chẩn đoán**: Bệnh lý tuần hoàn (ICD-9: 390–459), Đái tháo đường (ICD-9: 250.xx), Bệnh lý hô hấp (ICD-9: 460–519).

---

# 10. DATA PREPROCESSING & IDEMPOTENT IMPORTS

Module tiền xử lý được kiến trúc hóa qua các dịch vụ chuyên trách:
* `DatasetImportService`: Quản lý nạp tệp CSV, tính mã băm (checksum) và kiểm tra idempotent: nếu tệp đã nạp, cảnh báo và yêu cầu xác nhận ghi đè thay vì âm thầm nhân đôi dữ liệu.
* `DataPreprocessingService`: Bộ phân tích dữ liệu theo luồng (Streaming CSV Parser qua Apache Commons CSV), loại bỏ bản ghi không hợp lệ, lưu trữ thống kê Data Understanding.
* `TransactionBuilderService`: Gom các thuốc theo `encounter_id`, loại bỏ các encounter có $< 2$ thuốc, lưu batch vào bảng `transactions` và `transaction_items` trong CSDL.
* `TransactionService`: Tự động nạp dữ liệu từ CSDL khi bộ nhớ cache khởi động lại, đảm bảo quá trình khai phá không bị gián đoạn.

### Quy trình tiền xử lý:
$$\text{CSV File} \longrightarrow \text{Validation} \longrightarrow \text{Cleaning} \longrightarrow \text{Medicine Extraction} \longrightarrow \text{Transaction Building} \longrightarrow \text{Database Persistence}$$

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

# 17. ĐÁNH GIÁ MÔ HÌNH (BENCHMARK METHODOLOGY & RESULTS)

Module `MiningEvaluationService` thực hiện so sánh khoa học đối đầu giữa Apriori và FP-Growth:
* **Phương pháp đa lượt đo (Multi-run)**: 2 lượt warm-up để JVM JIT Compiler tối ưu hóa bytecode, sau đó thực hiện 5 lượt đo lường chính thức để tính toán **thời gian chạy trung vị (Median Runtime)**, trung bình (Mean) và độ lệch chuẩn (StdDev).
* **Đo lường bộ nhớ tiêu thụ**: Đo biến thiên xấp xỉ bộ nhớ heap JVM (Approximate JVM Heap Delta).
* **Độ tương đồng Jaccard**: Đánh giá tính tương đương của tập phổ biến và tập luật kết hợp qua công thức đối xứng:
  $$\text{Jaccard Similarity} = \frac{|A \cap B|}{|A \cup B|}$$

### Bảng Kết Quả Đo Lường Thực Tế từ Dataset Đầy Đủ (31,049 Transactions):
*(Quy trình đo lường chuẩn hóa: 2 lượt warm-up + 5 lượt đo lường chính thức lấy thời gian trung vị Median Runtime và độ lệch chuẩn StdDev)*

| Thuật toán | Giao dịch | Support | Confidence | Median Runtime | Approx RAM | Tập phổ biến | Luật kết hợp | Rule Jaccard | Itemset Jaccard |
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

# 18. LỰA CHỌN THUẬT TOÁN & ACTIVE MODEL

Dựa trên kết quả đo lường thực nghiệm khoa học qua nhiều lượt đo lặp, hệ thống chính thức lựa chọn **FP-Growth** làm thuật toán khuyến nghị cốt lõi:
1. **Kiểm chứng tương thích đầu ra**: Cả hai thuật toán đều cho ra kết quả tập mục phổ biến và luật kết hợp tương thích hoàn toàn (Rule Jaccard = 100.0%, Itemset Jaccard = 100.0%).
2. **Thời gian thực thi trung vị**: FP-Growth nhanh hơn Apriori **5.3 lần** ở $\text{minSupport}=0.01$ (46 ms so với 243 ms) và độ ổn định cao hơn (StdDev ±5.2 ms so với ±33.5 ms).
3. **Khả năng mở rộng**: FP-Growth chỉ quét CSDL 2 lần và không sinh hàng nghìn tập ứng viên trung gian $C_k$, tiết kiệm bộ nhớ và tránh nghẽn I/O khi dữ liệu tăng trưởng.
4. **Cơ chế kích hoạt Active Model**: Quản trị viên có thể chỉ định mô hình chạy tối ưu làm Active Model (`selected_for_recommendation = true`) thông qua giao diện `/mining/history` hoặc REST API.

---

# 19. CƠ SỞ DỮ LIỆU (DATABASE SCHEMA)

CSDL MySQL đồng nhất tên **`hospital_drug_mining`** gồm 21 bảng được chuẩn hóa:
* Quản trị: `users` (chứa `role_id`), `roles`.
* Lâm sàng: `patients`, `doctors`, `medical_visits`, `medicines`, `prescriptions`, `prescription_items`.
* Tiền xử lý & Nạp dữ liệu: `dataset_imports`, `dataset_statistics`.
* Giao dịch: `transactions`, `transaction_items`.
* Khai phá dữ liệu: `mining_runs` (gắn khóa ngoại `dataset_import_id` và cờ `selected_for_recommendation`), `frequent_itemsets`, `frequent_itemset_items`, `association_rules`, `association_rule_items`, `association_rule_antecedents`, `association_rule_consequents`.
* Đánh giá & Tương tác: `algorithm_benchmarks`, `drug_interactions`.

> **Thiết kế tiền tố / hệ quả chuẩn hóa**: `association_rule_antecedents` và `association_rule_consequents` lưu từng `medicine_id` độc lập (thay vì chuỗi gộp), giúp tìm kiếm luật bằng Indexing cực nhanh theo thời gian thực khi bác sĩ kê đơn.

---

# 20. KIẾN TRÚC HỆ THỐNG (SYSTEM ARCHITECTURE)

Mô hình phân tầng chuẩn mực:
* **Presentation Layer**: Thymeleaf, HTML5/CSS3, Bootstrap 5, Chart.js tương tác không đồng bộ (AJAX Fetch API).
* **Controller Layer**: Xử lý Web MVC và REST API (`MiningApiController`, `RecommendationApiController`, `GlobalExceptionHandler`).
* **Service Layer**: Tách biệt rõ giữa phân hệ Khai phá Dữ liệu (`com.hospital.datamining.service.mining.*`) và phân hệ Nghiệp vụ Lâm sàng (`com.hospital.datamining.service.prescription.*`).
* **Persistence Layer**: Spring Data JPA & Hibernate kết nối MySQL hoặc H2 in-memory mode.

---

# 21. TÍCH HỢP VÀO CHỨC NĂNG KÊ ĐƠN (PRESCRIPTION INTEGRATION)

1. Bác sĩ mở màn hình khám bệnh (`/prescriptions/create`) và chọn một thuốc khởi đầu (ví dụ: `Metformin`).
2. Giao diện gửi yêu cầu AJAX tới `DrugRecommendationService`.
3. Thuật toán tìm kiếm luật thực hiện 2 tầng dựa trên **Active Model**:
   * **Tầng 1 (Ưu tiên)**: Tìm các luật đa tiền tố (Multi-antecedent) khớp chính xác toàn bộ danh sách thuốc hiện có trong đơn.
   * **Tầng 2 (Fallback)**: Tìm các luật đơn tiền tố (Single-antecedent) tương ứng với từng thuốc đã chọn nếu tầng 1 không có kết quả.
4. Lọc bỏ các thuốc đã có trong đơn thuốc.
5. Sắp xếp thứ bậc ưu tiên: $\text{Lift} \downarrow \implies \text{Confidence} \downarrow \implies \text{Support} \downarrow$.
6. Hiển thị bảng gợi ý trực quan với huy hiệu chỉ số, cảnh báo tương tác thuốc DDI và nút "Thêm vào đơn" (tuyệt đối không tự động thêm thuốc).

---

# 22. CƠ SỞ DỮ LIỆU TƯƠNG TÁC THUỐC MẪU (DEMO DDI KNOWLEDGE BASE)

Hệ thống tích hợp dữ liệu tương tác thuốc minh họa (tham chiếu cơ sở dữ liệu DailyMed) qua bảng `drug_interactions` và `DrugInteractionService`:
* Khi gợi ý một thuốc kết hợp (ví dụ: `{Metformin}` kết hợp `{Insulin}`), hệ thống tự động đối chiếu chéo cặp thuốc này trong cơ sở dữ liệu tương tác.
* Trạng thái tương tác:
  * `KNOWN`: Đã ghi nhận trong cơ sở dữ liệu mẫu.
  * `NOT FOUND`: Chưa ghi nhận cảnh báo tương tác trong danh mục hiện tại.
* Mức độ cảnh báo: `Major`, `Moderate`, `Minor`.
* **Phân tách rạch ròi**:
  * *Khai phá luật kết hợp* $\to$ Cho biết các thuốc thường được kê cùng nhau trong thực tế.
  * *DDI Knowledge Base* $\to$ Cảnh báo tương tác bất lợi tiềm ẩn.
  * Hệ thống tuyệt đối không suy diễn "thường đi cùng nhau là an toàn".

---

# 23. KHUYẾN CÁO MIỄN TRỪ TRÁCH NHIỆM Y KHOA (CLINICAL DISCLAIMER)

Tại giao diện kê đơn thuốc của bác sĩ (`/prescriptions/create`) và panel gợi ý thuốc, hệ thống bắt buộc hiển thị cảnh báo:

> **Gợi ý này dựa trên các mẫu đồng sử dụng thuốc trong dữ liệu lịch sử và chỉ dùng để tham khảo. Hệ thống không tự động xác nhận hiệu quả hoặc độ an toàn của phối hợp thuốc. Quyết định kê đơn cuối cùng thuộc về bác sĩ.**

---

# 24. HƯỚNG DẪN CÀI ĐẶT & CẤU HÌNH (INSTALLATION)

### Yêu cầu môi trường:
* **Java Development Kit (JDK)**: Java 17 hoặc Java 21 LTS.
* **Apache Maven**: Phiên bản 3.8+ (đã tích hợp sẵn `mvn.cmd`).
* **Hệ quản trị CSDL**: MySQL 8.0+ (hoặc chạy trực tiếp với H2 in-memory profile mà không cần cài đặt MySQL).

### Tải bộ dữ liệu UCI chính thức:
```bash
# Trên Linux / macOS / Git Bash:
./scripts/download_dataset.sh

# Hoặc trên Windows PowerShell:
.\scripts\download_dataset.ps1
```

### Khởi tạo cơ sở dữ liệu MySQL:
1. Đăng nhập vào MySQL:
   ```bash
   mysql -u root -p
   ```
2. Tạo CSDL thống nhất:
   ```sql
   CREATE DATABASE IF NOT EXISTS hospital_drug_mining CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   USE hospital_drug_mining;
   ```
3. Nạp cấu trúc bảng và dữ liệu danh mục mẫu:
   ```bash
   mysql -u root -p hospital_drug_mining < database/schema.sql
   mysql -u root -p hospital_drug_mining < database/sample-data.sql
   ```

---

# 25. CẤU HÌNH HỆ THỐNG (CONFIGURATION)

Nội dung cấu hình chính trong `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_drug_mining?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Tắt tự động chạy mining nặng khi startup để tránh làm chậm ứng dụng
app.seed.auto-mining=false

# Đường dẫn bộ dữ liệu UCI
dataset.path=dataset/diabetic_data.csv
```

---

# 26. KHỞI CHẠY & KIỂM THỬ DỰ ÁN (RUN & TEST)

### Bước 1: Chạy toàn bộ Test Suite (H2 in-memory)
```bash
mvn clean test
```
*(Kết quả: 29 unit & integration tests PASS 100%, không yêu cầu MySQL).*

### Bước 2: Đóng gói dự án thành file JAR thực thi
```bash
mvn clean package
```

### Bước 3: Khởi chạy ứng dụng

**Cách 1: Khởi chạy với H2 Database (Không cần MySQL)**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

**Cách 2: Khởi chạy với MySQL**:
```bash
mvn spring-boot:run
```
hoặc:
```bash
java -jar target/hospital-drug-association-mining-1.0.0.jar
```

Truy cập ứng dụng tại trình duyệt: **`http://localhost:8080`**

---

# 27. TÀI KHOẢN TRẢI NGHIỆM DEMO

| Vai trò | Tên đăng nhập | Mật khẩu | Quyền hạn truy cập |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin` | `admin123` | Quản trị Dataset, Tiền xử lý, Chạy Apriori, Chạy FP-Growth, Xem Luật, Chạy Benchmark, Quản lý Active Model, Xem Lịch sử |
| **Bác sĩ lâm sàng (Doctor)** | `doctor` | `doctor123` | Quản lý bệnh nhân, Lập lượt khám, Kê đơn thuốc, Nhận gợi ý luật kết hợp thời gian thực & Cảnh báo DDI |

---

# 28. HẠN CHẾ CỦA ĐỀ TÀI (LIMITATIONS)

1. **Phạm vi đối tượng bệnh nhân**: Bộ dữ liệu UCI Diabetes 130-US tập trung chủ yếu vào bệnh nhân mắc đái tháo đường nằm viện nội trú, do đó các luật khai phá được không đại diện cho tất cả các nhóm bệnh lý khác hoặc mọi mô hình bệnh viện.
2. **Tính đại diện**: Dữ liệu thu thập tại các bệnh viện Mỹ trong giai đoạn 1999–2008, có thể có sự khác biệt so với mô hình bệnh tật và hướng dẫn điều trị hiện hành tại Việt Nam.
3. **Phạm vi thuộc tính**: Bộ dữ liệu chỉ ghi nhận 23 cột thuốc hạ đường huyết cụ thể trong danh mục nghiên cứu.
4. **Bản chất của Luật Kết hợp**: Các luật kết hợp chỉ phản ánh mối tương quan **đồng xuất hiện mang tính thống kê (correlation / co-occurrence)** trong dữ liệu lịch sử.
5. **Không chứng minh quan hệ nhân quả (No Causality)**: Tần suất đồng xuất hiện không giải thích được cơ chế tương tác ở cấp độ sinh học phân tử hay phản ứng dược lý.
6. **Không chứng minh độ an toàn của thuốc (No Clinical Safety Guarantee)**: Các chỉ số Support, Confidence và Lift cao không đồng nghĩa với việc phối hợp thuốc là an toàn hoặc hiệu quả tối ưu cho mọi bệnh nhân.
7. **Không thay thế phác đồ điều trị**: Gợi ý chỉ mang tính hỗ trợ ra quyết định lâm sàng (CDSS), bác sĩ luôn là người chịu trách nhiệm chuyên môn cuối cùng.
8. **Dữ liệu tương tác thuốc (DDI)**: Cần được tích hợp từ các nguồn dữ liệu dược thư quốc gia hoặc API có thẩm định pháp lý rõ ràng nếu đưa vào ứng dụng thực tế trên quy mô lớn.

---

# 29. HƯỚNG PHÁT TRIỂN (FUTURE WORK)

1. **Mở rộng nguồn dữ liệu đa trung tâm**: Tích hợp các tập dữ liệu lâm sàng chuyên sâu khác như MIMIC-IV, eICU Collaborative Research Database để mở rộng phạm vi chẩn đoán đa chuyên khoa.
2. **Làm giàu tri thức Dược lý**: Kết nối API thời gian thực với DrugBank, RxNorm, ATC và cơ sở dữ liệu biến cố có hại FDA FAERS.
3. **Đồ thị tri thức Y tế (DDI Knowledge Graph)**: Xây dựng Knowledge Graph liên kết bệnh lý - hoạt chất - tương tác thuốc để tối ưu hóa hệ thống hỗ trợ ra quyết định lâm sàng.

---

# 30. TÀI LIỆU THAM KHẢO (REFERENCES)

1. **Tatonetti, N. P., Patrick, P. B., Daneshjou, R., & Altman, R. B. (2012)**. *Data-driven prediction of drug effects and interactions*. Science Translational Medicine, 4(125), 125ra31.
2. **Strack, B., DeShazo, J. P., Gennings, C., et al. (2014)**. *Impact of HbA1c Measurement on Hospital Readmission Rates: Analysis of 70,000 Clinical Database Patient Records*. BioMed Research International, 2014, 781670.
3. **Agrawal, R., & Srikant, R. (1994)**. *Fast algorithms for mining association rules in large databases*. Proceedings of the 20th International Conference on Very Large Data Bases (VLDB), 487–499.
4. **Han, J., Pei, J., & Yin, Y. (2000)**. *Mining frequent patterns without candidate generation*. ACM SIGMOD Record, 29(2), 1–12.
5. **UCI Machine Learning Repository**: *Diabetes 130-US Hospitals for Years 1999–2008 Dataset*. https://archive.ics.uci.edu/dataset/296/diabetes+130-us+hospitals+for+years+1999-2008.
