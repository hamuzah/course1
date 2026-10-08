@echo off
setlocal
cd /d "%~dp0.."
set "ROOT=%CD%"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "PATH=%JAVA_HOME%\bin;%PATH%"
"%SystemRoot%\System32\where.exe" javac >nul 2>nul
if errorlevel 1 (
    echo Java JDK not found. Install JDK 17 or newer.
    if not defined G06_NOPAUSE pause
    exit /b 1
)
rem Each run gets a fresh folder; no existing folders are deleted.
:newrun
set "RUN=%ROOT%\tests\_test_runs\run_%RANDOM%_%RANDOM%"
if exist "%RUN%" goto newrun
mkdir "%RUN%\classes"
mkdir "%RUN%\work"
mkdir "%RUN%\temp"
javac --release 17 -encoding UTF-8 -d "%RUN%\classes" -sourcepath src src\ug\stleoskyegobe\g06\app\MainMenu.java tests\ProjectTests.java
if errorlevel 1 (
    echo Compilation failed.
    if not defined G06_NOPAUSE pause
    exit /b 1
)
pushd "%RUN%\work"
java "-Djava.io.tmpdir=%RUN%\temp" -cp "%RUN%\classes" ProjectTests > "%ROOT%\tests\TEST_LOG.txt" 2>&1
set "RESULT=%errorlevel%"
popd
type "%ROOT%\tests\TEST_LOG.txt"
if not defined G06_NOPAUSE pause
exit /b %RESULT%
