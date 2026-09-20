-- =============================================================================
-- HỆ THỐNG BỆNH VIỆN TÍCH HỢP KHAI PHÁ LUẬT KẾT HỢP THUỐC ĐIỀU TRỊ
-- Dataset: Diabetes 130-US Hospitals (1999-2008) - UCI Machine Learning Repository
-- CSDL ĐẦY ĐỦ (FULL DATABASE DUMP: DDL CẤU TRÚC BẢNG + DỮ LIỆU KHỞI TẠO ĐẦY ĐỦ)
-- File: database/hospital_drug_mining_full.sql
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+07:00";

-- -----------------------------------------------------------------------------
-- 0. KHỞI TẠO CƠ SỞ DỮ LIỆU
-- -----------------------------------------------------------------------------
DROP DATABASE IF EXISTS hospital_drug_mining;
CREATE DATABASE hospital_drug_mining CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_drug_mining;

-- -----------------------------------------------------------------------------
-- 1. BẢNG ROLES (Vai trò người dùng)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS roles;
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 2. BẢNG USERS (Tài khoản người dùng)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 3. BẢNG DOCTORS (Bác sĩ điều trị)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS doctors;
CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    doctor_code VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100),
    department VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doctors_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 4. BẢNG PATIENTS (Hồ sơ bệnh nhân)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS patients;
CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_code VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    date_of_birth DATE,
    identity_card VARCHAR(20),
    address VARCHAR(255),
    phone VARCHAR(20),
    insurance_number VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 5. BẢNG DIAGNOSES (Danh mục chẩn đoán ICD-9/ICD-10)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS diagnoses;
CREATE TABLE diagnoses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    icd_code VARCHAR(20) NOT NULL UNIQUE,
    disease_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 6. BẢNG MEDICINES (Danh mục thuốc chuẩn hóa - 26 nhóm thuốc)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS medicines;
