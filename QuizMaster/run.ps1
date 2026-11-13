param([switch]$Test, [switch]$Screenshots)
$ErrorActionPreference = 'Stop'
$classes = Join-Path $PSScriptRoot 'out/classes'
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = @(Get-ChildItem (Join-Path $PSScriptRoot 'src/main/java') -Filter '*.java' -Recurse | ForEach-Object FullName)
if ($Test -or $Screenshots) {
    $sources += @(Get-ChildItem (Join-Path $PSScriptRoot 'src/test/java') -Filter '*.java' -Recurse | ForEach-Object FullName)
}
& javac -encoding UTF-8 --release 17 -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Kompilierung fehlgeschlagen.' }
Copy-Item (Join-Path $PSScriptRoot 'src/main/resources/*') -Destination $classes -Recurse -Force
if ($Test -or $Screenshots) {
    $testArgs = @()
    if ($Screenshots) { $testArgs += (Join-Path $PSScriptRoot 'out/screenshots') }
    & java '-Djava.awt.headless=true' -cp $classes de.quizmaster.QuizUiTest @testArgs
} else {
    & java -cp $classes de.quizmaster.Main
}
if ($LASTEXITCODE -ne 0) { throw 'Ausführung fehlgeschlagen.' }
