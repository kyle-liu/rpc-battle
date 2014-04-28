#!/bin/bash

cd `dirname $0`/..
BASE_DIR=$PWD

SERVER_IP=10.235.170.2

REMOTE_RUN="ssh $SERVER_IP"

JAVA_OPTS="-Xms4g -Xmx4g -Xmn1g"

projects=`find . -maxdepth 1 -mindepth 1 -type d -not -name .\* -not -name benchmark -not -name ri -not -name target -printf '%f\n'` 

for prj in $projects ; do

    $REMOTE_RUN "pkill -9 java"

    sleep 2

    echo -n Starting server of $prj .
    $REMOTE_RUN "cd $BASE_DIR/$prj; $BASE_DIR/benchmark/run-server.sh" &> server.log &

    counter=0
    while ! $REMOTE_RUN "ps ux | fgrep -v 'fgrep com.taobao.rpc.benchmark.main.ServerMain' | fgrep com.taobao.rpc.benchmark.main.ServerMain -q" ; do
        sleep 1
        echo -n .
        counter=$((counter + 1))
        if [ $counter -gt 30 ] ; then
            echo Fail to start server of $prj!
            break
        fi
    done
    echo
    if [ $counter -gt 30 ] ; then
        continue
    fi

    if ! (
        cd $BASE_DIR/$prj && mvn clean install -Dmaven.test.skip &&
        mvn dependency:copy-dependencies -DoutputDirectory=target/release-jars -DincludeScope=compile &&
        cp target/*.jar target/release-jars
    ) ; then
        echo Fail to install or copy dependencies of $prj!
        continue 
    fi

    for size in 1 3 5 ; do
        echo running client use data size ${size}K...
        CLASS_PATH=`echo $BASE_DIR/$prj/target/release-jars/*.jar | tr " " :`
        $JAVA_HOME/bin/java $JAVA_OPTS -cp $CLASS_PATH com.taobao.rpc.benchmark.main.ClientMain -d 3000 -t 60000 -s $SERVER_IP -z $size -o ~/result.csv
    done

done

