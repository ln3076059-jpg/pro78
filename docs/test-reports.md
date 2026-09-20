# BÁO CÁO KẾT QUẢ KIỂM THỬ TỰ ĐỘNG (AUTOMATED TEST REPORT)

---

## 1. Tóm tắt Kết quả Kiểm thử
* **Thời điểm chạy kiểm thử**: `2026-09-20 14:54:02`
* **Môi trường**: Java 17 (`--release 17` trên JDK 21 LTS runtime), Apache Maven 3.9.11, Spring Boot 3.3.4, JUnit 5, H2 In-memory Database
* **Lệnh thực thi**: `mvn clean test` và `mvn clean package`
* **Tổng số kiểm thử**: **34 test cases** (trong **16 test suites**)
* **Số ca thành công (Pass)**: **34 (100%)**
* **Thất bại (Failures)**: **0**
* **Lỗi ngoại lệ (Errors)**: **0**
* **Bị bỏ qua (Skipped)**: **0**
* **Trạng thái Build**: **`BUILD SUCCESS`**

---

## 2. Danh mục Chi tiết 16 Test Suites (34 Tests)

| # | Test Suite Class | Số test | Trọng tâm kiểm thử | Kết quả |
| :---: | :--- | :---: | :--- | :---: |
| 1 | `AprioriMiningServiceTest` | 3 | Thuật toán Apriori chuẩn T1..T5, xử lý rỗng, và kiểm thử null parameters safety (`NullPointerException`). | **PASS** |
| 2 | `AprioriServiceTest` | 2 | Kiểm thử bổ sung thuật toán Apriori, xác thực cấu trúc tập phổ biến và luật cơ sở. | **PASS** |
| 3 | `FPGrowthMiningServiceTest` | 3 | Thuật toán FP-Growth đối chiếu Apriori, xử lý rỗng, và kiểm thử null parameters safety (`NullPointerException`). | **PASS** |
| 4 | `FPGrowthServiceTest` | 1 | Cấu trúc cây FP-Tree và Header Table không sinh tổ hợp ứng viên $C_k$. | **PASS** |
| 5 | `ExactEquivalenceTest` | 1 | Tính tương đương toán học 100% itemsets, support counts, rules, confidence, lift giữa Apriori và FP-Growth. | **PASS** |
| 6 | `MiningEvaluationServiceTest` | 2 | Benchmark đa lượt đo (2 warm-up + 5 measurement iterations lấy median), Rule Jaccard 100%, Itemset Jaccard 100%, và kiểm tra null inputs safety. | **PASS** |
| 7 | `ActiveMiningRunTest` | 3 | Quản lý Active Model: kích hoạt model, reset model cũ, fallback sang latest run, và từ chối kích hoạt model có trạng thái khác `SUCCESS`. | **PASS** |
| 8 | `AssociationRuleMetricTest` | 2 | Tính toán độc lập chỉ số Support, Confidence, Lift đối chiếu công thức toán học. | **PASS** |
| 9 | `AssociationMetricTest` | 1 | Xác thực các ngưỡng lọc chỉ số thống kê kết hợp. | **PASS** |
| 10 | `TransactionBuilderServiceTest` | 2 | Xây dựng giao dịch: gom thuốc theo đợt nằm viện và lọc giao dịch $\ge 2$ thuốc. | **PASS** |
| 11 | `DatasetImportServiceTest` | 1 | Pipeline import dataset UCI Diabetes, tính toán thống kê và kiểm tra tính toàn vẹn. | **PASS** |
| 12 | `DrugNormalizationServiceTest` | 4 | Chuẩn hóa tên generic thuốc, ánh xạ biệt dược, loại bỏ ký tự rác. | **PASS** |
| 13 | `DrugRecommendationServiceTest` | 3 | Gợi ý kê đơn 2 tầng (Tier 1: Đa tiền đề $\to$ Tier 2: Đơn tiền đề fallback), lọc trùng đơn thuốc, và truy vấn trực tiếp bằng `medicine_id` qua bảng quan hệ `association_rule_antecedents`. | **PASS** |
| 14 | `DrugInteractionServiceTest` | 3 | Tra cứu tương tác thuốc DDI hai chiều (Known interaction, Not found, Multiple interactions). | **PASS** |
| 15 | `BenchmarkReportGeneratorTest` | 1 | Chạy thực nghiệm đo lường đối đầu Apriori vs FP-Growth trên toàn bộ 31,049 transactions của bộ dữ liệu thực tế `diabetic_data.csv`. | **PASS** |
| 16 | `MimicPreprocessingServiceTest` | 2 | Tương thích dữ liệu phụ và chuyển đổi cấu trúc giao dịch. | **PASS** |

