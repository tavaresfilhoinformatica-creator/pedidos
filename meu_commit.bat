@echo off
echo Salvando e enviando alteracoes para o GitHub...
:: 1. Prepara todos os arquivos alterados
git add .

:: 2. Pede para voce digitar a mensagem da alteracao
set /p mensagem="Digite o que voce alterou no projeto: "

:: 3. Faz o commit usando a mensagem que voce digitou
git commit -m "%mensagem%"

:: 4. Envia para o GitHub
git push origin main

echo.
echo Alteracoes enviadas com sucesso!
pause
exit
