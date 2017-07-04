#!/bin/sh
clear

echo "Generating database classes"
java -classpath jooq-3.8.4.jar:jooq-meta-3.8.4.jar:jooq-codegen-3.8.4.jar:mysql-connector-java-5.1.39-bin.jar:. org.jooq.util.GenerationTool DatabaseJOOQ.xml
