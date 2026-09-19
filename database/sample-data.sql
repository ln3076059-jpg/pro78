-- =============================================================================
-- DỮ LIỆU MẪU KHỞI TẠO HỆ THỐNG - SAMPLE DATA
-- Bao gồm: Roles, Users, Doctors, Patients, Diagnoses, UCI Diabetes Medicines & FDA DDI
-- File: database/sample-data.sql
-- =============================================================================

USE hospital_mining_db;

-- 1. Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Quản trị viên hệ thống & Kỹ sư Data Mining'),
(2, 'ROLE_DOCTOR', 'Bác sĩ điều trị & Kê đơn thuốc')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Users (Mật khẩu mặc định: admin123 và doctor123 - mã hóa BCrypt)
-- Hash của 'admin123': $2a$10$slYQmyNdxq31q8O6WstBLe8hD1P/Q9sWpXJ2jG7j8s6lK7o/1jUaK
-- Hash của 'doctor123': $2a$10$wNqvO9uWJgqFm011WsmzmeF6nL2W2m/t2Wj8Z7wFq9p3a4y5o2k6m
INSERT INTO users (id, username, password, full_name, email, phone, role_id, enabled) VALUES
(1, 'admin', '$2a$10$slYQmyNdxq31q8O6WstBLe8hD1P/Q9sWpXJ2jG7j8s6lK7o/1jUaK', 'Quản Trị Viên Hệ Thống', 'admin@hospital.vn', '0901234567', 1, TRUE),
(2, 'doctor', '$2a$10$wNqvO9uWJgqFm011WsmzmeF6nL2W2m/t2Wj8Z7wFq9p3a4y5o2k6m', 'BS.CKII Nguyễn Văn Tuấn', 'tuan.nv@hospital.vn', '0912345678', 2, TRUE),
(3, 'doctor2', '$2a$10$wNqvO9uWJgqFm011WsmzmeF6nL2W2m/t2Wj8Z7wFq9p3a4y5o2k6m', 'ThS.BS Trần Thị Mai', 'mai.tt@hospital.vn', '0987654321', 2, TRUE)
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 3. Doctors
INSERT INTO doctors (id, user_id, doctor_code, full_name, specialty, department, phone, email) VALUES
(1, 2, 'DOC001', 'BS.CKII Nguyễn Văn Tuấn', 'Nội Tiết - Đái Tháo Đường', 'Khoa Nội Tiết', '0912345678', 'tuan.nv@hospital.vn'),
(2, 3, 'DOC002', 'ThS.BS Trần Thị Mai', 'Tim Mạch & Chuyển Hóa', 'Khoa Nội Tổng Hợp', '0987654321', 'mai.tt@hospital.vn')
ON DUPLICATE KEY UPDATE doctor_code=VALUES(doctor_code);

-- 4. Diagnoses (ICD-9 & ICD-10 liên quan đến Đái tháo đường & Tim mạch)
INSERT INTO diagnoses (id, icd_code, disease_name, category, description) VALUES
(1, '250', 'Đái tháo đường không biến chứng (Diabetes mellitus without mention of complication)', 'Bệnh nội tiết & chuyển hóa', 'ICD-9-CM nhóm 250'),
(2, '250.01', 'Đái tháo đường type 1 không biến chứng (Type 1 diabetes)', 'Bệnh nội tiết & chuyển hóa', 'Phụ thuộc insulin'),
(3, '250.02', 'Đái tháo đường type 2 mất bù (Type 2 diabetes, uncontrolled)', 'Bệnh nội tiết & chuyển hóa', 'Đường huyết dao động mạnh'),
(4, '401.9', 'Tăng huyết áp vô căn (Essential hypertension, unspecified)', 'Bệnh hệ tuần hoàn', 'Bệnh lý tim mạch kèm theo phổ biến ở bệnh nhân tiểu đường'),
(5, '428.0', 'Suy tim sung huyết (Congestive heart failure)', 'Bệnh hệ tuần hoàn', 'Suy giảm chức năng tâm thu/tâm trương'),
(6, '272.0', 'Tăng cholesterol máu thuần túy (Pure hypercholesterolemia)', 'Bệnh nội tiết & chuyển hóa', 'Rối loạn lipid máu')
ON DUPLICATE KEY UPDATE icd_code=VALUES(icd_code);