CREATE TABLE medicines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    drug_code VARCHAR(50) NOT NULL UNIQUE,
    generic_name VARCHAR(150) NOT NULL,
    display_name VARCHAR(150),
    brand_name VARCHAR(150),
    dosage_form VARCHAR(50),
    strength VARCHAR(50),
    route VARCHAR(50),
    active BOOLEAN DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_med_name (generic_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 7. BẢNG MEDICAL_VISITS (Lượt khám bệnh lâm sàng)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS medical_visits;
CREATE TABLE medical_visits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_code VARCHAR(50) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    visit_date DATETIME NOT NULL,
    symptoms TEXT,
    initial_diagnosis_id BIGINT,
    clinical_notes TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_visits_patients FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_visits_doctors FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    CONSTRAINT fk_visits_diagnoses FOREIGN KEY (initial_diagnosis_id) REFERENCES diagnoses(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 8. BẢNG PRESCRIPTIONS (Đơn thuốc)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS prescriptions;
CREATE TABLE prescriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_code VARCHAR(50) NOT NULL UNIQUE,
    visit_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    advice TEXT,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    CONSTRAINT fk_prescriptions_visits FOREIGN KEY (visit_id) REFERENCES medical_visits(id) ON DELETE CASCADE,
    CONSTRAINT fk_prescriptions_doctors FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 9. BẢNG PRESCRIPTION_ITEMS (Chi tiết các thuốc trong đơn)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS prescription_items;
CREATE TABLE prescription_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    duration_days INT DEFAULT 7,
    instructions TEXT,
    CONSTRAINT fk_prescription_items_presc FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE,
    CONSTRAINT fk_prescription_items_med FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 10. BẢNG DATASET_IMPORTS (Nhật ký nạp dữ liệu y tế UCI Diabetes)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS dataset_imports;
CREATE TABLE dataset_imports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    source VARCHAR(150) DEFAULT 'UCI Diabetes 130-US Hospitals',
    file_type VARCHAR(50) NOT NULL,
    file_size_bytes BIGINT,
    total_records INT DEFAULT 0,
    valid_records INT DEFAULT 0,
    invalid_records INT DEFAULT 0,
    unique_hadm_count INT DEFAULT 0,
    unique_drug_count INT DEFAULT 0,
    final_transactions_count INT DEFAULT 0,
    status VARCHAR(30) DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 11. BẢNG DATASET_STATISTICS (Thống kê Data Understanding CRISP-DM)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS dataset_statistics;
CREATE TABLE dataset_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    import_id BIGINT,
    dataset_name VARCHAR(255) NOT NULL,
    total_records INT DEFAULT 0,
    valid_records INT DEFAULT 0,
    missing_hadm_count INT DEFAULT 0,
    missing_drug_count INT DEFAULT 0,
    unique_subject_count INT DEFAULT 0,
    unique_hadm_count INT DEFAULT 0,
    unique_drug_count INT DEFAULT 0,
    zero_drug_encounter_count INT DEFAULT 0,
    one_drug_encounter_count INT DEFAULT 0,
    multi_drug_transaction_count INT DEFAULT 0,
    unique_diagnosis_count INT DEFAULT 0,
    avg_drugs_per_hadm DOUBLE DEFAULT 0.0,
    min_drugs_per_hadm INT DEFAULT 0,
    max_drugs_per_hadm INT DEFAULT 0,
    final_transaction_count INT DEFAULT 0,
    top_drugs_json TEXT,
    age_distribution_json TEXT,
    diagnosis_distribution_json TEXT,
    medication_count_distribution_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dataset_stats_import FOREIGN KEY (import_id) REFERENCES dataset_imports(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 12. BẢNG TRANSACTIONS (Giao dịch thuốc phục vụ khai phá)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS transactions;
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    encounter_id VARCHAR(64) NOT NULL,
    patient_reference VARCHAR(64),
    source_import_id BIGINT,
    item_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tx_encounter (encounter_id),
    INDEX idx_tx_patient (patient_reference),
    INDEX idx_tx_import (source_import_id),
    CONSTRAINT fk_tx_import FOREIGN KEY (source_import_id) REFERENCES dataset_imports(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 13. BẢNG TRANSACTION_ITEMS (Chi tiết các thuốc trong giao dịch)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS transaction_items;
CREATE TABLE transaction_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    medicine_id BIGINT,
    drug_name VARCHAR(150) NOT NULL,
    INDEX idx_tx_item_trans (transaction_id),
    INDEX idx_tx_item_med (medicine_id),
    CONSTRAINT fk_tx_item_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_tx_item_med FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 14. BẢNG MINING_RUNS (Lịch sử các phiên chạy thuật toán khai phá)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS mining_runs;
CREATE TABLE mining_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    run_name VARCHAR(100),
    algorithm VARCHAR(50) NOT NULL,
    dataset_source VARCHAR(255),
    min_support DOUBLE NOT NULL,
    min_confidence DOUBLE NOT NULL,
    min_lift DOUBLE NOT NULL,
    max_itemset_size INT DEFAULT 10,
    transaction_count INT NOT NULL,
    unique_drug_count INT NOT NULL,
    frequent_itemset_count INT NOT NULL,
    rule_count INT NOT NULL,
    runtime_ms BIGINT NOT NULL,
    memory_usage_mb DOUBLE DEFAULT 0.0,
    started_at DATETIME,
    finished_at DATETIME,
    status VARCHAR(30) DEFAULT 'SUCCESS',
    dataset_import_id BIGINT,
    selected_for_recommendation BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_mining_runs_active (selected_for_recommendation),
    CONSTRAINT fk_mining_runs_import FOREIGN KEY (dataset_import_id) REFERENCES dataset_imports(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 15. BẢNG FREQUENT_ITEMSETS (Tập phổ biến)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS frequent_itemsets;
CREATE TABLE frequent_itemsets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mining_run_id BIGINT NOT NULL,
    itemset_string TEXT NOT NULL,
    item_count INT NOT NULL,
    support DOUBLE NOT NULL,
    support_count INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_frequent_itemsets_run FOREIGN KEY (mining_run_id) REFERENCES mining_runs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 16. BẢNG FREQUENT_ITEMSET_ITEMS (Chi tiết các mục trong tập phổ biến)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS frequent_itemset_items;
CREATE TABLE frequent_itemset_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    itemset_id BIGINT NOT NULL,
    medicine_id BIGINT,
    drug_name VARCHAR(150) NOT NULL,
    CONSTRAINT fk_frequent_itemset_items_set FOREIGN KEY (itemset_id) REFERENCES frequent_itemsets(id) ON DELETE CASCADE,
    CONSTRAINT fk_frequent_itemset_items_med FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 17. BẢNG ASSOCIATION_RULES (Luật kết hợp)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS association_rules;
CREATE TABLE association_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mining_run_id BIGINT NOT NULL,
    antecedent TEXT NOT NULL,
    consequent TEXT NOT NULL,
    support DOUBLE NOT NULL,
    confidence DOUBLE NOT NULL,
    lift DOUBLE NOT NULL,
    antecedent_size INT NOT NULL,
    consequent_size INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rules_mining_run (mining_run_id),
    CONSTRAINT fk_rules_run FOREIGN KEY (mining_run_id) REFERENCES mining_runs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 18. BẢNG ASSOCIATION_RULE_ANTECEDENTS (Tiền đề chuẩn hóa gắn Medicine ID)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS association_rule_antecedents;
CREATE TABLE association_rule_antecedents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    medicine_id BIGINT,
    drug_name VARCHAR(150) NOT NULL,
    INDEX idx_rule_ante_rule (rule_id),
    INDEX idx_rule_ante_med (medicine_id),
    CONSTRAINT fk_rule_antecedents_rule FOREIGN KEY (rule_id) REFERENCES association_rules(id) ON DELETE CASCADE,
    CONSTRAINT fk_rule_antecedents_med FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 19. BẢNG ASSOCIATION_RULE_CONSEQUENTS (Hệ quả chuẩn hóa gắn Medicine ID)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS association_rule_consequents;
CREATE TABLE association_rule_consequents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    medicine_id BIGINT,
    drug_name VARCHAR(150) NOT NULL,
    INDEX idx_rule_cons_rule (rule_id),
    INDEX idx_rule_cons_med (medicine_id),
    CONSTRAINT fk_rule_consequents_rule FOREIGN KEY (rule_id) REFERENCES association_rules(id) ON DELETE CASCADE,
    CONSTRAINT fk_rule_consequents_med FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 20. BẢNG ALGORITHM_BENCHMARKS (Đo lường hiệu năng đối đầu Apriori vs FP-Growth)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS algorithm_benchmarks;
CREATE TABLE algorithm_benchmarks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    benchmark_name VARCHAR(100),
    dataset_name VARCHAR(100),
    transaction_count INT NOT NULL,
    min_support DOUBLE NOT NULL,
    min_confidence DOUBLE NOT NULL,
    min_lift DOUBLE NOT NULL,
    apriori_runtime_ms BIGINT NOT NULL,
    fpgrowth_runtime_ms BIGINT NOT NULL,
    apriori_itemset_count INT NOT NULL,
    fpgrowth_itemset_count INT NOT NULL,
    apriori_rule_count INT NOT NULL,
    fpgrowth_rule_count INT NOT NULL,
    apriori_memory_mb DOUBLE,
    fpgrowth_memory_mb DOUBLE,
    rule_overlap_percentage DOUBLE,
    itemset_overlap_percentage DOUBLE,
    recommended_algorithm VARCHAR(50),
    conclusion_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 21. BẢNG DRUG_INTERACTIONS (Cơ sở tri thức cảnh báo tương tác thuốc FDA DDI)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS drug_interactions;
CREATE TABLE drug_interactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    drug_name_a VARCHAR(150) NOT NULL,
    drug_name_b VARCHAR(150) NOT NULL,
    interaction_status VARCHAR(30) DEFAULT 'KNOWN',
    severity VARCHAR(30) DEFAULT 'Moderate',
    description TEXT,
    source VARCHAR(150) DEFAULT 'Demo interaction knowledge base (DailyMed ref)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ddi_drugs (drug_name_a, drug_name_b)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- NẠP TOÀN BỘ DỮ LIỆU BAN ĐẦU (SEED DATA)
-- =============================================================================

-- 1. Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Quản trị viên hệ thống & Kỹ sư Data Mining'),
(2, 'ROLE_DOCTOR', 'Bác sĩ điều trị & Kê đơn thuốc');

-- 2. Users (Mật khẩu: admin123 và doctor123)
INSERT INTO users (id, username, password, full_name, email, phone, role_id, enabled) VALUES
(1, 'admin', '$2a$10$slYQmyNdxq31q8O6WstBLe8hD1P/Q9sWpXJ2jG7j8s6lK7o/1jUaK', 'Quản Trị Viên Hệ Thống', 'admin@hospital.vn', '0901234567', 1, TRUE),
(2, 'doctor', '$2a$10$wNqvO9uWJgqFm011WsmzmeF6nL2W2m/t2Wj8Z7wFq9p3a4y5o2k6m', 'BS.CKII Nguyễn Văn Tuấn', 'tuan.nv@hospital.vn', '0912345678', 2, TRUE),
(3, 'doctor2', '$2a$10$wNqvO9uWJgqFm011WsmzmeF6nL2W2m/t2Wj8Z7wFq9p3a4y5o2k6m', 'ThS.BS Trần Thị Mai', 'mai.tt@hospital.vn', '0987654321', 2, TRUE);

-- 3. Doctors
INSERT INTO doctors (id, user_id, doctor_code, full_name, specialty, department, phone, email) VALUES
(1, 2, 'DOC001', 'BS.CKII Nguyễn Văn Tuấn', 'Nội Tiết - Đái Tháo Đường', 'Khoa Nội Tiết', '0912345678', 'tuan.nv@hospital.vn'),
(2, 3, 'DOC002', 'ThS.BS Trần Thị Mai', 'Tim Mạch & Chuyển Hóa', 'Khoa Nội Tổng Hợp', '0987654321', 'mai.tt@hospital.vn');

-- 4. Diagnoses (ICD-9 & ICD-10)
INSERT INTO diagnoses (id, icd_code, disease_name, category, description) VALUES
(1, '250', 'Đái tháo đường không biến chứng', 'Nội tiết', 'ICD-9-CM 250'),
(2, '250.01', 'Đái tháo đường type 1 không biến chứng', 'Nội tiết', 'Phụ thuộc insulin'),
(3, '250.02', 'Đái tháo đường type 2 mất bù', 'Nội tiết', 'Đường huyết dao động mạnh'),
(4, '401.9', 'Tăng huyết áp vô căn', 'Tim mạch', 'Bệnh lý tim mạch kèm theo'),
(5, '428.0', 'Suy tim sung huyết', 'Tim mạch', 'Suy giảm chức năng tim'),
(6, '272.0', 'Tăng cholesterol máu thuần túy', 'Nội tiết', 'Rối loạn lipid máu');

-- 5. Medicines (26 nhóm thuốc chuẩn hóa UCI Diabetes + thuốc phối hợp bệnh nền)
INSERT INTO medicines (id, drug_code, generic_name, display_name, brand_name, dosage_form, strength, route, active, description) VALUES
(1, 'METFORMIN', 'Metformin', 'Metformin HCl', 'Glucophage', 'Viên nén', '500mg', 'Uống', TRUE, 'Thuốc hạ đường huyết nhóm Biguanide'),
(2, 'INSULIN', 'Insulin', 'Insulin Người Sinh Học', 'Humulin / Novolin', 'Dung dịch tiêm', '100 IU/ml', 'Tiêm dưới da', TRUE, 'Hormone kiểm soát đường huyết sinh học'),
(3, 'GLIPIZIDE', 'Glipizide', 'Glipizide', 'Glucotrol', 'Viên giải phóng kéo dài', '5mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 2 kích thích tiết insulin'),
(4, 'GLYBURIDE', 'Glyburide', 'Glyburide (Glibenclamide)', 'Diabeta', 'Viên nén', '5mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 2'),
(5, 'PIOGLITAZONE', 'Pioglitazone', 'Pioglitazone HCl', 'Actos', 'Viên nén', '15mg', 'Uống', TRUE, 'Thiazolidinedione (TZD) tăng nhạy cảm insulin'),
(6, 'ROSIGLITAZONE', 'Rosiglitazone', 'Rosiglitazone Maleate', 'Avandia', 'Viên nén bao phim', '4mg', 'Uống', TRUE, 'Thiazolidinedione (TZD)'),
(7, 'GLIMEPIRIDE', 'Glimepiride', 'Glimepiride', 'Amaryl', 'Viên nén', '2mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 3'),
(8, 'REPAGLINIDE', 'Repaglinide', 'Repaglinide', 'Prandin', 'Viên nén', '1mg', 'Uống', TRUE, 'Nhóm Meglitinide kích thích tiết insulin nhanh'),
(9, 'NATEGLINIDE', 'Nateglinide', 'Nateglinide', 'Starlix', 'Viên nén', '60mg', 'Uống', TRUE, 'Dẫn xuất phenylalanine kích thích bài tiết insulin'),
(10, 'ACARBOSE', 'Acarbose', 'Acarbose', 'Precose', 'Viên nén', '50mg', 'Uống', TRUE, 'Ức chế alpha-glucosidase đường ruột'),
(11, 'MIGLITOL', 'Miglitol', 'Miglitol', 'Glyset', 'Viên nén', '50mg', 'Uống', TRUE, 'Ức chế alpha-glucosidase'),
(12, 'CHLORPROPAMIDE', 'Chlorpropamide', 'Chlorpropamide', 'Diabinese', 'Viên nén', '250mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 1'),
(13, 'TOLBUTAMIDE', 'Tolbutamide', 'Tolbutamide', 'Orinase', 'Viên nén', '500mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 1'),
(14, 'TOLAZAMIDE', 'Tolazamide', 'Tolazamide', 'Tolinase', 'Viên nén', '100mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 1'),
(15, 'ACETOHEXAMIDE', 'Acetohexamide', 'Acetohexamide', 'Dymelor', 'Viên nén', '250mg', 'Uống', TRUE, 'Sulfonylurea thế hệ 1'),
(16, 'TROGLITAZONE', 'Troglitazone', 'Troglitazone', 'Rezulin', 'Viên nén', '200mg', 'Uống', TRUE, 'Nhóm TZD lịch sử'),
(17, 'GLYBURIDE-METFORMIN', 'Glyburide-Metformin', 'Glyburide + Metformin', 'Glucovance', 'Viên nén', '2.5mg/500mg', 'Uống', TRUE, 'Phối hợp cố định liều Sulfonylurea + Biguanide'),
(18, 'GLIPIZIDE-METFORMIN', 'Glipizide-Metformin', 'Glipizide + Metformin', 'Metaglip', 'Viên nén', '2.5mg/250mg', 'Uống', TRUE, 'Phối hợp cố định liều Sulfonylurea + Biguanide'),
(19, 'GLIMEPIRIDE-PIOGLITAZONE', 'Glimepiride-Pioglitazone', 'Glimepiride + Pioglitazone', 'Duetact', 'Viên nén', '30mg/2mg', 'Uống', TRUE, 'Phối hợp Sulfonylurea + TZD'),
(20, 'METFORMIN-ROSIGLITAZONE', 'Metformin-Rosiglitazone', 'Metformin + Rosiglitazone', 'Avandamet', 'Viên nén', '2mg/500mg', 'Uống', TRUE, 'Phối hợp Biguanide + TZD'),
(21, 'METFORMIN-PIOGLITAZONE', 'Metformin-Pioglitazone', 'Metformin + Pioglitazone', 'Actoplus Met', 'Viên nén', '15mg/500mg', 'Uống', TRUE, 'Phối hợp Biguanide + TZD'),
(22, 'EXAMIDE', 'Examide', 'Examide', 'Examide', 'Viên nén', '10mg', 'Uống', FALSE, 'Thuốc đối chiếu nghiên cứu'),
(23, 'CITOGLIPTON', 'Citoglipton', 'Citoglipton', 'Citoglipton', 'Viên nén', '50mg', 'Uống', FALSE, 'Thuốc đối chiếu nghiên cứu'),
(24, 'ASPIRIN', 'Aspirin', 'Aspirin 81mg', 'Aspilets', 'Viên bao tan ruột', '81mg', 'Uống', TRUE, 'Kháng kết tập tiểu cầu phòng biến chứng tim mạch'),
(25, 'LISINOPRIL', 'Lisinopril', 'Lisinopril', 'Zestril', 'Viên nén', '10mg', 'Uống', TRUE, 'Ức chế men chuyển hạ áp và bảo vệ thận ĐTĐ'),
(26, 'ATORVASTATIN', 'Atorvastatin', 'Atorvastatin', 'Lipitor', 'Viên nén', '20mg', 'Uống', TRUE, 'Statin hạ lipid máu phòng ngừa xơ vữa động mạch');

-- 6. Patients
INSERT INTO patients (id, patient_code, full_name, gender, date_of_birth, identity_card, address, phone, insurance_number) VALUES
(1, 'PAT001', 'Nguyễn Văn Hùng', 'Nam', '1965-04-12', '001065012345', '120 Hai Bà Trưng, Quận 1, TP.HCM', '0934567890', 'BHYT-79-001'),
(2, 'PAT002', 'Lê Thị Thu Thảo', 'Nữ', '1972-08-25', '001072023456', '45 Lê Duẩn, Ba Đình, Hà Nội', '0945678901', 'BHYT-01-002'),
(3, 'PAT003', 'Phạm Minh Đức', 'Nam', '1958-11-03', '001058034567', '78 Trần Phú, Hải Châu, Đà Nẵng', '0956789012', 'BHYT-48-003'),
(4, 'PAT004', 'Vũ Hoàng Nam', 'Nam', '1980-03-15', '001080045678', '99 Nguyễn Huệ, TP. Huế', '0967890123', 'BHYT-46-004');

-- 7. Medical Visits
INSERT INTO medical_visits (id, visit_code, patient_id, doctor_id, visit_date, symptoms, initial_diagnosis_id, clinical_notes, status) VALUES
(1, 'VIS2026-001', 1, 1, '2026-09-18 08:30:00', 'Đau ngực trái, khát nước, đường huyết đói 11.2 mmol/L', 1, 'Tiền sử ĐTĐ type 2 kèm tăng huyết áp', 'COMPLETED'),
(2, 'VIS2026-002', 2, 1, '2026-09-18 09:45:00', 'Sút cân, mệt mỏi, HbA1c 9.1%', 3, 'ĐTĐ type 2 kiểm soát kém, xem xét phối hợp Insulin', 'COMPLETED'),
(3, 'VIS2026-003', 3, 2, '2026-09-19 10:15:00', 'Khát nhiều, tiểu đêm, phù nhẹ mu bàn chân', 1, 'HbA1c 8.4%, HA 145/90 mmHg', 'ACTIVE');

-- 8. Prescriptions & Prescription Items
INSERT INTO prescriptions (id, prescription_code, visit_id, doctor_id, created_at, advice, status) VALUES
(1, 'RX2026-001', 1, 1, '2026-09-18 09:00:00', 'Uống thuốc đúng giờ, hạn chế tinh bột, tái khám sau 1 tháng', 'COMPLETED'),
(2, 'RX2026-002', 2, 1, '2026-09-18 10:15:00', 'Theo dõi đường huyết mao mạch tại nhà 2 lần/ngày', 'COMPLETED');

INSERT INTO prescription_items (id, prescription_id, medicine_id, dosage, frequency, duration_days, instructions) VALUES
(1, 1, 1, '500mg', '2 lần/ngày', 30, 'Uống ngay sau bữa ăn sáng và tối'),
(2, 1, 25, '10mg', '1 lần/ngày', 30, 'Uống buổi sáng'),
(3, 2, 2, '20 IU', '1 lần/ngày', 30, 'Tiêm dưới da buổi tối trước khi đi ngủ'),
(4, 2, 1, '850mg', '2 lần/ngày', 30, 'Uống trong bữa ăn');

-- 9. Demo Drug Interactions (FDA DDI Knowledge Base)
INSERT INTO drug_interactions (id, drug_name_a, drug_name_b, interaction_status, severity, description, source) VALUES
(1, 'Metformin', 'Insulin', 'KNOWN', 'Moderate', 'Phối hợp làm tăng hiệu lực hạ đường huyết. Cần theo dõi đường huyết chặt chẽ và điều chỉnh liều insulin phù hợp.', 'Demo interaction knowledge base (DailyMed ref)'),
(2, 'Glyburide', 'Metformin', 'KNOWN', 'Moderate', 'Phối hợp sulfonylurea và biguanide có thể làm tăng nguy cơ hạ đường huyết, đặc biệt khi ăn uống không đều đặn.', 'Demo interaction knowledge base (DailyMed ref)'),
(3, 'Glipizide', 'Insulin', 'KNOWN', 'Major', 'Tăng nguy cơ hạ đường huyết nghiêm trọng khi dùng đồng thời sulfonylurea liều cao với insulin ngoại sinh.', 'Demo interaction knowledge base (DailyMed ref)'),
(4, 'Repaglinide', 'Insulin', 'KNOWN', 'Moderate', 'Tăng nguy cơ hạ đường huyết cấp tính do đồng kích thích tiết insulin và bổ sung insulin.', 'Demo interaction knowledge base (DailyMed ref)'),
(5, 'Pioglitazone', 'Insulin', 'KNOWN', 'Moderate', 'Phối hợp TZD với insulin làm tăng nguy cơ giữ dịch phù nề và suy tim sung huyết.', 'Demo interaction knowledge base (DailyMed ref)'),
(6, 'Rosiglitazone', 'Insulin', 'KNOWN', 'Major', 'Chống chỉ định phối hợp ở bệnh nhân có tiền sử suy tim do tăng giữ dịch quá mức.', 'Demo interaction knowledge base (DailyMed ref)'),
(7, 'Aspirin', 'Heparin', 'KNOWN', 'Major', 'Tăng nguy cơ xuất huyết tiêu hóa và chảy máu nghiêm trọng do tác dụng hiệp đồng chống đông máu.', 'Demo interaction knowledge base (DailyMed ref)');

-- 10. Dataset Imports (Bộ dữ liệu chuẩn UCI Diabetes)
INSERT INTO dataset_imports (id, file_name, source, file_type, file_size_bytes, total_records, valid_records, invalid_records, unique_hadm_count, unique_drug_count, final_transactions_count, status, created_at) VALUES
(1, 'diabetic_data.csv', 'UCI Diabetes 130-US Hospitals (1999-2008)', 'text/csv', 19159383, 101766, 101766, 0, 101766, 21, 31049, 'SUCCESS', '2026-09-20 00:00:00');

-- 11. Dataset Statistics
INSERT INTO dataset_statistics (id, import_id, dataset_name, total_records, valid_records, unique_subject_count, unique_hadm_count, unique_drug_count, zero_drug_encounter_count, one_drug_encounter_count, multi_drug_transaction_count, unique_diagnosis_count, avg_drugs_per_hadm, min_drugs_per_hadm, max_drugs_per_hadm, final_transaction_count, top_drugs_json, created_at) VALUES
(1, 1, 'diabetic_data.csv', 101766, 101766, 71518, 101766, 21, 23075, 47642, 31049, 915, 1.18, 0, 6, 31049, '[{"name":"Insulin","count":54383},{"name":"Metformin","count":20150},{"name":"Glipizide","count":12906},{"name":"Glyburide","count":10831}]', '2026-09-20 00:01:00');

-- 12. Mining Runs (Khởi tạo phiên khai phá chuẩn và kích hoạt làm Active Model)
INSERT INTO mining_runs (id, run_name, algorithm, dataset_source, min_support, min_confidence, min_lift, max_itemset_size, transaction_count, unique_drug_count, frequent_itemset_count, rule_count, runtime_ms, memory_usage_mb, started_at, finished_at, status, dataset_import_id, selected_for_recommendation, created_at) VALUES
(1, 'APRIORI - Baseline', 'APRIORI', 'diabetic_data.csv', 0.01, 0.30, 1.0, 5, 31049, 21, 45, 3, 243, 8.02, '2026-09-20 00:05:00', '2026-09-20 00:05:01', 'SUCCESS', 1, FALSE, '2026-09-20 00:05:01'),
(2, 'FP_GROWTH - Active Production Model', 'FP_GROWTH', 'diabetic_data.csv', 0.01, 0.30, 1.0, 5, 31049, 21, 45, 3, 46, 8.00, '2026-09-20 00:06:00', '2026-09-20 00:06:01', 'SUCCESS', 1, TRUE, '2026-09-20 00:06:01');

-- 13. Association Rules (3 luật khai phá cốt lõi từ 31,049 ca điều trị)
INSERT INTO association_rules (id, mining_run_id, antecedent, consequent, support, confidence, lift, antecedent_size, consequent_size, created_at) VALUES
(1, 2, 'Glyburide-Metformin', 'Insulin', 0.0133, 0.8227, 1.0908, 1, 1, '2026-09-20 00:06:01'),
(2, 2, 'Repaglinide', 'Insulin', 0.0281, 0.7558, 1.0021, 1, 1, '2026-09-20 00:06:01'),
(3, 2, 'Glyburide', 'Metformin', 0.1230, 0.5250, 1.0513, 1, 1, '2026-09-20 00:06:01');

-- 14. Association Rule Antecedents & Consequents (Bảng quan hệ phục vụ query Indexing theo Medicine ID)
INSERT INTO association_rule_antecedents (id, rule_id, medicine_id, drug_name) VALUES
(1, 1, 17, 'Glyburide-Metformin'),
(2, 2, 8, 'Repaglinide'),
(3, 3, 4, 'Glyburide');

INSERT INTO association_rule_consequents (id, rule_id, medicine_id, drug_name) VALUES
(1, 1, 2, 'Insulin'),
(2, 2, 2, 'Insulin'),
(3, 3, 1, 'Metformin');

-- 15. Algorithm Benchmarks (Kết quả thực nghiệm đối đầu chuẩn 2 warm-up + 5 lượt đo chính thức)
INSERT INTO algorithm_benchmarks (id, benchmark_name, dataset_name, transaction_count, min_support, min_confidence, min_lift, apriori_runtime_ms, fpgrowth_runtime_ms, apriori_itemset_count, fpgrowth_itemset_count, apriori_rule_count, fpgrowth_rule_count, apriori_memory_mb, fpgrowth_memory_mb, rule_overlap_percentage, itemset_overlap_percentage, recommended_algorithm, conclusion_notes, created_at) VALUES
(1, 'UCI-130Hospitals-Supp0.01', 'diabetic_data.csv', 31049, 0.01, 0.30, 1.0, 243, 46, 45, 45, 3, 3, 8.02, 8.00, 100.0, 100.0, 'FP-Growth', 'Thực nghiệm 5 lượt đo median: FP-Growth nhanh hơn Apriori 5.3 lần (46 ms so với 243 ms). Jaccard đạt 100.0%. Khuyến nghị chọn FP-Growth.', '2026-09-20 00:07:00'),
(2, 'UCI-130Hospitals-Supp0.02', 'diabetic_data.csv', 31049, 0.02, 0.30, 1.0, 142, 43, 31, 31, 2, 2, 21.01, 8.00, 100.0, 100.0, 'FP-Growth', 'FP-Growth nhanh hơn 3.3 lần (43 ms so với 142 ms). Jaccard 100%.', '2026-09-20 00:08:00'),
(3, 'UCI-130Hospitals-Supp0.05', 'diabetic_data.csv', 31049, 0.05, 0.30, 1.0, 98, 39, 19, 19, 1, 1, 38.00, 7.50, 100.0, 100.0, 'FP-Growth', 'FP-Growth nhanh hơn 2.5 lần (39 ms so với 98 ms). Jaccard 100%.', '2026-09-20 00:09:00'),
(4, 'UCI-130Hospitals-Supp0.10', 'diabetic_data.csv', 31049, 0.10, 0.30, 1.0, 86, 35, 14, 14, 1, 1, 35.50, 7.50, 100.0, 100.0, 'FP-Growth', 'FP-Growth nhanh hơn 2.5 lần (35 ms so với 86 ms). Jaccard 100%.', '2026-09-20 00:10:00');

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- HOÀN TẤT NẠP TOÀN BỘ CƠ SỞ DỮ LIỆU HOSPITAL_DRUG_MINING
-- =============================================================================
