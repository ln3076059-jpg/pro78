package com.hospital.datamining.config;

import com.hospital.datamining.entity.*;
import com.hospital.datamining.repository.*;
import com.hospital.datamining.service.importer.DatasetImportService;
import com.hospital.datamining.service.mining.AprioriMiningService;
import com.hospital.datamining.service.mining.FPGrowthMiningService;
import com.hospital.datamining.service.mining.MiningEvaluationService;
import com.hospital.datamining.service.mining.MiningRunService;
import com.hospital.datamining.service.mining.model.MiningParameters;
import com.hospital.datamining.service.mining.model.MiningResult;
import com.hospital.datamining.service.preprocessing.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.seed.auto-mining:false}")
    private boolean autoMiningEnabled;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final MedicineRepository medicineRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final PatientRepository patientRepository;
    private final MedicalVisitRepository medicalVisitRepository;
    private final DrugInteractionRepository drugInteractionRepository;
    private final PasswordEncoder passwordEncoder;
    private final DatasetImportService datasetImportService;
    private final TransactionService transactionService;
    private final AprioriMiningService aprioriMiningService;
    private final FPGrowthMiningService fpGrowthMiningService;
    private final MiningRunService miningRunService;
    private final MiningEvaluationService miningEvaluationService;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           DoctorRepository doctorRepository,
                           MedicineRepository medicineRepository,
                           DiagnosisRepository diagnosisRepository,
                           PatientRepository patientRepository,
                           MedicalVisitRepository medicalVisitRepository,
                           DrugInteractionRepository drugInteractionRepository,
                           PasswordEncoder passwordEncoder,
                           DatasetImportService datasetImportService,
                           TransactionService transactionService,
                           AprioriMiningService aprioriMiningService,
                           FPGrowthMiningService fpGrowthMiningService,
                           MiningRunService miningRunService,
                           MiningEvaluationService miningEvaluationService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.medicineRepository = medicineRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.patientRepository = patientRepository;
        this.medicalVisitRepository = medicalVisitRepository;
        this.drugInteractionRepository = drugInteractionRepository;
        this.passwordEncoder = passwordEncoder;
        this.datasetImportService = datasetImportService;
        this.transactionService = transactionService;
        this.aprioriMiningService = aprioriMiningService;
        this.fpGrowthMiningService = fpGrowthMiningService;
        this.miningRunService = miningRunService;
        this.miningEvaluationService = miningEvaluationService;
    }

    @Override
    public void run(String... args) {
        log.info("Khởi tạo dữ liệu ban đầu cho hệ thống bệnh viện...");

        // 1. Roles
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_ADMIN")
                        .description("Quản trị viên & Kỹ sư Khai phá dữ liệu")
                        .build()));

        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_DOCTOR")
                        .description("Bác sĩ điều trị & Kê đơn thuốc")
                        .build()));

        // 2. Users
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản Trị Viên Hệ Thống")
                    .email("admin@hospital.vn")
                    .phone("0901234567")
                    .role(adminRole)
                    .enabled(true)
                    .build());
            log.info("Đã tạo tài khoản admin / admin123");
        }

        User doctorUser = userRepository.findByUsername("doctor")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("doctor")
                        .password(passwordEncoder.encode("doctor123"))
                        .fullName("BS.CKII Nguyễn Văn Tuấn")
                        .email("tuan.nv@hospital.vn")
                        .phone("0912345678")
                        .role(doctorRole)
                        .enabled(true)
                        .build()));

        // 3. Doctors
        if (doctorRepository.findByDoctorCode("DOC001").isEmpty()) {
            doctorRepository.save(Doctor.builder()
                    .user(doctorUser)
                    .doctorCode("DOC001")
                    .fullName("BS.CKII Nguyễn Văn Tuấn")
                    .specialty("Nội Tiết & Đái Tháo Đường")
                    .department("Khoa Nội Tiết - Chuyển Hóa")
                    .phone("0912345678")
                    .email("tuan.nv@hospital.vn")
                    .build());
        }

        // 4. Diagnoses (ICD-9 từ UCI Diabetes)
        if (diagnosisRepository.count() == 0) {
            diagnosisRepository.save(Diagnosis.builder().icdCode("250.00").diseaseName("Đái tháo đường type 2 không biến chứng").category("Nội tiết").build());
            diagnosisRepository.save(Diagnosis.builder().icdCode("250.02").diseaseName("Đái tháo đường type 2 không kiểm soát").category("Nội tiết").build());
            diagnosisRepository.save(Diagnosis.builder().icdCode("250.80").diseaseName("Đái tháo đường có biến chứng khác").category("Nội tiết").build());
            diagnosisRepository.save(Diagnosis.builder().icdCode("401.9").diseaseName("Tăng huyết áp nguyên phát").category("Tim mạch").build());
            diagnosisRepository.save(Diagnosis.builder().icdCode("414.01").diseaseName("Xơ vữa động mạch vành").category("Tim mạch").build());
            diagnosisRepository.save(Diagnosis.builder().icdCode("428.0").diseaseName("Suy tim sung huyết").category("Tim mạch").build());
            diagnosisRepository.save(Diagnosis.builder().icdCode("585.9").diseaseName("Bệnh thận mạn tính").category("Thận").build());
        }

        // 5. Medicines (23 thuốc chuẩn UCI Diabetes 130-US Hospitals + 3 thuốc phối hợp tim mạch/bệnh nền)
        if (medicineRepository.count() == 0) {
            initMedicine("METFORMIN", "Metformin", "Metformin", "Glucophage", "Viên nén", "500mg", "Uống", "Biguanide - Giảm sản xuất glucose ở gan");
            initMedicine("INSULIN", "Insulin", "Insulin", "Humulin / Lantus", "Bút tiêm / Lọ", "100 IU/ml", "Tiêm dưới da", "Insulin sinh học kiểm soát đường huyết");
            initMedicine("GLIPIZIDE", "Glipizide", "Glipizide", "Glucotrol", "Viên nén", "5mg", "Uống", "Sulfonylurea thế hệ 2 - Kích thích tụy tiết insulin");
            initMedicine("GLYBURIDE", "Glyburide", "Glyburide", "DiaBeta", "Viên nén", "5mg", "Uống", "Sulfonylurea thế hệ 2 - Kích thích tụy tiết insulin");
            initMedicine("PIOGLITAZONE", "Pioglitazone", "Pioglitazone", "Actos", "Viên nén", "15mg", "Uống", "Thiazolidinedione (TZD) - Tăng nhạy cảm insulin");
            initMedicine("ROSIGLITAZONE", "Rosiglitazone", "Rosiglitazone", "Avandia", "Viên nén", "4mg", "Uống", "Thiazolidinedione (TZD) - Tăng nhạy cảm insulin");
            initMedicine("REPAGLINIDE", "Repaglinide", "Repaglinide", "Prandin", "Viên nén", "1mg", "Uống", "Meglitinide - Kích thích tiết insulin nhanh bữa ăn");
            initMedicine("NATEGLINIDE", "Nateglinide", "Nateglinide", "Starlix", "Viên nén", "60mg", "Uống", "Meglitinide - Kích thích tiết insulin theo bữa ăn");
            initMedicine("CHLORPROPAMIDE", "Chlorpropamide", "Chlorpropamide", "Diabinese", "Viên nén", "250mg", "Uống", "Sulfonylurea thế hệ 1");
            initMedicine("GLIMEPIRIDE", "Glimepiride", "Glimepiride", "Amaryl", "Viên nén", "2mg", "Uống", "Sulfonylurea thế hệ 2");
            initMedicine("ACETOHEXAMIDE", "Acetohexamide", "Acetohexamide", "Dymelor", "Viên nén", "250mg", "Uống", "Sulfonylurea thế hệ 1");
            initMedicine("TOLBUTAMIDE", "Tolbutamide", "Tolbutamide", "Orinase", "Viên nén", "500mg", "Uống", "Sulfonylurea thế hệ 1");
            initMedicine("ACARBOSE", "Acarbose", "Acarbose", "Precose", "Viên nén", "50mg", "Uống", "Ức chế alpha-glucosidase đường ruột");
            initMedicine("MIGLITOL", "Miglitol", "Miglitol", "Glyset", "Viên nén", "50mg", "Uống", "Ức chế alpha-glucosidase đường ruột");
            initMedicine("TROGLITAZONE", "Troglitazone", "Troglitazone", "Rezulin", "Viên nén", "200mg", "Uống", "Thiazolidinedione (TZD)");
            initMedicine("TOLAZAMIDE", "Tolazamide", "Tolazamide", "Tolinase", "Viên nén", "100mg", "Uống", "Sulfonylurea thế hệ 1");
            initMedicine("EXAMIDE", "Examide", "Examide", "Examide", "Viên nén", "10mg", "Uống", "Phối hợp điều trị tiểu đường");
            initMedicine("CITOGLIPTON", "Citoglipton", "Citoglipton", "Citoglipton", "Viên nén", "50mg", "Uống", "Ức chế DPP-4");
            initMedicine("GLYBURIDE-METFORMIN", "Glyburide Metformin", "Glyburide-Metformin", "Glucovance", "Viên nén", "2.5/500mg", "Uống", "Dạng phối hợp cố định Sulfonylurea + Biguanide");
            initMedicine("GLIPIZIDE-METFORMIN", "Glipizide Metformin", "Glipizide-Metformin", "Metaglip", "Viên nén", "2.5/500mg", "Uống", "Dạng phối hợp cố định Sulfonylurea + Biguanide");
            initMedicine("GLIMEPIRIDE-PIOGLITAZONE", "Glimepiride Pioglitazone", "Glimepiride-Pioglitazone", "Duetact", "Viên nén", "2/30mg", "Uống", "Dạng phối hợp cố định Sulfonylurea + TZD");
            initMedicine("METFORMIN-ROSIGLITAZONE", "Metformin Rosiglitazone", "Metformin-Rosiglitazone", "Avandamet", "Viên nén", "500/2mg", "Uống", "Dạng phối hợp cố định Biguanide + TZD");
            initMedicine("METFORMIN-PIOGLITAZONE", "Metformin Pioglitazone", "Metformin-Pioglitazone", "Actoplus Met", "Viên nén", "500/15mg", "Uống", "Dạng phối hợp cố định Biguanide + TZD");
            // Thuốc điều trị tim mạch / bệnh nền phối hợp
            initMedicine("ASPIRIN", "Aspirin", "Aspirin", "Aspilets", "Viên nén", "81mg", "Uống", "Kháng kết tập tiểu cầu phòng biến chứng tim mạch");
            initMedicine("ATORVASTATIN", "Atorvastatin", "Atorvastatin", "Lipitor", "Viên nén", "20mg", "Uống", "Hạ lipid máu ở bệnh nhân tiểu đường");
            initMedicine("LISINOPRIL", "Lisinopril", "Lisinopril", "Zestril", "Viên nén", "10mg", "Uống", "Ức chế men chuyển bảo vệ thận tiểu đường");
            log.info("Đã khởi tạo đầy đủ 26 danh mục thuốc chuẩn hóa!");
        }

        // 6. Demo Drug Interactions (Cơ sở tri thức tương tác thuốc minh họa)
        if (drugInteractionRepository.count() == 0) {
            initInteraction("Metformin", "Insulin", "Moderate", "Phối hợp làm tăng hiệu lực hạ đường huyết. Cần theo dõi đường huyết chặt chẽ và điều chỉnh liều insulin phù hợp.", "Demo interaction knowledge base (DailyMed ref)");
            initInteraction("Glyburide", "Metformin", "Moderate", "Phối hợp sulfonylurea và biguanide có thể làm tăng nguy cơ hạ đường huyết, đặc biệt khi bỏ bữa.", "Demo interaction knowledge base (DailyMed ref)");
            initInteraction("Glipizide", "Insulin", "Major", "Tăng nguy cơ hạ đường huyết nghiêm trọng khi dùng đồng thời sulfonylurea liều cao với insulin ngoại sinh.", "Demo interaction knowledge base (DailyMed ref)");
            initInteraction("Repaglinide", "Insulin", "Moderate", "Tăng nguy cơ hạ đường huyết cấp tính do đồng kích thích tiết insulin và bổ sung insulin.", "Demo interaction knowledge base (DailyMed ref)");
            initInteraction("Pioglitazone", "Insulin", "Moderate", "Phối hợp TZD với insulin làm tăng nguy cơ giữ dịch phù nề và suy tim sung huyết.", "Demo interaction knowledge base (DailyMed ref)");
            initInteraction("Rosiglitazone", "Insulin", "Major", "Chống chỉ định phối hợp ở bệnh nhân có tiền sử suy tim do tăng giữ dịch quá mức.", "Demo interaction knowledge base (DailyMed ref)");
            log.info("Đã khởi tạo cơ sở tri thức tương tác thuốc demo (Demo Drug Interaction Knowledge Base)!");
        }

        // 7. Patients
        if (patientRepository.count() == 0) {
            Patient p1 = patientRepository.save(Patient.builder()
                    .patientCode("PAT001")
                    .fullName("Nguyễn Văn Hùng")
                    .gender("Nam")
                    .dateOfBirth(LocalDate.of(1958, 4, 12))
                    .identityCard("001058012345")
                    .phone("0934567890")
                    .insuranceNumber("BHYT-79-001")
                    .address("120 Hai Bà Trưng, Quận 1, TP.HCM")
                    .build());

            Patient p2 = patientRepository.save(Patient.builder()
                    .patientCode("PAT002")
                    .fullName("Lê Thị Thu Thảo")
                    .gender("Nữ")
                    .dateOfBirth(LocalDate.of(1964, 8, 25))
                    .identityCard("001064023456")
                    .phone("0945678901")
                    .insuranceNumber("BHYT-01-002")
                    .address("45 Lê Duẩn, Ba Đình, Hà Nội")
                    .build());

            Doctor doc = doctorRepository.findByDoctorCode("DOC001").orElse(null);
            Diagnosis d1 = diagnosisRepository.findByIcdCode("250.00").orElse(null);
            Diagnosis d2 = diagnosisRepository.findByIcdCode("250.02").orElse(null);

            if (doc != null) {
                medicalVisitRepository.save(MedicalVisit.builder()
                        .visitCode("VIS2026-001")
                        .patient(p1)
                        .doctor(doc)
                        .visitDate(LocalDateTime.now().minusDays(1))
                        .initialDiagnosis(d1)
                        .symptoms("Khát nước nhiều, sụt cân nhẹ, chỉ số HbA1c 8.2%")
                        .clinicalNotes("Tiền sử đái tháo đường type 2 năm thứ 5, đang dùng Metformin đơn độc")
                        .status("ACTIVE")
                        .build());

                medicalVisitRepository.save(MedicalVisit.builder()
                        .visitCode("VIS2026-002")
                        .patient(p2)
                        .doctor(doc)
                        .visitDate(LocalDateTime.now().minusHours(3))
                        .initialDiagnosis(d2)
                        .symptoms("Đường huyết đói dao động 11-13 mmol/L, mệt mỏi kéo dài")
                        .clinicalNotes("Thất bại kiểm soát với thuốc uống đơn thuần, cần cân nhắc phối hợp insulin")
                        .status("ACTIVE")
                        .build());
            }
        }

        // 8. Tự động nạp bộ dữ liệu chuẩn UCI Diabetes và Khai phá Luật ban đầu (nếu được kích hoạt)
        if (autoMiningEnabled) {
            try {
                log.info("Cấu hình app.seed.auto-mining=true: Tự động nạp bộ dữ liệu chuẩn UCI Diabetes 130-US Hospitals...");
                datasetImportService.importUciDiabetesDataset();

                if (transactionService.hasTransactions()) {
                    List<Set<String>> transactions = transactionService.getTransactions();
                    log.info("Đã nạp thành công {} transactions từ dữ liệu UCI Diabetes!", transactions.size());

                    MiningParameters defaultParams = MiningParameters.builder()
                            .minSupport(0.01)
                            .minConfidence(0.30)
                            .minLift(1.0)
                            .maxItemsetSize(5)
                            .build();

                    // 1. Chạy Apriori và lưu kết quả
                    MiningResult aprioriRes = aprioriMiningService.mine(transactions, defaultParams);
                    miningRunService.saveMiningRun(aprioriRes, defaultParams, "UCI Diabetes 130-US Hospitals");

                    // 2. Chạy FP-Growth và lưu kết quả
                    MiningResult fpRes = fpGrowthMiningService.mine(transactions, defaultParams);
                    miningRunService.saveMiningRun(fpRes, defaultParams, "UCI Diabetes 130-US Hospitals");

                    // 3. Chạy Benchmark so sánh
                    miningEvaluationService.runBenchmark(transactions, defaultParams, "UCI Diabetes 130-US Hospitals");
                    log.info("Khởi tạo Data Mining tự động thành công!");
                }
            } catch (Exception e) {
                log.warn("Không thể tự động chạy khai phá ban đầu: {}", e.getMessage(), e);
            }
        } else {
            log.info("Khởi tạo master data hoàn tất! Chế độ auto-mining tắt (app.seed.auto-mining=false). Các tác vụ Data Mining sẽ thực thi theo yêu cầu trên Admin UI hoặc REST API.");
        }
    }

    private void initMedicine(String code, String name, String genericName, String brandName,
                              String form, String strength, String route, String desc) {
        if (medicineRepository.findByDrugCode(code).isEmpty()) {
            medicineRepository.save(Medicine.builder()
                    .drugCode(code)
                    .genericName(genericName)
                    .displayName(name)
                    .brandName(brandName)
                    .dosageForm(form)
                    .strength(strength)
                    .route(route)
                    .description(desc)
                    .active(true)
                    .build());
        }
    }

    private void initInteraction(String drugA, String drugB, String severity, String description, String source) {
        if (drugInteractionRepository.findInteractionsBetween(drugA, drugB).isEmpty()) {
            drugInteractionRepository.save(DrugInteraction.builder()
                    .drugNameA(drugA)
                    .drugNameB(drugB)
                    .interactionStatus("KNOWN")
                    .severity(severity)
                    .description(description)
                    .source(source)
                    .build());
        }
    }
}
