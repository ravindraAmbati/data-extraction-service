param(
    [string]$PidFile = ""
)

if (-not $PidFile) {
    $ProjectDir = Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")
    $PidFile = Join-Path $ProjectDir "data-extraction-service.pid"
}

if (-not (Test-Path -LiteralPath $PidFile)) {
    Write-Output "data-extraction-service is not running"
    exit 0
}

$pidValue = Get-Content -LiteralPath $PidFile -ErrorAction SilentlyContinue
if ($pidValue) {
    $process = Get-Process -Id $pidValue -ErrorAction SilentlyContinue
    if ($process) {
        Stop-Process -Id $pidValue
        Write-Output "Stopped data-extraction-service with PID $pidValue"
    } else {
        Write-Output "No running process found for PID $pidValue"
    }
}
Remove-Item -LiteralPath $PidFile -Force -ErrorAction SilentlyContinue
