-- =============================================================================
-- HỆ THỐNG BỆNH VIỆN TÍCH HỢP KHAI PHÁ LUẬT KẾT HỢP THUỐC ĐIỀU TRỊ
-- Dataset chính: Diabetes 130-US Hospitals for Years 1999-2008 (UCI ML Repository)
-- File: database/schema.sql
-- =============================================================================

CREATE DATABASE IF NOT EXISTS hospital_drug_mining CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_drug_mining;

-- 1. Bảng Roles (Vai trò người dùng)
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Bảng Users (Tài khoản người dùng)
CREATE TABLE IF NOT EXISTS users (
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

-- 3. Bảng Doctors (Bác sĩ điều trị)
CREATE TABLE IF NOT EXISTS doctors (
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

-- 4. Bảng Patients (Hồ sơ bệnh nhân)
CREATE TABLE IF NOT EXISTS patients (
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

-- 5. Bảng Diagnoses (Danh mục Chẩn đoán / Bệnh ICD-9 & ICD-10)
CREATE TABLE IF NOT EXISTS diagnoses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    icd_code VARCHAR(20) NOT NULL UNIQUE,
    disease_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Bảng Medicines (Danh mục Thuốc - Medicine Master)
CREATE TABLE IF NOT EXISTS medicines (
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

-- 7. Bảng Medical Visits (Lượt khám bệnh)
CREATE TABLE IF NOT EXISTS medical_visits (
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

-- 8. Bảng Prescriptions (Đơn thuốc)
CREATE TABLE IF NOT EXISTS prescriptions (
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

-- 9. Bảng Prescription Items (Chi tiết đơn thuốc)
CREATE TABLE IF NOT EXISTS prescription_items (
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

-- 10. Bảng Dataset Imports (Nhật ký nạp dữ liệu y tế UCI Diabetes)
CREATE TABLE IF NOT EXISTS dataset_imports (
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

-- 11. Bảng Dataset Statistics (Thống kê chi tiết Data Understanding phục vụ CRISP-DM)
CREATE TABLE IF NOT EXISTS dataset_statistics (
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

-- 12. Bảng Transactions (Tập giao dịch kết hợp thuốc phục vụ Data Mining)
CREATE TABLE IF NOT EXISTS transactions (
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

-- 13. Bảng Transaction Items (Chi tiết các thuốc trong từng giao dịch)
CREATE TABLE IF NOT EXISTS transaction_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    medicine_id BIGINT,
    drug_name VARCHAR(150) NOT NULL,
    INDEX idx_tx_item_trans (transaction_id),
    INDEX idx_tx_item_med (medicine_id),
    CONSTRAINT fk_tx_item_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_tx_item_med FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. Bảng Mining Runs (Lịch sử mỗi lần chạy thuật toán khai phá)
CREATE TABLE IF NOT EXISTS mining_runs (
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

-- 15. Bảng Frequent Itemsets (Tập phổ biến)
CREATE TABLE IF NOT EXISTS frequent_itemsets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mining_run_id BIGINT NOT NULL,
    itemset_string TEXT NOT NULL,
    item_count INT NOT NULL,
    support DOUBLE NOT NULL,
    support_count INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_frequent_itemsets_run FOREIGN KEY (mining_run_id) REFERENCES mining_runs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 16. Bảng Frequent Itemset Items (Từng thuốc trong tập phổ biến)
CREATE TABLE IF NOT EXISTS frequent_itemset_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    itemset_id BIGINT NOT NULL,
    medicine_id BIGINT,
    drug_name VARCHAR(150) NOT NULL,
    CONSTRAINT fk_frequent_itemset_items_set FOREIGN KEY (itemset_id) REFERENCES frequent_itemsets(id) ON DELETE CASCADE,
    CONSTRAINT fk_frequent_itemset_items_med FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 17. Bảng Association Rules (Luật kết hợp)
CREATE TABLE IF NOT EXISTS association_rules (
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

-- 18. Bảng Association Rule Antecedents (Từng tiền đề kèm liên kết Medicine ID)
CREATE TABLE IF NOT EXISTS association_rule_antecedents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    medicine_id BIGINT,
    drug_name VARCHAR(150) NOT NULL,
    INDEX idx_rule_ante_rule (rule_id),
    INDEX idx_rule_ante_med (medicine_id),
    CONSTRAINT fk_rule_antecedents_rule FOREIGN KEY (rule_id) REFERENCES association_rules(id) ON DELETE CASCADE,
    CONSTRAINT fk_rule_antecedents_med FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 19. Bảng Association Rule Consequents (Từng hệ quả kèm liên kết Medicine ID)
CREATE TABLE IF NOT EXISTS association_rule_consequents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    medicine_id BIGINT,
    drug_name VARCHAR(150) NOT NULL,
    INDEX idx_rule_cons_rule (rule_id),
    INDEX idx_rule_cons_med (medicine_id),
    CONSTRAINT fk_rule_consequents_rule FOREIGN KEY (rule_id) REFERENCES association_rules(id) ON DELETE CASCADE,
    CONSTRAINT fk_rule_consequents_med FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 20. Bảng Algorithm Benchmarks (So sánh Apriori vs FP-Growth)
CREATE TABLE IF NOT EXISTS algorithm_benchmarks (
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

-- 21. Bảng Drug Interactions (Tra cứu tương tác thuốc FDA DDI)
CREATE TABLE IF NOT EXISTS drug_interactions (
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
