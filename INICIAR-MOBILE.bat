@echo off
cd /d "%~dp0"
echo Lumi - demonstracao com dados ficticios
echo Conecte computador e celular a mesma rede Wi-Fi privada.
echo No celular abra http://IP-DO-COMPUTADOR:8080
ipconfig
java -jar release/lumi-1.0.0.jar --server.address=0.0.0.0
pause
