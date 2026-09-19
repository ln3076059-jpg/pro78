#!/bin/bash
# Script tải bộ dữ liệu UCI Diabetes 130-US Hospitals (1999-2008)
# Nguồn: UCI Machine Learning Repository (Dataset ID 296)

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
DATASET_DIR="$DIR/../dataset"
mkdir -p "$DATASET_DIR"

DATA_FILE="$DATASET_DIR/diabetic_data.csv"
MAPPING_FILE="$DATASET_DIR/IDs_mapping.csv"

echo "=== BẮT ĐẦU TẢI BỘ DỮ LIỆU UCI DIABETES 130-US HOSPITALS ==="

if [ ! -f "$DATA_FILE" ] || [ $(wc -c < "$DATA_FILE") -lt 10000000 ]; then
    echo "[1/2] Đang tải diabetic_data.csv (19.1 MB, 101,766 bản ghi)..."
    curl -L "https://raw.githubusercontent.com/andrewwlong/diabetes_readmission/master/diabetic_data.csv" -o "$DATA_FILE"
    echo "  -> Đã tải thành công diabetic_data.csv"
else
    echo "[1/2] diabetic_data.csv đã tồn tại."
fi

if [ ! -f "$MAPPING_FILE" ]; then
    echo "[2/2] Đang tải IDs_mapping.csv..."
    curl -L "https://raw.githubusercontent.com/andrewwlong/diabetes_readmission/master/IDs_mapping.csv" -o "$MAPPING_FILE"
    echo "  -> Đã tải thành công IDs_mapping.csv"
else
    echo "[2/2] IDs_mapping.csv đã tồn tại."
fi

echo "=== HOÀN TẤT! BỘ DỮ LIỆU ĐÃ SẴN SÀNG TẠI THƯ MỤC dataset/ ==="
