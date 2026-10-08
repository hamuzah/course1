@echo off
setlocal
cd /d "%~dp0"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "PATH=%JAVA_HOME%\bin;%PATH%"
"%SystemRoot%\System32\where.exe" javac >nul 2>nul
if errorlevel 1 (
    echo Java JDK not found. Install JDK 17 or newer and reopen your terminal.
    if not defined G06_NOPAUSE pause
    exit /b 1
)
if not exist out mkdir out
rem The main class imports every module. javac also compiles their dependencies.
rem Relative paths avoid problems when the extracted folder has spaces.
javac --release 17 -encoding UTF-8 -d out -sourcepath src src\ug\stleoskyegobe\g06\app\MainMenu.java
if errorlevel 1 (
    echo Compilation failed.
    if not defined G06_NOPAUSE pause
    exit /b 1
)
java -cp out ug.stleoskyegobe.g06.app.MainMenu
set "RESULT=%errorlevel%"
if not defined G06_NOPAUSE pause
exit /b %RESULT%
