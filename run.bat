@echo off
REM ---------------------------------------------------------------------------
REM Compiles and runs the clinic appointment demo (Java 8).
REM Uses javac from PATH; if that is missing it falls back to JAVA_HOME and then
REM to a default Oracle JDK 8 installation.
REM ---------------------------------------------------------------------------
setlocal

set "JAVAC=javac"
set "JAVA=java"

where javac >nul 2>nul
if errorlevel 1 (
    if defined JAVA_HOME (
        set "JAVAC=%JAVA_HOME%\bin\javac.exe"
        set "JAVA=%JAVA_HOME%\bin\java.exe"
    ) else (
        if exist "C:\Program Files\Java\jdk1.8.0_271\bin\javac.exe" (
            set "JAVAC=C:\Program Files\Java\jdk1.8.0_271\bin\javac.exe"
            set "JAVA=C:\Program Files\Java\jdk1.8.0_271\bin\java.exe"
        )
    )
)

if not exist out mkdir out

"%JAVAC%" -encoding UTF-8 -d out src\clinic\*.java src\clinic\staff\*.java src\clinic\booking\*.java
if errorlevel 1 goto :failed

"%JAVA%" -Dfile.encoding=UTF-8 -cp out clinic.AssignmentOne
goto :eof

:failed
echo.
echo Compilation failed - check the messages above.
exit /b 1