-- 5. Medicines (Medicine Master - Toàn bộ 24 nhóm thuốc UCI Diabetes 130-US Hospitals + Thuốc đồng kê đơn)
INSERT INTO medicines (id, drug_code, generic_name, display_name, brand_name, dosage_form, strength, route, active, description) VALUES
(1, 'METFORMIN', 'Metformin', 'Metformin HCl', 'Glucophage', 'Viên nén', '500mg / 850mg', 'Uống', TRUE, 'Thuốc hạ đường huyết nhóm Biguanide hàng đầu cho ĐTĐ type 2'),
(2, 'INSULIN', 'Insulin', 'Insulin Người Sinh Học', 'Humulin / Novolin', 'Dung dịch tiêm', '100 IU/ml', 'Tiêm dưới da', TRUE, 'Hormone kiểm soát đường huyết cho ĐTĐ type 1 và type 2 tiến triển'),
(3, 'GLIPIZIDE', 'Glipizide', 'Glipizide', 'Glucotrol', 'Viên giải phóng kéo dài', '5mg / 10mg', 'Uống', TRUE, 'Thuốc kích thích tiết insulin nhóm Sulfonylurea thế hệ 2'),
(4, 'GLYBURIDE', 'Glyburide', 'Glyburide (Glibenclamide)', 'Diabeta / Micronase', 'Viên nén', '2.5mg / 5mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 2 hạ đường huyết mạnh'),
(5, 'PIOGLITAZONE', 'Pioglitazone', 'Pioglitazone HCl', 'Actos', 'Viên nén', '15mg / 30mg', 'Uống', TRUE, 'Thuốc tăng nhạy cảm insulin nhóm Thiazolidinedione (TZD)'),
(6, 'ROSIGLITAZONE', 'Rosiglitazone', 'Rosiglitazone Maleate', 'Avandia', 'Viên nén bao phim', '2mg / 4mg', 'Uống', TRUE, 'Nhóm Thiazolidinedione (TZD) cải thiện nhạy cảm insulin'),
(7, 'GLIMEPIRIDE', 'Glimepiride', 'Glimepiride', 'Amaryl', 'Viên nén', '2mg / 4mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 3 kiểm soát đường huyết 24h'),
(8, 'REPAGLINIDE', 'Repaglinide', 'Repaglinide', 'Prandin', 'Viên nén', '0.5mg / 1mg', 'Uống', TRUE, 'Nhóm Meglitinide kích thích tiết insulin nhanh sau ăn'),
(9, 'NATEGLINIDE', 'Nateglinide', 'Nateglinide', 'Starlix', 'Viên nén', '60mg / 120mg', 'Uống', TRUE, 'Dẫn xuất phenylalanine kích thích bài tiết insulin sớm'),
(10, 'ACARBOSE', 'Acarbose', 'Acarbose', 'Precose / Glucobay', 'Viên nén', '50mg / 100mg', 'Uống', TRUE, 'Ức chế men alpha-glucosidase làm chậm hấp thu carbohydrate'),
(11, 'MIGLITOL', 'Miglitol', 'Miglitol', 'Glyset', 'Viên nén', '25mg / 50mg', 'Uống', TRUE, 'Ức chế alpha-glucosidase đường ruột'),
(12, 'CHLORPROPAMIDE', 'Chlorpropamide', 'Chlorpropamide', 'Diabinese', 'Viên nén', '100mg / 250mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 1 thời gian bán thải dài'),
(13, 'TOLBUTAMIDE', 'Tolbutamide', 'Tolbutamide', 'Orinase', 'Viên nén', '500mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 1 tác dụng ngắn'),
(14, 'TOLAZAMIDE', 'Tolazamide', 'Tolazamide', 'Tolinase', 'Viên nén', '100mg / 250mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 1'),
(15, 'ACETOHEXAMIDE', 'Acetohexamide', 'Acetohexamide', 'Dymelor', 'Viên nén', '250mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 1'),
(16, 'TROGLITAZONE', 'Troglitazone', 'Troglitazone', 'Rezulin', 'Viên nén', '200mg', 'Uống', TRUE, 'Nhóm TZD lịch sử'),
(17, 'GLYBURIDE-METFORMIN', 'Glyburide-Metformin', 'Glyburide + Metformin', 'Glucovance', 'Viên nén bao phim', '2.5mg/500mg', 'Uống', TRUE, 'Dạng phối hợp cố định liều giữa Sulfonylurea và Biguanide'),
(18, 'GLIPIZIDE-METFORMIN', 'Glipizide-Metformin', 'Glipizide + Metformin', 'Metaglip', 'Viên nén bao phim', '2.5mg/250mg', 'Uống', TRUE, 'Phối hợp kích thích tiết insulin và giảm đề kháng insulin'),
(19, 'GLIMEPIRIDE-PIOGLITAZONE', 'Glimepiride-Pioglitazone', 'Glimepiride + Pioglitazone', 'Duetact', 'Viên nén', '30mg/2mg', 'Uống', TRUE, 'Phối hợp Sulfonylurea thế hệ 3 và TZD'),
(20, 'METFORMIN-ROSIGLITAZONE', 'Metformin-Rosiglitazone', 'Metformin + Rosiglitazone', 'Avandamet', 'Viên nén', '2mg/500mg', 'Uống', TRUE, 'Phối hợp Biguanide và TZD'),
(21, 'METFORMIN-PIOGLITAZONE', 'Metformin-Pioglitazone', 'Metformin + Pioglitazone', 'Actoplus Met', 'Viên nén', '15mg/500mg', 'Uống', TRUE, 'Phối hợp Biguanide và TZD'),
(22, 'EXAMIDE', 'Examide', 'Examide', 'Examide', 'Viên nén', 'Standard', 'Uống', FALSE, 'Thuốc đối chiếu nghiên cứu'),
(23, 'CITOGLIPTON', 'Citoglipton', 'Citoglipton', 'Citoglipton', 'Viên nén', 'Standard', 'Uống', FALSE, 'Thuốc đối chiếu nghiên cứu'),
(24, 'ASPIRIN', 'Aspirin', 'Aspirin 81mg', 'Aspilets / Bayer', 'Viên bao tan ruột', '81mg', 'Uống', TRUE, 'Kháng kết tập tiểu cầu phòng biến cố tim mạch ở bệnh nhân ĐTĐ'),
(25, 'LISINOPRIL', 'Lisinopril', 'Lisinopril', 'Zestril', 'Viên nén', '10mg', 'Uống', TRUE, 'Ức chế men chuyển (ACEI) hạ áp và bảo vệ thận đái tháo đường'),
(26, 'ATORVASTATIN', 'Atorvastatin', 'Atorvastatin', 'Lipitor', 'Viên nén bao phim', '20mg / 40mg', 'Uống', TRUE, 'Statin hạ lipid máu phòng ngừa xơ vữa động mạch')
ON DUPLICATE KEY UPDATE generic_name=VALUES(generic_name);

