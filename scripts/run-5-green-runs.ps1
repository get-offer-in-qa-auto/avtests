# 5 green runs + Allure report with history + save each report to allure-reports/run-N
# BEFORE: cd infra && docker compose up -d
# Uses: UIREMOTE=local, backend:4111, frontend:3000

$ErrorActionPreference = "Stop"

$ReportsDir = "allure-reports"
New-Item -ItemType Directory -Force -Path $ReportsDir | Out-Null

$env:UIREMOTE = "local"
$env:APIBASEURL = "http://localhost:4111"
$env:UIBASEURL = "http://localhost:3000"

for ($i = 1; $i -le 5; $i++) {
    Write-Host ""
    Write-Host "========== Run $i of 5 ==========" -ForegroundColor Cyan
    mvn clean test -P ci -DforkCount=0 -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Run $i failed." -ForegroundColor Red
        exit 1
    }

    Write-Host "Generating report..." -ForegroundColor Green
    & "$PSScriptRoot\allure-report-with-history.ps1"

    $runReport = "$ReportsDir/run-$i"
    if (Test-Path "target/site/allure-maven-plugin") {
        Remove-Item $runReport -Recurse -Force -ErrorAction SilentlyContinue
        Copy-Item -Path "target/site/allure-maven-plugin" -Destination $runReport -Recurse
        Write-Host "Saved: $runReport" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "Done. Reports: allure-reports/run-1 ... run-5" -ForegroundColor Green
Write-Host "Open: start allure-reports/run-5/index.html" -ForegroundColor Yellow
