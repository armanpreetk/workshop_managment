param(
    [switch]$SkipBuild,
    [string]$BackendPort = "8081",
    [string]$GatewayPort = "8080",
    [string]$FrontendPort = "5173",
    [string]$BackendUrl
)

$ErrorActionPreference = 'Stop'

function Write-Section($text){ Write-Host "`n=== $text ===" -ForegroundColor Cyan }
function Ensure-Ok { param($label) if ($LASTEXITCODE -ne 0) { throw "Failed: $label (exit $LASTEXITCODE)" } }

$RepoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $RepoRoot

Write-Section "Config"
if (-not $BackendUrl) { $BackendUrl = "http://127.0.0.1:$BackendPort" }
$env:BACKEND_URL = $BackendUrl
Write-Host "Backend URL: $env:BACKEND_URL"

Write-Section "Killing anything on ports $BackendPort/$GatewayPort/$FrontendPort"
$ports = @($BackendPort, $GatewayPort, $FrontendPort)
foreach ($p in $ports) {
  try {
    $pids = (netstat -ano | Select-String ":$p" | ForEach-Object { ($_ -split "\s+")[-1] } | Select-Object -Unique)
    if ($pids) { foreach ($pid in $pids) { try { Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue } catch {} } }
  } catch {}
}

if (-not $SkipBuild) {
  Write-Section "Build backend"
  & .\mvnw.cmd -q -DskipTests package
  Ensure-Ok "backend build"

  Write-Section "Build gateway"
  & .\mvnw.cmd -q -DskipTests -f .\gateway-service\pom.xml package
  Ensure-Ok "gateway build"
}

Write-Section "Start backend (port $BackendPort)"
$backendArgs = @()
if ($BackendPort -ne "8081") { $backendArgs += "-Dspring-boot.run.jvmArguments=-Dserver.port=$BackendPort" }
Start-Process -FilePath .\mvnw.cmd -ArgumentList (@("-DskipTests","spring-boot:run") + $backendArgs) -NoNewWindow -WorkingDirectory $RepoRoot | Out-Null
Start-Sleep -Seconds 4

Write-Section "Start gateway (port $GatewayPort)"
$env:BACKEND_URL = $BackendUrl
Start-Process -FilePath .\mvnw.cmd -ArgumentList @("-f",".\gateway-service\pom.xml","-DskipTests","spring-boot:run") -NoNewWindow -WorkingDirectory $RepoRoot | Out-Null
Start-Sleep -Seconds 3

Write-Section "Start frontend (port $FrontendPort)"
$feDir = Join-Path $RepoRoot "frontend"
$feCmd = "npm"
$feArgs = @("run","dev","--","--port","$FrontendPort")
Start-Process -FilePath $feCmd -ArgumentList $feArgs -WorkingDirectory $feDir -NoNewWindow | Out-Null

Write-Section "All services launched"
Write-Host "Backend:  http://127.0.0.1:$BackendPort" -ForegroundColor Green
Write-Host "Gateway:  http://127.0.0.1:$GatewayPort" -ForegroundColor Green
Write-Host "Frontend: http://127.0.0.1:$FrontendPort" -ForegroundColor Green
Write-Host "Swagger via Gateway: http://127.0.0.1:$GatewayPort/swagger-ui/index.html" -ForegroundColor Green
