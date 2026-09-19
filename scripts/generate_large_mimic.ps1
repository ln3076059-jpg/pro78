# Generator for realistic MIMIC-III PRESCRIPTIONS dataset with 1,200+ rows
$outputPath = Join-Path $PSScriptRoot "..\demo-data\PRESCRIPTIONS_large_mimic.csv"

$header = "ROW_ID,SUBJECT_ID,HADM_ID,STARTDATE,ENDDATE,DRUG_TYPE,DRUG,DRUG_NAME_GENERIC,FORMULARY_DRUG_CD,GSN,NDC,PROD_STRENGTH,DOSE_VAL_RX,DOSE_UNIT_RX,FORM_VAL_DISP,FORM_UNIT_DISP,ROUTE"
$lines = [System.Collections.Generic.List[string]]::new()
$lines.Add($header)

$rowId = 1000

# Archetypal clinical regimens observed in MIMIC-III ICU / Inpatient wards:
# 1. Acute Coronary Syndrome (ACS / MI): Aspirin, Metoprolol, Heparin, Pantoprazole, Atorvastatin, Clopidogrel, Lisinopril
# 2. Sepsis / Severe Bacterial Infection: Vancomycin, Piperacillin/Tazobactam, Cefepime, Potassium Chloride, Pantoprazole
# 3. Heart Failure / Fluid Overload: Furosemide, Potassium Chloride, Lisinopril, Metoprolol, Spironolactone
# 4. Post-Operative Pain & Nausea: Acetaminophen, Hydromorphone, Ondansetron, Docusate Sodium, Pantoprazole
# 5. COPD / Severe Asthma Exacerbation: Albuterol, Ipratropium, Methylprednisolone, Azithromycin
# 6. Diabetic Inpatient Care: Insulin Regular, Insulin Glargine, Metformin, Dextrose 50%

