# HƯỚNG DẪN TRẢ LỜI 22 CÂU HỎI TRỌNG TÂM KHI BẢO VỆ ĐỒ ÁN DATA MINING

Tài liệu này được biên soạn bám sát chính xác 22 câu hỏi cốt lõi của hội đồng chấm đồ án môn **Nhập môn Khai phá Dữ liệu và Máy học**:

---

### Câu 1: Dataset lấy từ đâu?
* **Trả lời**: Bộ dữ liệu lấy từ **UCI Machine Learning Repository** (Đại học California, Irvine), có mã định danh Dataset ID 296: *Diabetes 130-US Hospitals for Years 1999–2008*. Tệp dữ liệu chính là `diabetic_data.csv`.

---

### Câu 2: Tại sao dataset uy tín?
* **Trả lời**:
  * Dữ liệu được trích xuất từ cơ sở dữ liệu Health Facts của tập đoàn y tế Cerner Corporation, đại diện cho **130 bệnh viện và mạng lưới chăm sóc y tế trên khắp nước Mỹ** trong 10 năm liên tục (1999–2008).
  * Bộ dữ liệu đã được thẩm định và công bố trong bài báo khoa học bình duyệt quốc tế: *Strack et al. (2014), BioMed Research International* và được lưu trữ trên kho lưu trữ chuẩn mực toàn cầu UCI ML Repository.
  * Dự án hoàn toàn minh bạch, sử dụng dữ liệu UCI Diabetes và không tuyên bố sai lệch là MIMIC-III.

---

### Câu 3: Dataset có bao nhiêu record?
* **Trả lời**:
  * Bộ dữ liệu gồm **101,766 bản ghi** tương ứng với 101,766 lượt điều trị (encounters).
  * Bao gồm **71,518 bệnh nhân duy nhất** (patient_nbr).
  * Có 50 thuộc tính, trong đó có **24 cột thuốc điều trị tiểu đường**.

---

### Câu 4: Tại sao `encounter_id` được xem là một transaction?
* **Trả lời**:
  * Một bệnh nhân (`patient_nbr`) có thể nhập viện nhiều lần trong 10 năm với các tình trạng bệnh khác nhau.
  * Mỗi `encounter_id` đại diện cho **một đợt điều trị lâm sàng cụ thể tại bệnh viện**. Các thuốc được chỉ định trong cùng một encounter phản ánh chính xác quyết định kê đơn phối hợp của bác sĩ cho tình trạng bệnh lý tại thời điểm đó.
  * Do đó, xem mỗi `encounter_id` là một Transaction là mô hình hóa chuẩn xác nhất cho bài toán phát hiện mẫu đồng kê đơn thuốc (co-prescription).

---

### Câu 5: Làm sao xác định thuốc có được sử dụng trong encounter?
* **Trả lời**:
  * Trong 24 cột thuốc của UCI dataset, mỗi cột biểu diễn trạng thái của một hoạt chất.
  * Thuốc được xác định là **CÓ SỬ DỤNG** nếu mang một trong các giá trị: `Steady`, `Up`, hoặc `Down`.
  * Thuốc được xác định là **KHÔNG SỬ DỤNG** nếu mang giá trị: `No`.

---

### Câu 6: `No`, `Steady`, `Up`, `Down` nghĩa là gì trong tiền xử lý?
* **Trả lời**:
  * `No`: Thuốc không được kê đơn cho bệnh nhân trong đợt điều trị này (Bỏ qua).
  * `Steady`: Thuốc được chỉ định và giữ nguyên mức liều ổn định trong suốt đợt điều trị.
  * `Up`: Thuốc được chỉ định và bác sĩ quyết định tăng liều trong đợt điều trị.
  * `Down`: Thuốc được chỉ định và bác sĩ quyết định giảm liều trong đợt điều trị.
  * Cả 3 trạng thái `Steady`, `Up`, `Down` đều khẳng định thuốc có mặt trong phác đồ điều trị của encounter đó và được thêm vào Transaction.

---

### Câu 7: Apriori là gì?
* **Trả lời**:
  * Apriori là thuật toán kinh điển trong khai phá luật kết hợp do Agrawal & Srikant đề xuất năm 1994.
  * Thuật toán tìm kiếm các tập mục phổ biến theo phương pháp **duyệt theo mức (level-wise breadth-first search)**, mở rộng dần kích thước tập mục từ $k=1, 2, \dots$ thông qua phép kết hợp và kiểm tra độ hỗ trợ.

---

