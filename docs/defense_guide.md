# HƯỚNG DẪN TRẢ LỜI CÁC CÂU HỎI TRỌNG TÂM KHI BẢO VỆ ĐỒ ÁN DATA MINING

Tài liệu này được biên soạn chi tiết và đầy đủ để phục vụ hội đồng bảo vệ đồ án môn **Nhập môn Khai phá Dữ liệu và Máy học**, với đề tài:
**"Ứng dụng kỹ thuật khai phá luật kết hợp tích hợp vào hệ thống quản lý bệnh viện trên công nghệ Java để giải quyết bài toán kết hợp thuốc điều trị"**.

---

### Câu 1: Dataset lấy từ đâu?
* **Trả lời**: 
  * Bộ dữ liệu lấy từ **UCI Machine Learning Repository** (Đại học California, Irvine), có mã định danh Dataset ID: 296 (*Diabetes 130-US Hospitals for Years 1999–2008*).
  * Tệp dữ liệu chính là `diabetic_data.csv` (dung lượng 19.1 MB, 101,766 dòng) đi kèm tệp ánh xạ định danh `IDs_mapping.csv`.
  * Bộ dữ liệu được tải tự động qua script `scripts/download_dataset.sh` và `scripts/download_dataset.ps1` trực tiếp từ URL nén chính thức của UCI, có cơ chế giải nén và fallback sang mirror GitHub khi cần.

---

### Câu 2: Vì sao UCI đáng tin cậy?
* **Trả lời**:
  * Dữ liệu được trích xuất từ cơ sở dữ liệu Health Facts của tập đoàn y tế lâm sàng Cerner Corporation, thu thập trong 10 năm liên tục (1999–2008) tại **130 bệnh viện và cơ sở y tế tích hợp trên toàn nước Mỹ**.
  * Dữ liệu đã được bình duyệt và công bố trong bài báo khoa học quốc tế uy tín: *Strack, B., DeShazo, J. P., Gennings, C., et al. (2014), "Impact of HbA1c Measurement on Hospital Readmission Rates", BioMed Research International, ID 781670*.
  * Bộ dữ liệu được lưu trữ chuẩn mực trên kho dữ liệu học máy toàn cầu UCI ML Repository và là benchmark chuẩn cho nghiên cứu phân tích y tế lâm sàng. Đồ án tuyệt đối minh bạch nguồn gốc và không giả mạo là bộ dữ liệu khác.

---

### Câu 3: Dataset có bao nhiêu record và cấu trúc các trường thuốc như thế nào?
* **Trả lời**:
  * **101,766 bản ghi** (encounters) đại diện cho 101,766 đợt nằm viện nội trú.
  * **71,518 bệnh nhân duy nhất** (`patient_nbr`).
  * Gồm 50 thuộc tính tổng thể: bao gồm nhân khẩu học (tuổi, giới tính, chủng tộc), quá trình nhập viện/xuất viện, các chỉ số xét nghiệm, chẩn đoán ICD-9, và **23 cột thuốc điều trị đái tháo đường cụ thể** (`metformin`, `repaglinide`, `nateglinide`, `chlorpropamide`, `glimepiride`, `acetohexamide`, `glipizide`, `glyburide`, `tolbutamide`, `pioglitazone`, `rosiglitazone`, `acarbose`, `miglitol`, `troglitazone`, `tolazamide`, `examide`, `citoglipton`, `insulin`, `glyburide-metformin`, `glipizide-metformin`, `glimepiride-pioglitazone`, `metformin-rosiglitazone`, `metformin-pioglitazone`) cùng 2 thuộc tính tóm tắt là `change` và `diabetesMed`.

---

### Câu 4: Vì sao `encounter_id` được xem là một transaction?
* **Trả lời**:
  * Một bệnh nhân (`patient_nbr`) có thể có nhiều lần nhập viện tại các thời điểm khác nhau với diễn tiến bệnh lý và phác đồ thay đổi.
  * Mỗi `encounter_id` biểu diễn **một đợt điều trị lâm sàng trọn vẹn tại bệnh viện**. Các thuốc được chỉ định dùng trong cùng một encounter thể hiện quyết định kết hợp thuốc đồng thời của hội đồng điều trị hoặc bác sĩ điều trị cho ca bệnh đó.
  * Do đó, mô hình hóa mỗi `encounter_id` thành một Transaction chứa danh sách các thuốc active là định nghĩa khoa học và chính xác nhất cho bài toán khai phá đồng kê đơn (co-prescription).

---