$drugSpecs = @{
    "Aspirin" = @{ Generic="Aspirin"; Dose="81"; Unit="mg"; Route="PO"; Gsn="004383"; Formulary="ASA81" }
    "Metoprolol" = @{ Generic="Metoprolol Tartrate"; Dose="25"; Unit="mg"; Route="PO"; Gsn="019445"; Formulary="METO25" }
    "Heparin" = @{ Generic="Heparin Sodium"; Dose="5000"; Unit="UNIT"; Route="SC"; Gsn="000282"; Formulary="HEP5000" }
    "Pantoprazole" = @{ Generic="Pantoprazole Sodium"; Dose="40"; Unit="mg"; Route="IV"; Gsn="047321"; Formulary="PANT40IV" }
    "Atorvastatin" = @{ Generic="Atorvastatin Calcium"; Dose="40"; Unit="mg"; Route="PO"; Gsn="033501"; Formulary="ATOR40" }
    "Clopidogrel" = @{ Generic="Clopidogrel Bisulfate"; Dose="75"; Unit="mg"; Route="PO"; Gsn="040221"; Formulary="CLOP75" }
    "Lisinopril" = @{ Generic="Lisinopril"; Dose="10"; Unit="mg"; Route="PO"; Gsn="002131"; Formulary="LISI10" }
    "Vancomycin" = @{ Generic="Vancomycin HCl"; Dose="1000"; Unit="mg"; Route="IVPB"; Gsn="008912"; Formulary="VANC1G" }
    "Piperacillin/Tazobactam" = @{ Generic="Piperacillin-Tazobactam"; Dose="3.375"; Unit="g"; Route="IVPB"; Gsn="024102"; Formulary="ZOSYN3" }
    "Cefepime" = @{ Generic="Cefepime HCl"; Dose="1000"; Unit="mg"; Route="IVPB"; Gsn="032011"; Formulary="CEFE1G" }
    "Potassium Chloride" = @{ Generic="Potassium Chloride"; Dose="20"; Unit="mEq"; Route="PO"; Gsn="001402"; Formulary="KCL20" }
    "Furosemide" = @{ Generic="Furosemide"; Dose="40"; Unit="mg"; Route="IV"; Gsn="008101"; Formulary="FURO40IV" }
    "Spironolactone" = @{ Generic="Spironolactone"; Dose="25"; Unit="mg"; Route="PO"; Gsn="006212"; Formulary="SPIR25" }
    "Acetaminophen" = @{ Generic="Acetaminophen"; Dose="650"; Unit="mg"; Route="PO"; Gsn="004121"; Formulary="APAP650" }
    "Hydromorphone" = @{ Generic="Hydromorphone HCl"; Dose="1"; Unit="mg"; Route="IV"; Gsn="004211"; Formulary="DILA1MG" }
    "Ondansetron" = @{ Generic="Ondansetron HCl"; Dose="4"; Unit="mg"; Route="IV"; Gsn="021301"; Formulary="ZOFR4MG" }
    "Docusate Sodium" = @{ Generic="Docusate Sodium"; Dose="100"; Unit="mg"; Route="PO"; Gsn="004812"; Formulary="COLA100" }
    "Albuterol" = @{ Generic="Albuterol Inhalation Solution"; Dose="2.5"; Unit="mg"; Route="IH"; Gsn="012111"; Formulary="ALBU2.5" }
    "Ipratropium" = @{ Generic="Ipratropium Bromide"; Dose="0.5"; Unit="mg"; Route="IH"; Gsn="013211"; Formulary="ATRO0.5" }
    "Methylprednisolone" = @{ Generic="Methylprednisolone Sodium Succinate"; Dose="125"; Unit="mg"; Route="IV"; Gsn="007812"; Formulary="SOLU125" }
    "Azithromycin" = @{ Generic="Azithromycin"; Dose="500"; Unit="mg"; Route="IV"; Gsn="025112"; Formulary="ZITH500" }
    "Insulin Regular" = @{ Generic="Insulin Regular Human"; Dose="5"; Unit="UNIT"; Route="SC"; Gsn="002341"; Formulary="INSR5" }
    "Insulin Glargine" = @{ Generic="Insulin Glargine"; Dose="20"; Unit="UNIT"; Route="SC"; Gsn="046123"; Formulary="LANT20" }
    "Metformin" = @{ Generic="Metformin HCl"; Dose="500"; Unit="mg"; Route="PO"; Gsn="029811"; Formulary="METF500" }
    "Dextrose 50%" = @{ Generic="Dextrose 50%"; Dose="25"; Unit="g"; Route="IV"; Gsn="009811"; Formulary="D50W25" }
}

$rnd = [System.Random]::new(42)

