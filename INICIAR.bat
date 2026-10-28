@echo off
cd /d "%~dp0"
echo Abra http://localhost:8080 no navegador.
java -jar release/lumi-1.0.0.jar
pause
