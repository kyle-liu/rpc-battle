#!/bin/bash
BASE=`pwd`

JAVA_OPTS="$JAVA_OPTS -Xms4g -Xmx4g -Xmn1g"

mvn clean &&
mvn install -Dmaven.test.skip || (echo Fail to clean or install!; exit 1)

mvn dependency:copy-dependencies -DoutputDirectory=target/release-jars -DincludeScope=compile &&
cp target/*.jar target/release-jars || (echo Fail to copy dependencies!; exit 2)

CLASS_PATH=`echo $BASE/target/release-jars/*.jar | tr " " :`

cd target/release-jars &&
$JAVA_HOME/bin/java $JAVA_OPTS -cp $CLASS_PATH com.taobao.rpc.benchmark.main.ClientMain "$@"