-- 6. Patients
INSERT INTO patients (id, patient_code, full_name, gender, date_of_birth, identity_card, address, phone, insurance_number) VALUES
(1, 'PAT001', 'Nguyễn Văn Hùng', 'Nam', '1965-04-12', '001065012345', '120 Hai Bà Trưng, Quận 1, TP.HCM', '0934567890', 'BHYT-79-001'),
(2, 'PAT002', 'Lê Thị Thu Thảo', 'Nữ', '1972-08-25', '001072023456', '45 Lê Duẩn, Ba Đình, Hà Nội', '0945678901', 'BHYT-01-002'),
(3, 'PAT003', 'Phạm Minh Đức', 'Nam', '1958-11-03', '001058034567', '78 Trần Phú, Hải Châu, Đà Nẵng', '0956789012', 'BHYT-48-003'),
(4, 'PAT004', 'Vũ Hoàng Nam', 'Nam', '1980-03-15', '001080045678', '99 Nguyễn Huệ, TP. Huế', '0967890123', 'BHYT-46-004')
ON DUPLICATE KEY UPDATE patient_code=VALUES(patient_code);

-- 7. Medical Visits
INSERT INTO medical_visits (id, visit_code, patient_id, doctor_id, visit_date, symptoms, initial_diagnosis_id, clinical_notes, status) VALUES
(1, 'VIS2026-001', 1, 1, '2026-09-18 08:30:00', 'Đau ngực trái, khát nước, đường huyết đói 11.2 mmol/L', 1, 'Tiền sử ĐTĐ type 2 kèm tăng huyết áp', 'COMPLETED'),
(2, 'VIS2026-002', 2, 1, '2026-09-18 09:45:00', 'Sút cân, mệt mỏi, HbA1c 9.1%', 3, 'ĐTĐ type 2 kiểm soát kém, xem xét phối hợp Insulin', 'COMPLETED'),
(3, 'VIS2026-003', 3, 2, '2026-09-19 10:15:00', 'Khát nhiều, tiểu đêm, phù nhẹ mu bàn chân', 1, 'HbA1c 8.4%, HA 145/90 mmHg', 'ACTIVE')
ON DUPLICATE KEY UPDATE visit_code=VALUES(visit_code);