### Câu 5: Các giá trị `No`, `Steady`, `Up`, `Down` được xử lý thế nào trong tiền xử lý?
* **Trả lời**:
  * `No`: Thuốc không được chỉ định trong đợt điều trị này $\implies$ **Không đưa thuốc vào Transaction**.
  * `Steady`: Thuốc được chỉ định và duy trì liều ổn định $\implies$ **Đưa thuốc vào Transaction**.
  * `Up`: Thuốc được chỉ định và tăng liều trong đợt điều trị $\implies$ **Đưa thuốc vào Transaction**.
  * `Down`: Thuốc được chỉ định và giảm liều trong đợt điều trị $\implies$ **Đưa thuốc vào Transaction**.
  * Tóm lại: Bất kỳ trạng thái nào trong $\{\text{Steady}, \text{Up}, \text{Down}\}$ đều xác nhận thuốc có mặt trong phác đồ điều trị của encounter đó.

---

### Câu 6: Vì sao lại loại các transaction có $< 2$ thuốc?
* **Trả lời**:
  * **0 loại thuốc (22,869 encounters - 22.47%)**: Bệnh nhân không được kê bất kỳ thuốc hạ đường huyết nào trong danh mục 23 thuốc (chỉ điều trị bằng dinh dưỡng hoặc bệnh lý khác). Giao dịch rỗng không thể khai phá.
  * **1 loại thuốc (47,848 encounters - 47.02%)**: Bệnh nhân sử dụng đơn trị liệu (monotherapy). Một transaction chỉ có đúng 1 item không thể tạo ra bất kỳ luật kết hợp dạng $X \implies Y$ (trong đó $X \cap Y = \emptyset$ và cả $X, Y$ đều khác rỗng).
  * **$\ge 2$ loại thuốc (31,049 encounters - 30.51%)**: Đại diện cho các ca đa trị liệu (polypharmacy). Đây là tập dữ liệu có giá trị và hợp lệ duy nhất để giải quyết bài toán kết hợp thuốc điều trị.

---

### Câu 7: Thuật toán Apriori hoạt động thế nào?
* **Trả lời**:
  * Apriori do Agrawal & Srikant đề xuất năm 1994, hoạt động theo phương pháp **duyệt theo mức (level-wise breadth-first search)**:
    1. Quét CSDL đếm tần suất các item đơn lẻ để tạo $L_1$ (Frequent 1-itemsets).
    2. Với mỗi mức $k \ge 2$, thực hiện phép kết nối (Join step): $L_{k-1} \Join L_{k-1}$ để sinh tập ứng viên $C_k$.
    3. Áp dụng bước cắt tỉa (Prune step): kiểm tra xem mọi tập con kích thước $k-1$ của ứng viên có thuộc $L_{k-1}$ không. Nếu không, loại bỏ ngay.
    4. Quét lại toàn bộ tập transaction để đếm độ hỗ trợ thực tế của các ứng viên trong $C_k$, giữ lại các tập đạt $\ge \text{minSupport}$ để thành lập $L_k$.
    5. Lặp lại cho đến khi không sinh thêm được tập phổ biến nào hoặc đạt `maxItemsetSize`.
    6. Từ các tập phổ biến, sinh các luật kết hợp $X \implies Y$ thỏa mãn $\text{minConfidence}$ và $\text{minLift}$.

---

### Câu 8: Nguyên lý Apriori (Apriori Property / Anti-monotonicity) là gì?
* **Trả lời**:
  * **Phát biểu**: *Mọi tập con của một tập mục phổ biến đều phải là tập mục phổ biến.*
  * **Hệ quả dùng để cắt tỉa**: *Nếu một tập mục không phổ biến (Support < minSupport), thì bất kỳ tập cha mở rộng nào chứa nó chắc chắn cũng không phổ biến.*
  * Nhờ nguyên lý này, thuật toán loại bỏ sớm một không gian tìm kiếm khổng lồ các tổ hợp ứng viên mà không cần kiểm tra trên dữ liệu thực tế.

---

### Câu 9: Thuật toán FP-Growth khác Apriori như thế nào?
* **Trả lời**:
  * **Khắc phục nhược điểm cốt tử của Apriori**: Apriori phải quét CSDL nhiều lần và phát sinh hàng nghìn tập ứng viên trung gian $C_k$, gây nghẽn bộ nhớ và I/O khi dataset lớn hoặc ngưỡng support thấp.
  * **FP-Growth**:
    * **Chỉ quét cơ sở dữ liệu đúng 2 lần**: Lần 1 đếm $L_1$ và sắp xếp các item theo tần suất giảm dần; Lần 2 nén toàn bộ giao dịch vào cây tiền tố **FP-Tree**.
    * **Hoàn toàn không sinh tập ứng viên**: Sử dụng Header Table kết hợp con trỏ liên kết (node links) để định vị nhanh các nút cùng tên trên cây.
    * **Khai phá đệ quy**: Phân chia không gian tìm kiếm bằng cơ sở mẫu điều kiện (Conditional Pattern Base) và xây dựng cây FP-Tree điều kiện (Divide and Conquer).

