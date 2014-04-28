#!/bin/bash
cd `dirname $0`
BASE=`pwd`

JAVA_OPTS="-Xms4g -Xmx4g -Xmn1g"

mvn clean
mvn install assembly:assembly -Dmaven.test.skip

java $JAVA_OPTS -Djava.ext.dirs="./target/rpc-zaza-0.0.1-SNAPSHOT.dir/rpc-zaza-0.0.1-SNAPSHOT/lib" com.taobao.rpc.benchmark.main.ClientMain  -t 200000 -s 10.235.170.1 > zazaclient.log 2>&1 &