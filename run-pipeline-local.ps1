# Локальный прогон пайплайна run-tests (API тесты).
# Полный прогон с UI требует Selenoid с совместимой версией Docker API.
param(
    [switch]$ApiOnly = $true
)

$ErrorActionPreference = "Continue"
$ProjectRoot = $PSScriptRoot
$InfraDir = Join-Path $ProjectRoot "infra"

Write-Host ">>> 1. Stop containers" -ForegroundColor Yellow
Set-Location $InfraDir
docker compose down 2>&1 | Out-Null

Write-Host ">>> 2. Запуск сервисов (detached)" -ForegroundColor Yellow
docker compose up -d
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host ">>> 3. Waiting for backend (60s)..." -ForegroundColor Yellow
$maxWait = 60
for ($i = 0; $i -lt $maxWait; $i++) {
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:4111/actuator/health" -UseBasicParsing -TimeoutSec 3
        if ($r.StatusCode -eq 200) {
            Write-Host "Backend ready." -ForegroundColor Green
            break
        }
    } catch { }
    if ($i -eq $maxWait - 1) {
        Write-Host "Backend timeout" -ForegroundColor Red
        docker compose down
        exit 1
    }
    Start-Sleep -Seconds 1
}

Set-Location $ProjectRoot
$env:APIBASEURL = "http://localhost:4111"
$env:UIBASEURL = "http://localhost:3000"

Write-Host ">>> 4. Запуск тестов" -ForegroundColor Yellow
if ($ApiOnly) {
    mvn clean test -P api -q
} else {
    mvn clean test -q
}
$testExitCode = $LASTEXITCODE

Write-Host ">>> 5. Stop services" -ForegroundColor Yellow
Set-Location $InfraDir
docker compose down 2>&1 | Out-Null

Write-Host ">>> Done. Exit code: $testExitCode" -ForegroundColor $(if ($testExitCode -eq 0) { "Green" } else { "Red" })
exit $testExitCode
