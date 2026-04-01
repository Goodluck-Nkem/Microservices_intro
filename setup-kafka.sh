#!/bin/bash

KAFKA_VERSION=3.6.0
SCALA_VERSION=2.13

echo "========================================"
echo "Kafka Setup for macOS/Linux"
echo "========================================"

if [ -d "kafka_${KAFKA_VERSION}" ]; then
    echo "Kafka already downloaded."
else
    echo "Downloading Kafka ${KAFKA_VERSION}..."
    curl -O "https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/kafka_2.13-${KAFKA_VERSION}.tgz"
    tar -xzf "kafka_2.13-${KAFKA_VERSION}.tgz"
    rm "kafka_2.13-${KAFKA_VERSION}.tgz"
fi

echo ""
echo "========================================"
echo "Starting Zookeeper..."
echo "========================================"
./kafka_${KAFKA_VERSION}/bin/zookeeper-server-start.sh ./kafka_${KAFKA_VERSION}/config/zookeeper.properties &

echo "Waiting for Zookeeper to start..."
sleep 10

echo ""
echo "========================================"
echo "Starting Kafka Broker..."
echo "========================================"
./kafka_${KAFKA_VERSION}/bin/kafka-server-start.sh ./kafka_${KAFKA_VERSION}/config/server.properties

echo ""
echo "========================================"
echo "Kafka is starting up..."
echo ""
echo "To stop Kafka, press Ctrl+C or run:"
echo "  ./kafka_${KAFKA_VERSION}/bin/kafka-server-stop.sh"
echo "  ./kafka_${KAFKA_VERSION}/bin/zookeeper-server-stop.sh"
echo "========================================"