---

### Câu 10: Độ hỗ trợ (Support) là gì?
* **Trả lời**:
  * Tỷ lệ phần trăm các giao dịch trong toàn bộ cơ sở dữ liệu có chứa đồng thời cả tập tiền đề $X$ và hệ quả $Y$:
    $$\text{Support}(X \implies Y) = \frac{\text{count}(X \cup Y)}{N}$$
  * Phản ánh mức độ phổ biến, đại diện của mẫu kết hợp thuốc trong thực tiễn điều trị. Giúp loại bỏ các mẫu ngẫu nhiên quá hiếm gặp.

---

### Câu 11: Độ tin cậy (Confidence) là gì?
* **Trả lời**:
  * Xác suất có điều kiện chỉ định thêm thuốc $Y$ khi bệnh nhân đã được chỉ định thuốc $X$:
    $$\text{Confidence}(X \implies Y) = \frac{\text{Support}(X \cup Y)}{\text{Support}(X)} = \frac{\text{count}(X \cup Y)}{\text{count}(X)}$$
  * Thể hiện mức độ tin cậy của quy tắc: Trong 100 ca đã dùng phác đồ $X$, có bao nhiêu ca bác sĩ đồng thời kê thêm thuốc $Y$.

---

### Câu 12: Độ nâng (Lift) là gì?
* **Trả lời**:
  * Tỷ số giữa Confidence thực tế so với xác suất xuất hiện kỳ vọng của $Y$ khi độc lập:
    $$\text{Lift}(X \implies Y) = \frac{\text{Confidence}(X \implies Y)}{\text{Support}(Y)} = \frac{\text{Support}(X \cup Y)}{\text{Support}(X) \times \text{Support}(Y)}$$
  * Là thước đo quan trọng nhất để đánh giá mối liên kết giữa $X$ và $Y$ có thực sự vượt qua mức ngẫu nhiên độc lập hay không.

---

### Câu 13: `Lift > 1` có ý nghĩa gì?
* **Trả lời**:
  * $\text{Lift} > 1$ chứng tỏ hai tập thuốc $X$ và $Y$ xuất hiện cùng nhau **nhiều hơn đáng kể so với kỳ vọng xác suất độc lập**. Mối quan hệ kết hợp mang tính tương quan dương tính mạnh mẽ trong thực hành lâm sàng.
  * Nếu $\text{Lift} = 1$: $X$ và $Y$ hoàn toàn độc lập (xuất hiện cùng nhau chỉ là ngẫu nhiên).
  * Nếu $\text{Lift} < 1$: $X$ và $Y$ ít xuất hiện cùng nhau hơn ngẫu nhiên (có thể do thay thế nhau hoặc đối kháng).

---

### Câu 14: Vì sao không thể nói `Lift > 1` đồng nghĩa với phối hợp thuốc an toàn?
* **Trả lời**:
  * **Bản chất toán học**: Association Rule Mining chỉ là phương pháp **phân tích tương quan thống kê đồng xuất hiện (statistical correlation / co-occurrence)** trong dữ liệu quá khứ.
  * **Không chứng minh nhân quả (No causality)**: Dữ liệu không giải thích cơ chế sinh học phân tử hay phản ứng dược động học/dược lực học.
  * **Không chứng minh độ an toàn (No clinical safety)**: Trong điều trị lâm sàng, nhiều trường hợp bệnh nhân nặng có biến chứng phức tạp, bác sĩ buộc phải kết hợp 2 thuốc có tương tác hoặc nguy cơ hạ đường huyết cao dưới sự giám sát đặc biệt. Việc chúng xuất hiện cùng nhau không có nghĩa là an toàn tuyệt đối cho mọi người bệnh.
  * Vì vậy, hệ thống luôn bắt buộc đi kèm cảnh báo miễn trừ trách nhiệm y khoa và kiểm tra chéo cơ sở dữ liệu tương tác thuốc DDI.

---