-- 8. Drug Interactions (FDA DDI / DailyMed - Dữ liệu tra cứu tương tác thuốc chuẩn)
INSERT INTO drug_interactions (id, drug_name_a, drug_name_b, interaction_status, severity, description, source) VALUES
(1, 'Metformin', 'Furosemide', 'KNOWN', 'Moderate', 'Furosemide có thể làm tăng nồng độ Metformin trong huyết tương; cần theo dõi toan lactic máu và chức năng thận.', 'FDA DDI / DailyMed'),
(2, 'Insulin', 'Metoprolol', 'KNOWN', 'Moderate', 'Thuốc chẹn beta như Metoprolol có thể che lấp các triệu chứng cảnh báo hạ đường huyết (như tim đập nhanh, run rẩy).', 'FDA DDI / DailyMed'),
(3, 'Glipizide', 'Ciprofloxacin', 'KNOWN', 'Major', 'Kháng sinh quinolone phối hợp với sulfonylurea có thể gây hạ đường huyết nghiêm trọng đe dọa tính mạng.', 'FDA FAERS / DailyMed'),
(4, 'Lisinopril', 'Potassium Chloride', 'KNOWN', 'Major', 'Thuốc ức chế men chuyển phối hợp chất bổ sung kali làm tăng nguy cơ tăng kali máu nghiêm trọng.', 'FDA DDI'),
(5, 'Aspirin', 'Heparin', 'KNOWN', 'Major', 'Tăng nguy cơ xuất huyết tiêu hóa và chảy máu nghiêm trọng do tác dụng hiệp đồng chống đông máu.', 'FDA DDI'),
(6, 'Metformin', 'Insulin', 'KNOWN', 'Minor', 'Phối hợp thường quy trong điều trị ĐTĐ type 2 mất bù; cần chỉnh liều insulin phù hợp để tránh hạ đường huyết.', 'FDA DDI / Clinical Guidelines'),
(7, 'Glipizide', 'Metformin', 'KNOWN', 'Minor', 'Phối hợp phổ biến sulfonylurea và biguanide; kiểm soát tốt đường huyết với nguy cơ hạ đường huyết ở mức kiểm soát.', 'FDA DailyMed')
ON DUPLICATE KEY UPDATE interaction_status=VALUES(interaction_status);