---

## 3. Nhật Ký Đầu Ra Thực Tế từ Maven Surefire (Console Output)

```text
[INFO] Scanning for projects...
[INFO] 
[INFO] -----------< com.hospital:hospital-drug-association-mining >------------
[INFO] Building hospital-drug-association-mining 1.0.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- clean:3.3.2:clean (default-clean) @ hospital-drug-association-mining ---
[INFO] Deleting D:\.idea\it61\target
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ hospital-drug-association-mining ---
[INFO] Copying 2 resources from src\main\resources to target\classes
[INFO] Copying 20 resources from src\main\resources to target\classes
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ hospital-drug-association-mining ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 87 source files with javac [debug parameters release 17] to target\classes
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ hospital-drug-association-mining ---
[INFO] skip non existing resourceDirectory D:\.idea\it61\src\test\resources
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ hospital-drug-association-mining ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 16 source files with javac [debug parameters release 17] to target\test-classes
[INFO] 
[INFO] --- surefire:3.2.5:test (default-test) @ hospital-drug-association-mining ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.hospital.datamining.interaction.DrugInteractionServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.736 s
[INFO] Running com.hospital.datamining.mining.ActiveMiningRunTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.832 s
[INFO] Running com.hospital.datamining.mining.AprioriMiningServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.111 s
[INFO] Running com.hospital.datamining.mining.AprioriServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.076 s
[INFO] Running com.hospital.datamining.mining.AssociationMetricTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.035 s
[INFO] Running com.hospital.datamining.mining.AssociationRuleMetricTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.075 s
[INFO] Running com.hospital.datamining.mining.BenchmarkReportGeneratorTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 10.64 s
[INFO] Running com.hospital.datamining.mining.ExactEquivalenceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.040 s
[INFO] Running com.hospital.datamining.mining.FPGrowthMiningServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.056 s
[INFO] Running com.hospital.datamining.mining.FPGrowthServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.038 s
[INFO] Running com.hospital.datamining.mining.MiningEvaluationServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.230 s
[INFO] Running com.hospital.datamining.preprocessing.DatasetImportServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.105 s
[INFO] Running com.hospital.datamining.preprocessing.DrugNormalizationServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.016 s
[INFO] Running com.hospital.datamining.preprocessing.MimicPreprocessingServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.015 s
[INFO] Running com.hospital.datamining.preprocessing.TransactionBuilderServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s
[INFO] Running com.hospital.datamining.recommendation.DrugRecommendationServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.089 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 34, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] --- jar:3.4.2:jar (default-jar) @ hospital-drug-association-mining ---
[INFO] Building jar: D:\.idea\it61\target\hospital-drug-association-mining-1.0.0.jar
[INFO] 
[INFO] --- spring-boot:3.3.4:repackage (repackage) @ hospital-drug-association-mining ---
[INFO] Replacing main artifact D:\.idea\it61\target\hospital-drug-association-mining-1.0.0.jar with repackaged archive, adding nested dependencies in BOOT-INF/.
[INFO] The original artifact has been renamed to D:\.idea\it61\target\hospital-drug-association-mining-1.0.0.jar.original
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  54.529 s
[INFO] Finished at: 2026-09-20T14:54:02+07:00
[INFO] ------------------------------------------------------------------------
```
