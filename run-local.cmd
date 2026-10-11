@echo off
REM Starts the backend with the embedded H2 database (no MySQL needed).
REM Finds an installed JDK automatically if JAVA_HOME isn't set in this terminal.

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto run

for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do set "JAVA_HOME=%%~fD"
if not defined JAVA_HOME (
  echo Could not find Java 21. Install it with: winget install EclipseAdoptium.Temurin.21.JDK
  exit /b 1
)
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo JAVA_HOME points to "%JAVA_HOME%" but java.exe is missing there.
  exit /b 1
)

:run
echo Using Java from %JAVA_HOME%
call "%~dp0mvnw.cmd" spring-boot:run "-Dspring-boot.run.profiles=local" %*
