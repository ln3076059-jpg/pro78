package com.hospital.datamining.service.preprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service chuẩn hóa tên thuốc chuyên biệt cho hệ thống Data Mining y tế.
 * Giải quyết triệt để bài toán biến thể tên thuốc (Aspirin, aspirin, ASPIRIN, Aspirin 81mg...)
 * nhằm gom các bản ghi về cùng một item danh mục duy nhất trước khi khai phá luật kết hợp.
 */
@Service
public class DrugNormalizationService {

    private static final Logger log = LoggerFactory.getLogger(DrugNormalizationService.class);

    // Bộ nhớ đệm lưu ánh xạ: originalName -> normalizedName
    private final Map<String, String> originalToNormalizedCache = new ConcurrentHashMap<>();

    // Regex phát hiện và loại bỏ các định lượng liều (e.g., 81mg, 500 mg, 100iu, 0.5%, 10meq/ml, 5000units...)
    private static final Pattern DOSAGE_PATTERN = Pattern.compile(
            "\\b\\d+(\\.\\d+)?\\s*(mg|mcg|ug|g|ml|l|iu|meq|units?|unit|%|tablets?|tabs?|caps?|capsules?|pills?|amp|vials?|vien|goi|chai|ong)\\b(/[a-zA-Z0-9]+)?",
            Pattern.CASE_INSENSITIVE
    );

    // Regex phát hiện các dạng bào chế / đường dùng thừa trong ngoặc hoặc ở đuôi tên thuốc
    private static final Pattern ROUTE_OR_FORM_PATTERN = Pattern.compile(
            "\\b(oral|iv|im|sc|po|ec|er|xr|cr|sr|hcl|sodium|potassium|tablet|capsule|injection|solution|suspension|cream|ointment|drops?|spray)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Regex xóa các cụm đóng mở ngoặc e.g. (81mg), (Oral), [Aspilets]
    private static final Pattern PARENTHESES_PATTERN = Pattern.compile("\\s*\\([^)]*\\)|\\s*\\[[^]]*\\]");

    public DrugNormalizationService() {
        // Đăng ký trước một số thuốc tiêu biểu trong MIMIC-III
        registerCanonical("Aspirin");
        registerCanonical("Metoprolol");
        registerCanonical("Heparin");
        registerCanonical("Pantoprazole");
        registerCanonical("Atorvastatin");
        registerCanonical("Furosemide");
        registerCanonical("Potassium Chloride");
        registerCanonical("Lisinopril");
        registerCanonical("Insulin");
        registerCanonical("Metformin");
        registerCanonical("Acetaminophen");
    }

    /**
     * Chuẩn hóa tên thuốc ưu tiên genericName, nếu rỗng thì fallback sang drugName
     *
     * @param genericName Tên gốc / hoạt chất (DRUG_NAME_GENERIC)
     * @param drugName Tên thuốc thương mại / ghi nhận (DRUG)
     * @return Tên thuốc chuẩn hóa (Title Case, đã loại bỏ hàm lượng liều)
     */
    public String normalize(String genericName, String drugName) {
        String primary = (genericName != null && !genericName.trim().isEmpty() && !"NA".equalsIgnoreCase(genericName.trim()))
                ? genericName : drugName;
        return normalize(primary);
    }

    /**
     * Chuẩn hóa một chuỗi tên thuốc bất kỳ
     *
     * @param raw Tên thuốc thô ban đầu
     * @return Tên thuốc đã chuẩn hóa
     */
    public String normalize(String raw) {
        if (raw == null) {
            return "";
        }

        String rawTrimmed = raw.trim();
        if (rawTrimmed.isEmpty() || "NA".equalsIgnoreCase(rawTrimmed)) {
            return "";
        }

        // Kiểm tra bộ nhớ đệm
        if (originalToNormalizedCache.containsKey(rawTrimmed)) {
            return originalToNormalizedCache.get(rawTrimmed);
        }

        String clean = rawTrimmed;

        // 1. Loại bỏ dấu ngoặc kép bọc ngoài
        if (clean.startsWith("\"") && clean.endsWith("\"") && clean.length() > 1) {
            clean = clean.substring(1, clean.length() - 1).trim();
        }

        // 2. Loại bỏ nội dung trong ngoặc đơn / ngoặc vuông
        clean = PARENTHESES_PATTERN.matcher(clean).replaceAll(" ");

        // 3. Loại bỏ hàm lượng liều (81mg, 500mg, 100iu...)
        clean = DOSAGE_PATTERN.matcher(clean).replaceAll(" ");

        // 4. Loại bỏ các ký tự phân cách thừa (dấu gạch ngang, phẩy, chấm phẩy)
        clean = clean.replaceAll("[,;/*+\\-]", " ");

        // 5. Chuẩn hóa khoảng trắng và chuyển sang Title Case
        String[] words = clean.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String w : words) {
            if (w.isEmpty()) continue;
            // Bỏ qua các từ chỉ liều lẻ hoặc ký hiệu đơn lẻ
            if (w.matches("^\\d+$") || w.length() == 1 && !Character.isLetter(w.charAt(0))) {
                continue;
            }

            // Kiểm tra nếu là từ phụ (ví dụ sodium, hcl, oral...) khi đã có ít nhất 1 từ chính
            if (sb.length() > 0 && ROUTE_OR_FORM_PATTERN.matcher(w).matches()) {
                // Nếu thuốc là Sodium Chloride hoặc Potassium Chloride thì giữ Sodium/Potassium
                String current = sb.toString().trim().toLowerCase();
                if (!("sodium".equalsIgnoreCase(w) || "potassium".equalsIgnoreCase(w))) {
                    continue;
                }
            }

            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(Character.toUpperCase(w.charAt(0)));
            if (w.length() > 1) {
                sb.append(w.substring(1).toLowerCase());
            }
        }

        String normalized = sb.toString().trim();

        // 6. Xử lý trường hợp đặc biệt về danh mục chuẩn hóa
        normalized = applyCanonicalRules(normalized);

        if (normalized.isEmpty()) {
            normalized = toTitleCase(rawTrimmed);
        }

        // Lưu vào cache
        originalToNormalizedCache.put(rawTrimmed, normalized);
        return normalized;
    }

