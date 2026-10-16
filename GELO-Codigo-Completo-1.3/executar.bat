@echo off
setlocal
where java >nul 2>nul
if errorlevel 1 (
  echo ERRO: Java nao foi encontrado no PATH. Instale o JDK 17 ou superior.
  pause
  exit /b 1
)
where mvn >nul 2>nul
if errorlevel 1 (
  echo ERRO: Maven nao foi encontrado no PATH. Instale o Maven e abra um terminal novo.
  pause
  exit /b 1
)
echo Iniciando GELO em http://localhost:8080/
mvn spring-boot:run
endlocal
