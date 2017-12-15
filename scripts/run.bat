echo off

set DRIVER=%1
set IMEIS=%2

echo "Start running resume for imeis: %IMEIS%"

for %%d in (libs\*.*) do (
	set test=%%d
)

set JAR=%test%
java -Ddriver=%DRIVER% -Dimeis=%IMIES% -jar %JAR%

echo on