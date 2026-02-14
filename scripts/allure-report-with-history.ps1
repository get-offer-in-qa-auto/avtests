# Генерация Allure-отчёта с сохранением истории прошлых прогонов.
# Использование: .\scripts\allure-report-with-history.ps1
# Предварительно: mvn test -P ci (или mvn clean test -P ci)

$ErrorActionPreference = "Stop"

$AllureResults = "target/allure-results"
$AllureReport = "target/site/allure-maven-plugin"
$HistoryDir = "allure-history"

# Копируем history из предыдущего отчёта в allure-results
if (Test-Path $HistoryDir) {
    Write-Host "Restoring Allure history from $HistoryDir..."
    New-Item -ItemType Directory -Force -Path "$AllureResults/history" | Out-Null
    Copy-Item -Path "$HistoryDir/*" -Destination "$AllureResults/history/" -Recurse -Force
}

# Генерируем отчёт
mvn allure:report -q

# Сохраняем history для следующего прогона
if (Test-Path "$AllureReport/history") {
    Write-Host "Saving Allure history to $HistoryDir..."
    New-Item -ItemType Directory -Force -Path $HistoryDir | Out-Null
    Copy-Item -Path "$AllureReport/history/*" -Destination "$HistoryDir/" -Recurse -Force
}

Write-Host "Report generated: $AllureReport"
Write-Host "Open via HTTP (file:// won't load widgets!):"
Write-Host "  cd $AllureReport && python -m http.server 8765"
Write-Host "  then open http://localhost:8765/"
Write-Host "Or: mvn allure:serve (from project root)"
