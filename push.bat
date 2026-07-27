@echo off
echo Iniciando o envio do projeto para o GitHub...

git init
git add .
git commit -m "Primeiro commit do projeto pedidos"
git branch -M main
git remote add origin https://github.com/tavaresfilhoinformatica-creator/pedidos.git
git push -u origin main

echo.
echo Processo concluido!
pause
