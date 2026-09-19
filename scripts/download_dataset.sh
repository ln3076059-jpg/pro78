#!/bin/bash
# =============================================================================
# Script tải bộ dữ liệu UCI Diabetes 130-US Hospitals (1999-2008)
# Nguồn chính thức (Primary): UCI Machine Learning Repository (Dataset ID 296)
# URL: https://archive.ics.uci.edu/dataset/296/diabetes+130-us+hospitals+for+years+1999-2008
# Mirror dự phòng (Fallback): GitHub repository mirror
# =============================================================================

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
DATASET_DIR="$DIR/../dataset"
mkdir -p "$DATASET_DIR"

DATA_FILE="$DATASET_DIR/diabetic_data.csv"
MAPPING_FILE="$DATASET_DIR/IDs_mapping.csv"
ZIP_FILE="$DATASET_DIR/dataset_uci.zip"

UCI_ZIP_URL="https://archive.ics.uci.edu/static/public/296/diabetes+130-us+hospitals+for+years+1999-2008.zip"
FALLBACK_DATA_URL="https://raw.githubusercontent.com/andrewwlong/diabetes_readmission/master/diabetic_data.csv"
FALLBACK_MAP_URL="https://raw.githubusercontent.com/andrewwlong/diabetes_readmission/master/IDs_mapping.csv"

echo "==================================================================="
echo "=== TẢI BỘ DỮ LIỆU UCI DIABETES 130-US HOSPITALS (1999-2008)   ==="
echo "==================================================================="

# Kiểm tra xem cả 2 tệp đã tồn tại và đủ kích thước chưa
if [ -f "$DATA_FILE" ] && [ $(wc -c < "$DATA_FILE") -gt 15000000 ] && [ -f "$MAPPING_FILE" ]; then
    echo "[OK] Bộ dữ liệu đã tồn tại đầy đủ tại dataset/:"
    echo "  - diabetic_data.csv ($(wc -c < "$DATA_FILE") bytes, $(wc -l < "$DATA_FILE") dòng)"
    echo "  - IDs_mapping.csv ($(wc -c < "$MAPPING_FILE") bytes, $(wc -l < "$MAPPING_FILE") dòng)"
    exit 0
fi

echo "[1/3] Đang tải từ Nguồn chính thức: UCI Machine Learning Repository..."
echo "  URL: $UCI_ZIP_URL"

DOWNLOAD_SUCCESS=0
if curl -fsSL "$UCI_ZIP_URL" -o "$ZIP_FILE"; then
    echo "  -> Tải thành công tệp ZIP từ UCI. Đang giải nén..."
    if command -v unzip >/dev/null 2>&1; then
        unzip -q -o "$ZIP_FILE" -d "$DATASET_DIR"
        # Xử lý nếu thư mục con dataset_diabetes được sinh ra
        if [ -f "$DATASET_DIR/dataset_diabetes/diabetic_data.csv" ]; then
            mv "$DATASET_DIR/dataset_diabetes/diabetic_data.csv" "$DATA_FILE"
            mv "$DATASET_DIR/dataset_diabetes/IDs_mapping.csv" "$MAPPING_FILE"
            rm -rf "$DATASET_DIR/dataset_diabetes"
        fi
        rm -f "$ZIP_FILE"
        if [ -f "$DATA_FILE" ] && [ $(wc -c < "$DATA_FILE") -gt 15000000 ]; then
            DOWNLOAD_SUCCESS=1
            echo "  -> Giải nén thành công dữ liệu gốc UCI!"
        fi
    fi
fi

# Nếu tải từ UCI không thành công, chuyển sang Fallback Mirror
if [ $DOWNLOAD_SUCCESS -eq 0 ]; then
    echo "[CẢNH BÁO] Không thể tải hoặc giải nén từ UCI. Chuyển sang Mirror dự phòng (GitHub)..."
    echo "  Tải diabetic_data.csv..."
    curl -fsSL "$FALLBACK_DATA_URL" -o "$DATA_FILE"
    echo "  Tải IDs_mapping.csv..."
    curl -fsSL "$FALLBACK_MAP_URL" -o "$MAPPING_FILE"
    rm -f "$ZIP_FILE"
fi

# Xác thực kết quả cuối cùng
if [ -f "$DATA_FILE" ] && [ $(wc -c < "$DATA_FILE") -gt 15000000 ]; then
    echo "[THÀNH CÔNG] Đã chuẩn bị bộ dữ liệu UCI Diabetes 130-US Hospitals:"
    echo "  - diabetic_data.csv: $(wc -c < "$DATA_FILE") bytes, $(wc -l < "$DATA_FILE") dòng"
    echo "  - IDs_mapping.csv: $(wc -c < "$MAPPING_FILE") bytes"
else
    echo "[LỖI] Tải dữ liệu thất bại. Vui lòng kiểm tra kết nối mạng."
    exit 1
fi