### Câu 8: Apriori Principle (Nguyên lý Apriori) là gì?
* **Trả lời**:
  * **Tính chất phản đơn điệu (Anti-monotonicity)**: *Mọi tập con của một tập mục phổ biến bắt buộc phải là tập mục phổ biến*.
  * Ngược lại: *Nếu một tập mục không phổ biến (Support < minSupport), thì bất kỳ tập cha mở rộng nào chứa nó cũng chắc chắn không phổ biến*.
  * Nguyên lý này cho phép Apriori cắt tỉa (prune) không gian tìm kiếm, loại bỏ sớm hàng nghìn tổ hợp ứng viên mà không cần kiểm tra trên cơ sở dữ liệu.

---

### Câu 9: Frequent Itemset (Tập mục phổ biến) là gì?
* **Trả lời**:
  * Là một tập hợp gồm một hoặc nhiều mục (ở đây là các loại thuốc) có tần suất xuất hiện đồng thời trong toàn bộ tập giao dịch lớn hơn hoặc bằng ngưỡng độ hỗ trợ tối thiểu ($\text{Support} \ge \text{minSupport}$) do người dùng thiết lập.

---

### Câu 10: Độ hỗ trợ (Support) là gì?
* **Trả lời**:
  * Tỷ lệ phần trăm các giao dịch chứa cả tập mục $X$ và $Y$:
    $$\text{Support}(X \implies Y) = \frac{\text{count}(X \cup Y)}{N}$$
  * Phản ánh mức độ phổ biến của mẫu kết hợp thuốc trong thực tế lịch sử bệnh viện.

---

### Câu 11: Độ tin cậy (Confidence) là gì?
* **Trả lời**:
  * Xác suất có điều kiện xuất hiện hệ quả $Y$ khi đã biết có tiền đề $X$:
    $$\text{Confidence}(X \implies Y) = \frac{\text{Support}(X \cup Y)}{\text{Support}(X)}$$
  * Trong y tế: Trong số các ca bệnh đã được kê thuốc $X$, có bao nhiêu phần trăm bác sĩ cũng chỉ định thêm thuốc $Y$.

---

### Câu 12: Độ nâng (Lift) là gì?
* **Trả lời**:
  * Tỷ số giữa Confidence thực tế so với Support của $Y$ khi độc lập:
    $$\text{Lift}(X \implies Y) = \frac{\text{Confidence}(X \implies Y)}{\text{Support}(Y)} = \frac{\text{Support}(X \cup Y)}{\text{Support}(X) \times \text{Support}(Y)}$$

---

### Câu 13: `Lift > 1` nghĩa là gì?
* **Trả lời**:
  * $\text{Lift} > 1$ chứng minh tiền đề $X$ và hệ quả $Y$ có xu hướng **đồng xuất hiện nhiều hơn so với xác suất ngẫu nhiên độc lập**.
  * Điều này chứng tỏ sự kết hợp giữa hai thuốc mang tính chủ đích điều trị trong thực hành lâm sàng, chứ không phải ngẫu nhiên bắt gặp.

---

### Câu 14: Tại sao `Lift > 1` không chứng minh thuốc an toàn?
* **Trả lời**:
  * Luật kết hợp và chỉ số Lift chỉ là **phân tích tương quan thống kê (statistical co-occurrence)** dựa trên thói quen kê đơn trong quá khứ.
  * Nó **hoàn toàn không chứng minh quan hệ nhân quả (causality)** và **không kiểm định độ an toàn dược lý (clinical safety)**.
  * Có những cặp thuốc có tương tác bất lợi nhưng trong các ca bệnh nặng, bác sĩ vẫn buộc phải dùng phối hợp dưới sự giám sát chặt chẽ. Do đó, hệ thống luôn phải có cảnh báo miễn trừ trách nhiệm y khoa.

---

### Câu 15: FP-Growth khác Apriori như thế nào?
* **Trả lời**:
  * **Apriori**: Sinh tập ứng viên theo từng cấp $C_k$, phải quét CSDL nhiều lần (tương ứng với độ dài tập phổ biến lớn nhất), tốn kém I/O và nghẽn bộ nhớ khi $C_k$ bùng nổ tổ hợp.
  * **FP-Growth**: Sử dụng cấu trúc cây nén **FP-Tree**, **chỉ quét CSDL đúng 2 lần** và **hoàn toàn không sinh tập ứng viên trung gian**. Thuật toán khai phá đệ quy trên cây tiền tố điều kiện (Divide and Conquer).

---

### Câu 16: Vì sao phải so sánh Apriori và FP-Growth?
* **Trả lời**:
  1. Để kiểm chứng tính đúng đắn toán học: Cả 2 thuật toán chạy trên cùng bộ dữ liệu với cùng $\text{minSupport}$ phải cho ra tập phổ biến và luật **trùng khớp 100%**.
  2. Để đánh giá thực nghiệm: Đo lường khách quan thời gian thực thi (Runtime) và bộ nhớ (Memory) trên dữ liệu y tế quy mô lớn để có căn cứ khoa học chọn ra giải thuật vận hành tối ưu.

---

