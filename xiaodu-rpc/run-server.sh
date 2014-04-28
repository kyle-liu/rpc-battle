#!/bin/bash
cd `dirname $0`
BASE=`pwd`
# JAVA_OPTS=-Xms4g -Xmx4g -Xmn1g
mvn clean
mvn install -Dmaven.test.skip
mvn dependency:copy-dependencies -DoutputDirectory=target/release-jars -DincludeScope=compile
cp target/*.jar target/release-jars
CLASS_PATH=`echo $BASE/target/release-jars/*.jar | tr " " :`
cd ./target/release-jars
$JAVA_HOME/bin/java $JAVA_OPTS -cp $CLASS_PATH com.taobao.rpc.benchmark.main.ServerMain
