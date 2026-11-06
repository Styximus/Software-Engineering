param(
    [switch]$Test,
    [switch]$Screenshots,
    [switch]$Server,
    [string]$ServerUrl = 'http://127.0.0.1:8765',
    [string]$Library = 'data/client/library',
    [string]$BindAddress = '127.0.0.1',
    [int]$Port = 8765,
    [string]$ServerData = 'data/server'
)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if (-not (Get-Command javac -ErrorAction SilentlyContinue) -or
        -not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw 'Ein JDK ab Version 17 wird benötigt. java und javac müssen im PATH liegen.'
    }
    New-Item -ItemType Directory -Force -Path out | Out-Null
    $sources = @(Get-ChildItem -LiteralPath src -Filter '*.java' | ForEach-Object FullName)
    if ($Test -or $Screenshots) {
        $sources += @(Get-ChildItem -LiteralPath tests -Filter '*.java' | ForEach-Object FullName)
    }
    & javac -encoding UTF-8 --release 17 -d out @sources
    if ($LASTEXITCODE -ne 0) { throw 'Kompilierung fehlgeschlagen.' }
    if ($Test -or $Screenshots) {
        $testArgs = @('-Djava.awt.headless=true', '-cp', 'out', 'QuizTest')
        & java @testArgs
        if ($LASTEXITCODE -ne 0) { throw 'Quiz-Tests fehlgeschlagen.' }
        $workshopArgs = @('-Djava.awt.headless=true', '-cp', 'out', 'WorkshopTest')
        if ($Screenshots) { $workshopArgs += 'docs/screenshots' }
        & java @workshopArgs
    } elseif ($Server) {
        & java -cp out WorkshopServer $BindAddress $Port $ServerData samples/catalog
    } else {
        & java -cp out Main $ServerUrl $Library
    }
    if ($LASTEXITCODE -ne 0) { throw 'Ausführung fehlgeschlagen.' }
} finally {
    Pop-Location
}
