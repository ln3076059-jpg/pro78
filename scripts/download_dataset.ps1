# =============================================================================
# Script tai bo du lieu UCI Diabetes 130-US Hospitals (1999-2008)
# Nguon chinh thuc (Primary): UCI Machine Learning Repository (Dataset ID 296)
# URL: https://archive.ics.uci.edu/dataset/296/diabetes+130-us+hospitals+for+years+1999-2008
# Mirror du phong (Fallback): GitHub repository mirror
# =============================================================================

$datasetDir = Join-Path $PSScriptRoot "..\dataset"
if (-not (Test-Path $datasetDir)) {
    New-Item -ItemType Directory -Path $datasetDir -Force | Out-Null
}

$dataFile = Join-Path $datasetDir "diabetic_data.csv"
$mappingFile = Join-Path $datasetDir "IDs_mapping.csv"
$zipFile = Join-Path $datasetDir "dataset_uci.zip"

$uciZipUrl = "https://archive.ics.uci.edu/static/public/296/diabetes+130-us+hospitals+for+years+1999-2008.zip"
$fallbackDataUrl = "https://raw.githubusercontent.com/andrewwlong/diabetes_readmission/master/diabetic_data.csv"
$fallbackMapUrl = "https://raw.githubusercontent.com/andrewwlong/diabetes_readmission/master/IDs_mapping.csv"

Write-Host "===================================================================" -ForegroundColor Cyan
Write-Host "=== TAI BO DU LIEU UCI DIABETES 130-US HOSPITALS (1999-2008)    ===" -ForegroundColor Cyan
Write-Host "===================================================================" -ForegroundColor Cyan

# Kiem tra neu tep da ton tai va day du dung luong
if ((Test-Path $dataFile) -and ((Get-Item $dataFile).Length -gt 15000000) -and (Test-Path $mappingFile)) {
    $dataLen = (Get-Item $dataFile).Length
    $mapLen = (Get-Item $mappingFile).Length
    Write-Host "[OK] Bo du lieu da ton tai day du tai dataset/:" -ForegroundColor Green
    Write-Host "  - diabetic_data.csv ($dataLen bytes, 101,766 ban ghi)" -ForegroundColor Green
    Write-Host "  - IDs_mapping.csv ($mapLen bytes)" -ForegroundColor Green
    exit 0
}

Write-Host "[1/3] Dang tai tu Nguon chinh thuc: UCI Machine Learning Repository..." -ForegroundColor Yellow
Write-Host "  URL: $uciZipUrl" -ForegroundColor Gray

$downloadSuccess = $false
try {
    Invoke-WebRequest -Uri $uciZipUrl -OutFile $zipFile -UseBasicParsing -TimeoutSec 60
    Write-Host "  -> Da tai file ZIP tu UCI ($((Get-Item $zipFile).Length) bytes). Dang giai nen..." -ForegroundColor Yellow
    
    Expand-Archive -Path $zipFile -DestinationPath $datasetDir -Force
    
    $extractedFolder = Join-Path $datasetDir "dataset_diabetes"
    if (Test-Path $extractedFolder) {
        $innerData = Join-Path $extractedFolder "diabetic_data.csv"
        $innerMap = Join-Path $extractedFolder "IDs_mapping.csv"
        if (Test-Path $innerData) { Move-Item -Path $innerData -Destination $dataFile -Force }
        if (Test-Path $innerMap) { Move-Item -Path $innerMap -Destination $mappingFile -Force }
        Remove-Item -Path $extractedFolder -Recurse -Force
    }
    
    if (Test-Path $zipFile) { Remove-Item -Path $zipFile -Force }
    
    if ((Test-Path $dataFile) -and ((Get-Item $dataFile).Length -gt 15000000)) {
        $downloadSuccess = $true
        Write-Host "  -> Giai nen thanh cong bo du lieu goc tu UCI!" -ForegroundColor Green
    }
} catch {
    Write-Host "  -> Khong the tai tu UCI ($($_.Exception.Message))." -ForegroundColor Yellow
}

# Neu UCI fail, dung fallback GitHub mirror
if (-not $downloadSuccess) {
    Write-Host "[CANH BAO] Chuyen sang Mirror du phong (GitHub repository)..." -ForegroundColor Yellow
    Write-Host "  Tai diabetic_data.csv..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri $fallbackDataUrl -OutFile $dataFile -UseBasicParsing
    Write-Host "  Tai IDs_mapping.csv..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri $fallbackMapUrl -OutFile $mappingFile -UseBasicParsing
    if (Test-Path $zipFile) { Remove-Item -Path $zipFile -Force }
}

if ((Test-Path $dataFile) -and ((Get-Item $dataFile).Length -gt 15000000)) {
    $dataLen = (Get-Item $dataFile).Length
    Write-Host "[THANH CONG] Da chuan bi bo du lieu UCI Diabetes 130-US Hospitals:" -ForegroundColor Green
    Write-Host "  - diabetic_data.csv: $dataLen bytes (101,766 ban ghi)" -ForegroundColor Green
    Write-Host "  - IDs_mapping.csv: $((Get-Item $mappingFile).Length) bytes" -ForegroundColor Green
} else {
    Write-Host "[LOI] Khong the tai du lieu. Vui long kiem tra ket noi mang." -ForegroundColor Red
    exit 1
}
