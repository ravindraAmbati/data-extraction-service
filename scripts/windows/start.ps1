param(
    [string]$AppJar = "",
    [string]$Profile = "local",
    [string]$PidFile = "",
    [string]$JavaOpts = $env:JAVA_OPTS
)

$ProjectDir = Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")
if (-not $AppJar) {
    $AppJar = Join-Path $ProjectDir "target\data-extraction-service.jar"
}
if (-not $PidFile) {
    $PidFile = Join-Path $ProjectDir "data-extraction-service.pid"
}

if (Test-Path -LiteralPath $PidFile) {
    $existingPid = Get-Content -LiteralPath $PidFile -ErrorAction SilentlyContinue
    if ($existingPid -and (Get-Process -Id $existingPid -ErrorAction SilentlyContinue)) {
        Write-Output "data-extraction-service is already running with PID $existingPid"
        exit 0
    }
}

if (-not (Test-Path -LiteralPath $AppJar)) {
    Write-Error "JAR not found: $AppJar. Run: mvn package"
    exit 1
}

$arguments = @()
if ($JavaOpts) {
    $arguments += $JavaOpts.Split(" ", [System.StringSplitOptions]::RemoveEmptyEntries)
}
$arguments += @("-jar", $AppJar, "--spring.profiles.active=$Profile")

$process = Start-Process -FilePath "java" -ArgumentList $arguments -PassThru -WindowStyle Hidden
Set-Content -LiteralPath $PidFile -Value $process.Id
Write-Output "Started data-extraction-service with PID $($process.Id) using profile $Profile"