### Câu 15: Vì sao phải benchmark nhiều lần (multi-run benchmark)?
* **Trả lời**:
  * Nền tảng Java chạy trên máy ảo **Java Virtual Machine (JVM)**, chịu sự chi phối mạnh mẽ của:
    1. **JIT Compiler (Just-In-Time)**: Các lần chạy đầu tiên mã nguồn được biên dịch và tối ưu hóa dần (C1/C2 compiler), dẫn đến lần đầu luôn chậm hơn bình thường.
    2. **Garbage Collector (GC)**: Chu kỳ thu gom rác có thể kích hoạt ngẫu nhiên làm gián đoạn luồng thực thi (Stop-The-World pause).
    3. **Lập lịch của Hệ điều hành & CPU Throttling**: Tải nền của OS tạo ra dao động đo lường.
  * Do đó, chạy 1 lần duy nhất rồi kết luận là thiếu chuẩn xác khoa học. Hệ thống thực hiện chuẩn mực: **2 lượt warm-up** để JIT ổn định, sau đó chạy **5 lượt đo lường chính thức** để tính toán thời gian và độ phân tán.

---

### Câu 16: Dùng giá trị trung vị (Median Runtime) để làm gì?
* **Trả lời**:
  * Trong thống kê thực nghiệm máy tính, phân phối thời gian thực thi thường bị lệch phải bởi các giá trị ngoại lai (outliers) do GC pause hoặc xung đột tài nguyên OS.
  * **Giá trị trung vị (Median)** có tính chất bền vững (robust statistic), không bị bóp méo bởi các điểm cực trị như giá trị trung bình cộng (Mean).
  * Sử dụng Median đảm bảo so sánh thời gian thực thi giữa Apriori và FP-Growth là khách quan, ổn định và phản ánh đúng bản chất hiệu năng thuật toán.

---

### Câu 17: Vì sao FP-Growth được lựa chọn làm mô hình khuyến nghị?
* **Trả lời**:
  * **Tương thích kết quả 100%**: Trên tập dữ liệu thực tế 31,049 transactions, cả Apriori và FP-Growth cho ra tập mục phổ biến và tập luật kết hợp trùng khớp hoàn toàn (Độ tương đồng Jaccard = 100.0%, các chỉ số Support, Confidence, Lift giống hệt nhau).
  * **Tốc độ vượt trội**: Ở ngưỡng $\text{minSupport}=0.01$, FP-Growth đạt thời gian trung vị **~83 ms**, nhanh hơn **3.88 lần** so với Apriori (~322 ms).
  * **Khả năng mở rộng (Scalability)**: FP-Growth chỉ duyệt dữ liệu 2 lần và nén thông tin trong cây FP-Tree, không làm bùng nổ tổ hợp ứng viên $C_k$, giúp tối ưu RAM và an toàn cho hệ thống bệnh viện khi dữ liệu mở rộng quy mô lớn.

---

### Câu 18: Association Rule được tích hợp vào chức năng nào của hệ thống bệnh viện?
* **Trả lời**:
  * Tích hợp trực tiếp vào **Phân hệ Kê đơn thuốc lâm sàng của Bác sĩ** (`/prescriptions/create`).
  * Khi bác sĩ tiến hành thăm khám và kê một thuốc khởi đầu (ví dụ: `Metformin`), hệ thống tự động kích hoạt API gợi ý thuốc phối hợp trong thời gian thực.
  * Bác sĩ nhìn thấy danh sách thuốc gợi ý kèm các chỉ số Support, Confidence, Lift, cảnh báo tương tác thuốc DDI và có quyền chủ động bấm **"Thêm vào đơn"** hoặc **"Bỏ qua"**. Tuyệt đối không tự động chèn thuốc vào đơn.

---

### Câu 19: Thuật toán gợi ý thuốc cho bác sĩ (Doctor Recommendation) hoạt động chi tiết ra sao?
* **Trả lời**:
  * Hoạt động theo quy trình 5 bước được tối ưu hóa:
    1. **Xác định Active Mining Model**: Truy vấn mô hình khai phá đang được kích hoạt (`selected_for_recommendation = true`) của lần nạp dữ liệu gần nhất.
    2. **Tìm kiếm luật qua bảng chuẩn hóa (Normalized Table Query)**:
       * *Tầng 1 (Multi-item Antecedent)*: Tìm các luật có tập tiền đề chứa chính xác các thuốc bác sĩ đã chọn trong đơn.
       * *Tầng 2 (Single-item Fallback)*: Nếu không có luật đa tiền tố, tìm các luật đơn tiền tố cho từng thuốc lẻ đã chọn.
    3. **Lọc dữ liệu**: Loại bỏ các thuốc đã có mặt trong đơn thuốc của bệnh nhân để tránh kê trùng.
    4. **Xếp hạng (Ranking)**: Sắp xếp theo thứ tự ưu tiên: $\text{Lift} \downarrow \implies \text{Confidence} \downarrow \implies \text{Support} \downarrow$.
    5. **Kiểm tra chéo tương tác thuốc (DDI Check)**: Tra cứu bảng `drug_interactions` để gán nhãn mức độ tương tác và hiển thị cảnh báo lâm sàng.