for ($hadm = 10001; $hadm -le 10320; $hadm++) {
    $subjectId = $hadm - 9000
    
    # Pick 1-2 primary clinical patterns
    $patternChoice = $hadm % 6
    $selectedDrugs = [System.Collections.Generic.HashSet[string]]::new()
    
    switch ($patternChoice) {
        0 { # ACS
            $selectedDrugs.Add("Aspirin") | Out-Null
            $selectedDrugs.Add("Metoprolol") | Out-Null
            $selectedDrugs.Add("Pantoprazole") | Out-Null
            if ($rnd.NextDouble() -lt 0.85) { $selectedDrugs.Add("Heparin") | Out-Null }
            if ($rnd.NextDouble() -lt 0.75) { $selectedDrugs.Add("Atorvastatin") | Out-Null }
            if ($rnd.NextDouble() -lt 0.60) { $selectedDrugs.Add("Clopidogrel") | Out-Null }
            if ($rnd.NextDouble() -lt 0.40) { $selectedDrugs.Add("Lisinopril") | Out-Null }
        }
        1 { # Sepsis / Infection
            $selectedDrugs.Add("Vancomycin") | Out-Null
            $selectedDrugs.Add("Piperacillin/Tazobactam") | Out-Null
            $selectedDrugs.Add("Potassium Chloride") | Out-Null
            if ($rnd.NextDouble() -lt 0.70) { $selectedDrugs.Add("Pantoprazole") | Out-Null }
            if ($rnd.NextDouble() -lt 0.45) { $selectedDrugs.Add("Cefepime") | Out-Null }
            if ($rnd.NextDouble() -lt 0.50) { $selectedDrugs.Add("Acetaminophen") | Out-Null }
        }
        2 { # Heart Failure
            $selectedDrugs.Add("Furosemide") | Out-Null
            $selectedDrugs.Add("Potassium Chloride") | Out-Null
            if ($rnd.NextDouble() -lt 0.80) { $selectedDrugs.Add("Metoprolol") | Out-Null }
            if ($rnd.NextDouble() -lt 0.70) { $selectedDrugs.Add("Lisinopril") | Out-Null }
            if ($rnd.NextDouble() -lt 0.50) { $selectedDrugs.Add("Spironolactone") | Out-Null }
            if ($rnd.NextDouble() -lt 0.40) { $selectedDrugs.Add("Aspirin") | Out-Null }
        }
        3 { # Post-op Pain
            $selectedDrugs.Add("Acetaminophen") | Out-Null
            $selectedDrugs.Add("Hydromorphone") | Out-Null
            $selectedDrugs.Add("Ondansetron") | Out-Null
            if ($rnd.NextDouble() -lt 0.80) { $selectedDrugs.Add("Docusate Sodium") | Out-Null }
            if ($rnd.NextDouble() -lt 0.60) { $selectedDrugs.Add("Pantoprazole") | Out-Null }
        }
        4 { # COPD / Asthma
            $selectedDrugs.Add("Albuterol") | Out-Null
            $selectedDrugs.Add("Ipratropium") | Out-Null
            $selectedDrugs.Add("Methylprednisolone") | Out-Null
            if ($rnd.NextDouble() -lt 0.65) { $selectedDrugs.Add("Azithromycin") | Out-Null }
            if ($rnd.NextDouble() -lt 0.50) { $selectedDrugs.Add("Pantoprazole") | Out-Null }
        }
        5 { # Diabetes with comorbidity
            $selectedDrugs.Add("Insulin Regular") | Out-Null
            $selectedDrugs.Add("Insulin Glargine") | Out-Null
            if ($rnd.NextDouble() -lt 0.60) { $selectedDrugs.Add("Metformin") | Out-Null }
            if ($rnd.NextDouble() -lt 0.50) { $selectedDrugs.Add("Aspirin") | Out-Null }
            if ($rnd.NextDouble() -lt 0.40) { $selectedDrugs.Add("Atorvastatin") | Out-Null }
            if ($rnd.NextDouble() -lt 0.30) { $selectedDrugs.Add("Dextrose 50%") | Out-Null }
        }
    }
    
    # Random occasional background medication
    if ($rnd.NextDouble() -lt 0.25) { $selectedDrugs.Add("Pantoprazole") | Out-Null }
    if ($rnd.NextDouble() -lt 0.20) { $selectedDrugs.Add("Acetaminophen") | Out-Null }
    
    foreach ($d in $selectedDrugs) {
        $rowId++
        $spec = $drugSpecs[$d]
        $start = "2150-05-10 08:00:00"
        $end = "2150-05-15 18:00:00"
        $line = "$rowId,$subjectId,$hadm,$start,$end,MAIN,$d,$($spec.Generic),$($spec.Formulary),$($spec.Gsn),0000000000,1 tab,$($spec.Dose),$($spec.Unit),1,TAB,$($spec.Route)"
        $lines.Add($line)
    }
}

[System.IO.File]::WriteAllLines($outputPath, $lines, [System.Text.Encoding]::UTF8)
Write-Host "Generated $($lines.Count - 1) rows into $outputPath"