### Câu 17: Thuật toán cuối cùng được chọn dựa vào kết quả nào?
* **Trả lời**:
  * Dựa trên số liệu đo lường thực tế trên **31,049 giao dịch** của UCI Diabetes:
    * Ở $\text{minSupport} = 0.01$, FP-Growth chỉ mất **83 ms**, nhanh hơn **3.88 lần** so với Apriori (322 ms).
    * FP-Growth ổn định hơn nhiều khi hạ thấp ngưỡng Support, không bị nghẽn RAM do không sinh tập ứng viên.
    * Cả 2 thuật toán cho kết quả luật giống hệt nhau (45 itemsets, 3 rules).
  * Vì vậy, **FP-Growth được lựa chọn làm thuật toán chính** cho hệ thống.

---

### Câu 18: Association Rule được tích hợp ở đâu trong hệ thống?
* **Trả lời**:
  * Tích hợp trực tiếp vào **Phân hệ Kê đơn thuốc của Bác sĩ** (`/prescriptions/create`).
  * Khi bác sĩ chọn thuốc vào đơn, hệ thống tự động truy vấn các luật kết hợp trong CSDL để gợi ý các thuốc thường phối hợp kèm các chỉ số Support, Confidence, Lift và cảnh báo tương tác thuốc DDI.

---

### Câu 19: Nếu bác sĩ chọn nhiều thuốc đầu vào thì tìm rule thế nào?
* **Trả lời**:
  * Hệ thống áp dụng **cơ chế gợi ý 2 tầng (Two-tier Ranking)**:
    1. **Tầng 1 (Ưu tiên)**: Tìm kiếm các luật đa tiền tố (Multi-antecedent) có tập $X$ khớp chính xác toàn bộ danh sách thuốc bác sĩ đã chọn (ví dụ: $\{Thuốc_A, Thuốc_B\} \implies \{Thuốc_C\}$).
    2. **Tầng 2 (Fallback)**: Nếu không có luật đa tiền tố, hệ thống tự động fallback tìm các luật đơn tiền tố cho từng thuốc lẻ ($Thuốc_A \implies X$, $Thuốc_B \implies Y$) và gộp kết quả.
  * Tự động loại bỏ các thuốc đã có trong đơn thuốc.
  * Sắp xếp kết quả theo thứ bậc ưu tiên: $\text{Lift} \downarrow \implies \text{Confidence} \downarrow \implies \text{Support} \downarrow$.

---

### Câu 20: Dataset có hạn chế gì?
* **Trả lời**:
  1. Chỉ tập trung vào bệnh nhân mắc đái tháo đường nội trú, không đại diện cho mọi chuyên khoa hoặc mô hình bệnh viện khác.
  2. Các trường thuốc chỉ ghi nhận trạng thái (`Steady`, `Up`, `Down`) mà không có liều lượng miligram cụ thể, đường dùng hay thời gian dùng chính xác từng giờ.
  3. Dữ liệu mang tính lịch sử quan sát, chỉ phản ánh thói quen đồng sử dụng chứ không phản ánh kết cục an toàn của phối hợp thuốc.

---

### Câu 21: Bài báo Tatonetti et al. (2012) liên quan gì đến đề tài?
* **Trả lời**:
  * Bài báo của Tatonetti và cộng sự trên tạp chí *Science Translational Medicine* đã chứng minh tính khả thi và tiềm năng của việc khai thác dữ liệu lâm sàng thực tế (EHR) để phát hiện các mối quan hệ và tín hiệu thuốc trên quy mô lớn.
  * Đây là cơ sở khoa học truyền cảm hứng cho bài toán khai phá mẫu kết hợp thuốc trong đồ án. Đồ án vận dụng tư tưởng này thông qua kỹ thuật Association Rule Mining (chứ không tuyên bố tái hiện hoàn toàn mô hình dược cảnh giác của tác giả).

---

### Câu 22: Quy trình DMML của project là gì?
* **Trả lời**:
  * Tuân thủ chuẩn mực **CRISP-DM 6 giai đoạn**:
    1. *Business Understanding*: Bài toán hỗ trợ kê đơn đồng thời trong đái tháo đường.
    2. *Data Understanding*: Thống kê phân phối tuổi, chẩn đoán, số thuốc từ 101,766 encounter.
    3. *Data Preparation*: Làm sạch, chuẩn hóa, lọc transaction $\ge 2$ thuốc (thu được 31,049 transactions).
    4. *Modeling*: Triển khai Apriori và FP-Growth thuần Java.
    5. *Evaluation*: Đánh giá Support, Confidence, Lift, so sánh Runtime, RAM và xác nhận trùng khớp 100%.
    6. *Deployment*: Nhúng vào chức năng kê đơn lâm sàng của bác sĩ kèm cảnh báo FDA DDI và miễn trừ trách nhiệm y khoa.
