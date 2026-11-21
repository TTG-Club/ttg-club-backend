# Инвентаризация схемы MySQL перед переездом на PostgreSQL (этап 0). Только чтение.
#
#   .\db\pg-migration\inventory\run.ps1                 # подключение из application-local.properties
#   $env:SPRING_DATASOURCE_URL = 'jdbc:mysql://...'     # или из переменных окружения
#   .\db\pg-migration\inventory\run.ps1 -SkipExpected   # не пересобирать ожидаемую схему
#
# Результат: report.md рядом со скриптом, сырые выгрузки в out/.
param(
    [string]$Properties = "$PSScriptRoot\..\..\..\src\main\resources\application-local.properties",
    [string]$JavaHome = "$env:USERPROFILE\.jdks\temurin-25.0.3",
    [string]$Mvn = "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.9.14\db91789b\bin\mvn.cmd",
    [switch]$SkipExpected
)
$ErrorActionPreference = 'Stop'
$root = Resolve-Path "$PSScriptRoot\..\..\.."
$here = $PSScriptRoot
$java = Join-Path $JavaHome 'bin\java.exe'
$env:JAVA_HOME = $JavaHome
$cpFile = Join-Path $here 'out\cp.txt'
New-Item -ItemType Directory -Force (Join-Path $here 'out') | Out-Null

Push-Location $root
try {
    & $Mvn -o -q compile dependency:build-classpath "-Dmdep.outputFile=$cpFile"
    if ($LASTEXITCODE -ne 0) { throw 'mvn compile/build-classpath failed' }
} finally { Pop-Location }
$cp = "$root\target\classes;" + (Get-Content $cpFile -Raw).Trim()
Set-Content -Encoding ascii $cpFile $cp

if (-not $SkipExpected) {
    # Ожидаемая схема из сущностей, без подключения к БД.
    foreach ($d in @(@('mysql', 'org.hibernate.dialect.MySQLDialect'), @('pg', 'org.hibernate.dialect.PostgreSQLDialect'))) {
        & $java -cp "@$cpFile" "$here\SchemaGen.java" "$root\target\classes" "$here\expected\expected-$($d[0]).sql" $d[1] | Select-String '^(classes|done)'
        python -I "$here\parse_ddl.py" "$here\expected\expected-$($d[0]).sql" "$here\expected\expected-$($d[0]).json"
    }
}

python -I "$here\gen_checks.py" "$here\expected\expected-mysql.json" "$here\expected\expected-pg.json" "$here\checks.sql"

$props = if (Test-Path $Properties) { $Properties } else { '-' }
& $java -cp "@$cpFile" "$here\Dump.java" $props "$here\meta.sql" "$here\out"
& $java -cp "@$cpFile" "$here\Dump.java" $props "$here\checks.sql" "$here\out"

python -I "$here\compare.py" "$here\out" "$here\expected\expected-mysql.json" "$here\report.md"
