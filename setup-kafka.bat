@echo off
REM Kafka Setup Script for Windows
REM This script downloads and sets up Kafka locally

set KAFKA_VERSION=3.6.0
set SCALA_VERSION=2.13
set KAFKA_HOME=%CD%\kafka

echo ========================================
echo Kafka Setup for Windows
echo ========================================

REM Check if Kafka is already downloaded
if exist "kafka_%KAFKA_VERSION%" (
    echo Kafka already downloaded.
) else (
    echo Downloading Kafka %KAFKA_VERSION%...
    powershell -Command "Invoke-WebRequest -Uri 'https://archive.apache.org/dist/kafka/%KAFKA_VERSION%/kafka_2.13-%KAFKA_VERSION%.tgz' -OutFile 'kafka.tgz'"
    echo Extracting Kafka...
    powershell -Command "Expand-Archive -Path 'kafka.tgz' -DestinationPath '.' -Force"
    del kafka.tgz
)

echo.
echo ========================================
echo Starting Zookeeper...
echo ========================================
start "Zookeeper" cmd /k "cd kafka_%KAFKA_VERSION% && bin\windows\zookeeper-server-start.bat config\zookeeper.properties"

echo Waiting for Zookeeper to start...
timeout /t 10

echo.
echo ========================================
echo Starting Kafka Broker...
echo ========================================
start "Kafka" cmd /k "cd kafka_%KAFKA_VERSION% && bin\windows\kafka-server-start.bat config\server.properties"

echo.
echo ========================================
echo Kafka is starting up...
echo.
echo Topics will be created automatically by the services.
echo.
echo To stop Kafka, run:
echo   bin\windows\kafka-server-stop.bat
echo   bin\windows\zookeeper-server-stop.bat
echo ========================================
