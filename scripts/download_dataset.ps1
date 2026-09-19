# Script tai bo du lieu UCI Diabetes 130-US Hospitals (1999-2008)
# Nguon: UCI Machine Learning Repository (Dataset ID 296)

$datasetDir = Join-Path $PSScriptRoot "..\dataset"
if (-not (Test-Path $datasetDir)) {
    New-Item -ItemType Directory -Path $datasetDir -Force | Out-Null
}

$dataFile = Join-Path $datasetDir "diabetic_data.csv"
$mappingFile = Join-Path $datasetDir "IDs_mapping.csv"

Write-Host "=== BAT DAU KIEM TRA VA TAI BO DU LIEU UCI DIABETES 130-US HOSPITALS ===" -ForegroundColor Cyan

# 1. Kiem tra va tai diabetic_data.csv
if (-not (Test-Path $dataFile) -or ((Get-Item $dataFile).Length -lt 10000000)) {
    Write-Host "[1/2] Dang tai diabetic_data.csv (19.1 MB, 101,766 ban ghi)..." -ForegroundColor Yellow
    $url = "https://raw.githubusercontent.com/andrewwlong/diabetes_readmission/master/diabetic_data.csv"
    Invoke-WebRequest -Uri $url -OutFile $dataFile -UseBasicParsing
    $size = (Get-Item $dataFile).Length
    Write-Host "  -> Da tai thanh cong: $dataFile ($size bytes)" -ForegroundColor Green
} else {
    $size = (Get-Item $dataFile).Length
    Write-Host "[1/2] diabetic_data.csv da ton tai ($size bytes, 101,766 ban ghi)." -ForegroundColor Green
}

# 2. Kiem tra va tai IDs_mapping.csv
if (-not (Test-Path $mappingFile)) {
    Write-Host "[2/2] Dang tai IDs_mapping.csv..." -ForegroundColor Yellow
    $mapUrl = "https://raw.githubusercontent.com/andrewwlong/diabetes_readmission/master/IDs_mapping.csv"
    Invoke-WebRequest -Uri $mapUrl -OutFile $mappingFile -UseBasicParsing
    Write-Host "  -> Da tai thanh cong: $mappingFile" -ForegroundColor Green
} else {
    Write-Host "[2/2] IDs_mapping.csv da ton tai." -ForegroundColor Green
}

Write-Host "=== HOAN TAT! BO DU LIEU DA SAN SANG TAI THU MUC dataset/ ===" -ForegroundColor Cyan
