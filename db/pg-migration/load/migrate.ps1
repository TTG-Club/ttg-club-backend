<#
.SYNOPSIS
  Переезд 5e14 с MySQL на PostgreSQL: дамп MySQL -> схема -> загрузка данных -> все changeset'ы.

.DESCRIPTION
  Выполняет шаги 3–6 «Порядка переезда» из db/pg-migration/README.md:
    1. сборка приложения и classpath (Maven);
    2. проверка целевой базы PostgreSQL: версия 18+, пустая схема public,
       право менять session_replication_role;
    3. дамп MySQL (mysqldump) — или готовый файл из -DumpFile;
    4. преобразование дампа в SQL для PostgreSQL (dump_to_pg.py; при нарушениях
       данных — отчёт и остановка);
    5. схема до метки data-load (LiquibaseUpdate.java);
    6. загрузка данных (LoadPg.java, одна транзакция);
    7. остальные changeset'ы: jsonb (этап 2) и разметка core-api (этап 3) —
       можно пропустить флагом -StopAtDataLoad, тогда их применит приложение при старте;
    8. контрольные числа записей.

  Очистку MySQL (README, шаг 1) сценарий НЕ выполняет — её нужно сделать заранее.

  Подключение к PostgreSQL — переменные окружения, как у приложения:
    SPRING_DATASOURCE_URL       jdbc:postgresql://<хост>:5432/<база>
    SPRING_DATASOURCE_USERNAME
    SPRING_DATASOURCE_PASSWORD
  Пароль MySQL — переменная MYSQL_PWD; если её нет, сценарий спросит пароль.

  Запускать из корня репозитория бэкенда. Нужны: JDK 25 (java в PATH или JAVA_HOME),
  Python 3, Maven, для дампа — mysqldump.

.EXAMPLE
  $env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://db:5432/dnd5'
  $env:SPRING_DATASOURCE_USERNAME = 'dnd5'
  $env:SPRING_DATASOURCE_PASSWORD = '...'
  .\db\pg-migration\load\migrate.ps1 -MysqlHost db-mysql -MysqlUser dnd5 -MysqlDatabase dnd5

.EXAMPLE
  # готовый дамп, Maven из IntelliJ, без сети
  .\db\pg-migration\load\migrate.ps1 -DumpFile D:\dumps\prod.sql -Mvn 'C:\...\mvn.cmd' -Offline