---

### Câu 20: Bộ dữ liệu UCI Diabetes có những hạn chế gì?
* **Trả lời**:
  * Báo cáo đồ án ghi nhận rõ ràng 8 hạn chế mang tính phương pháp luận:
    1. Chỉ tập trung vào bệnh nhân mắc đái tháo đường nội trú, không đại diện cho mọi khoa khám bệnh hoặc toàn bộ dân số.
    2. Dữ liệu chỉ thu thập tại 130 bệnh viện Mỹ từ năm 1999–2008, phác đồ có thể có khác biệt so với hướng dẫn điều trị hiện hành của Bộ Y tế Việt Nam.
    3. Chỉ có 23 trường hoạt chất hạ đường huyết cụ thể được ghi nhận trong phạm vi nghiên cứu.
    4. Luật kết hợp chỉ phản ánh tương quan đồng xuất hiện thống kê (co-occurrence), không chứng minh quan hệ nhân quả (causality).
    5. Chỉ số Lift và Confidence cao không đồng nghĩa với việc phối hợp thuốc là an toàn hay hiệu quả dược lý.
    6. Dữ liệu không ghi nhận liều lượng chi tiết theo miligram, dạng bào chế hay giờ dùng thuốc cụ thể trong ngày.
    7. Không thay thế được hướng dẫn điều trị chuẩn (clinical practice guidelines) và chuyên môn của bác sĩ.
    8. Dữ liệu tương tác thuốc đi kèm chỉ mang tính chất cơ sở dữ liệu mẫu minh họa (Demo Knowledge Base), cần tích hợp API dược thư chính thức khi đưa vào vận hành thực tế.

---

### Câu 21: Bài báo Tatonetti et al. (2012) liên quan thế nào đến đề tài?
* **Trả lời**:
  * Bài báo nổi tiếng của Nicholas P. Tatonetti và cộng sự trên tạp chí *Science Translational Medicine* (2012) mang tên *"Data-driven prediction of drug effects and interactions"* đã chứng minh: Việc khai thác dữ liệu quan sát điều trị thực tế (Real-World Observational Data / EHR) ở quy mô lớn có thể phát hiện các tín hiệu sử dụng thuốc và tương tác thuốc chưa từng được báo cáo trước đó.
  * Công trình này là **nguồn cảm hứng và cơ sở khoa học định hướng** cho đồ án về tiềm năng ứng dụng Data Mining trong y tế.
  * Đồ án sử dụng kỹ thuật Association Rule Mining (Apriori & FP-Growth) để phát hiện mẫu phối hợp thuốc thực tế, kế thừa tư tưởng khai phá dữ liệu của Tatonetti mà không tự nhận là tái hiện nguyên văn mô hình toán học dược cảnh giác của tác giả.

---

### Câu 22: Quy trình CRISP-DM được áp dụng ra sao trong toàn bộ đồ án?
* **Trả lời**:
  * Đồ án thể hiện trọn vẹn 6 pha chuẩn mực của quy trình quốc tế **CRISP-DM**:
    1. **Business Understanding**: Xác định bài toán giảm thiểu sai sót và hỗ trợ bác sĩ kê đơn phối hợp thuốc cho bệnh nhân đái tháo đường.
    2. **Data Understanding**: Phân tích thống kê 101,766 bản ghi, độ tuổi, các nhóm chẩn đoán ICD-9 và tần suất các thuốc trên Dashboard.
    3. **Data Preparation**: Tiền xử lý theo luồng (Streaming CSV), mã hóa trạng thái thuốc, lọc bỏ bản ghi thiếu dữ liệu, lọc 31,049 giao dịch $\ge 2$ thuốc và lưu trữ vào CSDL MySQL.
    4. **Modeling**: Triển khai thuần Java hai thuật toán Apriori và FP-Growth với tham số đầu vào chặt chẽ.
    5. **Evaluation**: So sánh khách quan bằng Support, Confidence, Lift, bộ nhớ xấp xỉ, thời gian trung vị 5 lần đo và chỉ số tương đồng Jaccard.
    6. **Deployment**: Tích hợp mô hình vào giao diện kê đơn lâm sàng của Bác sĩ qua Web UI, cung cấp gợi ý thuốc thời gian thực kèm cảnh báo DDI và khuyến cáo trách nhiệm y khoa.
