# Добавляет Docker Hub секреты в GitHub для пайплайна build-and-push-docker.
# Требует: gh cli (winget install GitHub.cli)
#
# Использование:
#   .\scripts\setup-docker-secrets.ps1 -Username zeeero -Token "dckr_pat_..."
# Или: $env:DOCKERHUB_USERNAME="zeeero"; $env:DOCKERHUB_TOKEN="..."; .\scripts\setup-docker-secrets.ps1

param(
    [string]$Username = $env:DOCKERHUB_USERNAME,
    [string]$Token = $env:DOCKERHUB_TOKEN
)

if (-not $Username) { $Username = Read-Host "Docker Hub username" }
if (-not $Token) { $Token = Read-Host "Docker Hub token (Access Token)" -AsSecureString; $Token = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($Token)) }

if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    Write-Host "Установите GitHub CLI: winget install GitHub.cli"
    Write-Host ""
    Write-Host "Либо добавьте секреты вручную:"
    Write-Host "  https://github.com/YOUR_ORG/avtests/settings/secrets/actions"
    Write-Host "  DOCKERHUB_USERNAME = $Username"
    Write-Host "  DOCKERHUB_TOKEN   = (ваш токен)"
    exit 1
}

$Username | gh secret set DOCKERHUB_USERNAME
$Token | gh secret set DOCKERHUB_TOKEN
Write-Host "Secrets added."
