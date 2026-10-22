# =============================================================================
#  run-local.ps1 - Chạy ứng dụng CineX ở local, tự nạp biến môi trường từ .env
#
#    .\run-local.ps1                # cổng 8080
#    .\run-local.ps1 -Port 8090     # đổi cổng
#
#  Yêu cầu: MySQL đã tạo sẵn database (xem README) và file .env đã điền key.
# =============================================================================
param(
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$envFile = Join-Path $projectDir ".env"

if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $name, $value = $line.Split("=", 2)
            [Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), "Process")
        }
    }
    Write-Host "Đã nạp cấu hình từ .env"
} else {
    Write-Warning "Không tìm thấy .env - dùng giá trị mặc định của application.yml"
}

$env:APP_PORT = "$Port"
Set-Location $projectDir
& (Join-Path $projectDir "gradlew.bat") bootRun
