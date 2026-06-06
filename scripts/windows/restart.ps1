param(
    [string]$AppJar = "",
    [string]$Profile = "local",
    [string]$PidFile = "",
    [string]$JavaOpts = $env:JAVA_OPTS
)

& "$PSScriptRoot\stop.ps1" -PidFile $PidFile
& "$PSScriptRoot\start.ps1" -AppJar $AppJar -Profile $Profile -PidFile $PidFile -JavaOpts $JavaOpts