#>
[CmdletBinding()]
param(
	# MySQL-источник (не нужен, если задан -DumpFile)
	[string]$MysqlHost = 'localhost',
	[int]$MysqlPort = 3306,
	[string]$MysqlUser,
	[string]$MysqlDatabase,

	# Готовый дамп (mysqldump --single-transaction --skip-triggers) вместо снятия нового
	[string]$DumpFile,

	# Папка для дампа, data.sql, classpath и журнала; по умолчанию — новая папка во временном каталоге
	[string]$WorkDir = (Join-Path ([IO.Path]::GetTempPath()) ('pg-migration-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))),

	# Путь к mvn / mvn.cmd, если его нет в PATH
	[string]$Mvn,
	# Maven без сети (зависимости уже в ~/.m2)
	[switch]$Offline,

	# Остановиться после загрузки данных; остальные changeset'ы применит приложение при старте
	[switch]$StopAtDataLoad
)

$ErrorActionPreference = 'Stop'
# кириллица в выводе java и python — UTF-8 в консоли
$env:PYTHONIOENCODING = 'utf-8'
$javaOpts = @('-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8')
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path
$load = Join-Path $repo 'db\pg-migration\load'

function Write-Step([string]$text) {
	Write-Host ''
	Write-Host "==> $text" -ForegroundColor Cyan
}

function Assert-Exit([string]$what) {
	if ($LASTEXITCODE -ne 0) {
		throw "${what}: код выхода $LASTEXITCODE"
	}
}

function Find-Tool([string]$name) {
	$cmd = Get-Command $name -ErrorAction SilentlyContinue
	if ($cmd) { return $cmd.Source }
	return $null
}

# ---------- проверки окружения ----------
foreach ($var in 'SPRING_DATASOURCE_URL', 'SPRING_DATASOURCE_USERNAME', 'SPRING_DATASOURCE_PASSWORD') {
	if (-not [Environment]::GetEnvironmentVariable($var)) {
		throw "не задана переменная $var (подключение к PostgreSQL)"
	}
}
if ($env:SPRING_DATASOURCE_URL -notlike 'jdbc:postgresql:*') {
	throw "SPRING_DATASOURCE_URL должна указывать на PostgreSQL: $env:SPRING_DATASOURCE_URL"
}

$java = if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) { "$env:JAVA_HOME\bin\java.exe" } else { Find-Tool 'java' }
if (-not $java) { throw 'не найден java (JDK 25): задайте JAVA_HOME или добавьте java в PATH' }

$python = Find-Tool 'python'
if (-not $python) { $python = Find-Tool 'py' }
if (-not $python) { throw 'не найден Python 3' }

if (-not $Mvn) {
	$Mvn = Find-Tool 'mvn'
	if (-not $Mvn) {
		# Maven из самой свежей IntelliJ: в старых версиях Maven не запускается на JDK 25
		$Mvn = Get-ChildItem 'C:\Program Files\JetBrains\*\plugins\maven*\lib\maven3\bin\mvn.cmd' -ErrorAction SilentlyContinue |
			Sort-Object LastWriteTime -Descending | Select-Object -First 1 -ExpandProperty FullName
	}
}
if (-not $Mvn) { throw 'не найден Maven: укажите путь параметром -Mvn' }

if ($DumpFile) {
	$DumpFile = (Resolve-Path $DumpFile).Path
} else {
	if (-not $MysqlUser -or -not $MysqlDatabase) {
		throw 'укажите -MysqlUser и -MysqlDatabase (или готовый дамп -DumpFile)'
	}
	$mysqldump = Find-Tool 'mysqldump'
	if (-not $mysqldump) { throw 'не найден mysqldump (или укажите готовый дамп -DumpFile)' }
}

New-Item -ItemType Directory -Force $WorkDir | Out-Null
$WorkDir = (Resolve-Path $WorkDir).Path
$cp = Join-Path $WorkDir 'cp.txt'
$dataSql = Join-Path $WorkDir 'data.sql'
Start-Transcript -Path (Join-Path $WorkDir 'migrate.log') | Out-Null
$prevEncoding = [Console]::OutputEncoding
[Console]::OutputEncoding = New-Object Text.UTF8Encoding $false

try {
	Write-Host "рабочая папка: $WorkDir"
	Write-Host "PostgreSQL:    $env:SPRING_DATASOURCE_URL ($env:SPRING_DATASOURCE_USERNAME)"
	Push-Location $repo

	# ---------- 1. сборка ----------
	Write-Step 'Сборка приложения и classpath'
	$mvnArgs = @('-q', 'compile', 'dependency:build-classpath', "-Dmdep.outputFile=$cp")
	if ($Offline) { $mvnArgs = @('-o') + $mvnArgs }
	& $Mvn @mvnArgs
	Assert-Exit 'Maven'
	# target/classes — первым: changelog и customChange (перевод в разметку) берутся оттуда.
	# В файле аргументов java пробел разделяет аргументы, а «\» внутри кавычек — экранирование:
	# путь — в кавычках и с прямыми слешами.
	$classpath = ((Join-Path $repo 'target\classes') + ';' + (Get-Content $cp -Raw).Trim()) -replace '\\', '/'
	[IO.File]::WriteAllText($cp, '"' + $classpath + '"', (New-Object Text.UTF8Encoding $false))
	$cpArg = "@$cp"

	# ---------- 2. проверка базы ----------
	Write-Step 'Проверка базы PostgreSQL'
	& $java @javaOpts -cp $cpArg (Join-Path $load 'PgTool.java') preflight
	Assert-Exit 'проверка базы'

	# ---------- 3. дамп MySQL ----------
	if (-not $DumpFile) {
		Write-Step "Дамп MySQL $MysqlDatabase@${MysqlHost}:$MysqlPort"
		$DumpFile = Join-Path $WorkDir 'dump.sql'
		$hadPwd = [bool]$env:MYSQL_PWD
		if (-not $hadPwd) {
			$secure = Read-Host "Пароль MySQL для $MysqlUser" -AsSecureString
			$bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
			try {
				$env:MYSQL_PWD = [Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
			} finally {
				# копия пароля в неуправляемой памяти — затереть сразу
				[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
			}
		}
		try {
			# --result-file, а не «>»: перенаправление PowerShell 5 перекодирует дамп в UTF-16
			& $mysqldump --single-transaction --skip-triggers --default-character-set=utf8mb4 `
				"--host=$MysqlHost" "--port=$MysqlPort" "--user=$MysqlUser" "--result-file=$DumpFile" $MysqlDatabase
			Assert-Exit 'mysqldump'
		} finally {
			if (-not $hadPwd) { Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue }
		}
	}
	Write-Host ("дамп: $DumpFile, {0:N0} МБ" -f ((Get-Item $DumpFile).Length / 1MB))

	# ---------- 4. дамп -> SQL для PostgreSQL ----------
	Write-Step 'Преобразование дампа (dump_to_pg.py)'
	& $python -I (Join-Path $load 'dump_to_pg.py') $DumpFile `
		(Join-Path $repo 'src\main\resources\db\changelog\changes') $dataSql
	Assert-Exit 'dump_to_pg.py (нарушения данных — в отчёте выше)'

	# ---------- 5. схема до data-load ----------
	Write-Step 'Схема до метки data-load'
	& $java @javaOpts -cp $cpArg (Join-Path $load 'LiquibaseUpdate.java') data-load
	Assert-Exit 'liquibase update до data-load'

	# ---------- 6. загрузка ----------
	Write-Step 'Загрузка данных (LoadPg.java)'
	& $java @javaOpts -cp $cpArg (Join-Path $load 'LoadPg.java') $dataSql
	Assert-Exit 'загрузка данных (транзакция откатилась; перед повтором очистите схему public)'

	# ---------- 7. остальные changeset'ы ----------
	if ($StopAtDataLoad) {
		Write-Step 'Остальные changeset''ы пропущены (-StopAtDataLoad): их применит приложение при старте'
	} else {
		Write-Step 'Остальные changeset''ы: jsonb и разметка core-api (несколько минут)'
		& $java @javaOpts -cp $cpArg (Join-Path $load 'LiquibaseUpdate.java')
		Assert-Exit 'liquibase update'
	}

	# ---------- 8. итог ----------
	Write-Step 'Контрольные числа'
	& $java @javaOpts -cp $cpArg (Join-Path $load 'PgTool.java') stats
	Assert-Exit 'контрольные числа'

	Write-Host ''
	Write-Host 'Готово. Сверьте числа с MySQL, затем переключите приложение на эту базу.' -ForegroundColor Green
} finally {
	Pop-Location
	Stop-Transcript | Out-Null
	[Console]::OutputEncoding = $prevEncoding
}
