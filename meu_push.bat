@echo off
echo Limpando e iniciando envio para o GitHub...

git init
git add .
git commit -m "Primeiro commit do projeto pedidos"
git branch -M main

@rem Remove o remoto antigo se existir e adiciona o correto
git remote remove origin 2>nul
git remote add origin https://github.com/tavaresfilhoinformatica-creator/pedidos.git

git push -u origin main

echo.
echo Processo concluido!
pause