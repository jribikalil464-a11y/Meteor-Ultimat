$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
Write-Host "Meteor Ultimate 1.21.4 build" -ForegroundColor Cyan
$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java) { throw "Java 21 JDK is required." }
$versionText = (& java -version 2>&1 | Out-String)
if ($versionText -notmatch '21') { throw "Java 21 is required. Detected: $versionText" }
$gv = "8.10.2"
$tools = Join-Path $PSScriptRoot ".tools"
$home = Join-Path $tools "gradle-$gv"
$gradle = Join-Path $home "bin\gradle.bat"
New-Item -ItemType Directory -Force -Path $tools | Out-Null
if (-not (Test-Path $gradle)) {
  $zip = Join-Path $tools "gradle-$gv-bin.zip"
  Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-$gv-bin.zip" -OutFile $zip
  Expand-Archive -Path $zip -DestinationPath $tools -Force
}
if (-not (Test-Path ".\gradlew.bat")) {
  & $gradle wrapper --gradle-version $gv
  if ($LASTEXITCODE -ne 0) { throw "Gradle wrapper creation failed." }
}
& .\gradlew.bat --no-daemon clean build
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed; inspect the full output." }
$libs = Join-Path $PSScriptRoot "build\libs"
$jar = Get-ChildItem $libs -Filter "*.jar" | Where-Object { $_.Name -notmatch "sources|javadoc|dev" } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $jar) { throw "No remapped addon JAR found in build\libs." }
$release = Join-Path $PSScriptRoot "release"
New-Item -ItemType Directory -Force -Path $release | Out-Null
Copy-Item $jar.FullName (Join-Path $release "Meteor-Ultimate-1.21.4.jar") -Force
Write-Host "SUCCESS: release\Meteor-Ultimate-1.21.4.jar" -ForegroundColor Green
