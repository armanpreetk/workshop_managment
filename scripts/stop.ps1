param(
  [string[]]$Ports = @("8081","8080","5173")
)
$ErrorActionPreference = 'SilentlyContinue'
Write-Host "Stopping processes on ports: $($Ports -join ', ')" -ForegroundColor Cyan
foreach ($p in $Ports) {
  $pids = (netstat -ano | Select-String ":$p" | ForEach-Object { ($_ -split "\s+")[-1] } | Select-Object -Unique)
  if ($pids) { foreach ($pid in $pids) { try { Stop-Process -Id $pid -Force } catch {} } }
}