    private String applyCanonicalRules(String name) {
        String lower = name.toLowerCase();

        if (lower.startsWith("aspirin")) return "Aspirin";
        if (lower.startsWith("metoprolol")) return "Metoprolol";
        if (lower.startsWith("heparin")) return "Heparin";
        if (lower.startsWith("pantoprazole")) return "Pantoprazole";
        if (lower.startsWith("atorvastatin")) return "Atorvastatin";
        if (lower.startsWith("furosemide")) return "Furosemide";
        if (lower.startsWith("potassium chloride")) return "Potassium Chloride";
        if (lower.startsWith("lisinopril")) return "Lisinopril";
        if (lower.startsWith("insulin")) return "Insulin";
        if (lower.startsWith("metformin")) return "Metformin";
        if (lower.startsWith("acetaminophen") || lower.startsWith("paracetamol")) return "Acetaminophen";
        if (lower.startsWith("omeprazole")) return "Omeprazole";
        if (lower.startsWith("clopidogrel")) return "Clopidogrel";
        if (lower.startsWith("vancomycin")) return "Vancomycin";
        if (lower.startsWith("warfarin")) return "Warfarin";

        return name;
    }

    private String toTitleCase(String text) {
        if (text == null || text.isEmpty()) return "";
        String[] parts = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty()) continue;
            if (i > 0) sb.append(" ");
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) {
                sb.append(p.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    public void registerCanonical(String canonicalName) {
        originalToNormalizedCache.put(canonicalName, canonicalName);
        originalToNormalizedCache.put(canonicalName.toLowerCase(), canonicalName);
        originalToNormalizedCache.put(canonicalName.toUpperCase(), canonicalName);
    }

    public void registerMapping(String original, String normalized) {
        originalToNormalizedCache.put(original, normalized);
    }

    public Map<String, String> getNormalizationCache() {
        return Map.copyOf(originalToNormalizedCache);
    }

    public int getCachedCount() {
        return originalToNormalizedCache.size();
    }
}
