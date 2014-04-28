reference implementaion
============================

Just a simple workaround reference implementaion demo using `Spring RMI`.

This Demo show you how to integrate your implemetation to the benchmark.

How to implement
============================

0. Instatll benchmark module
-----------------------

Instatll benchmark module, or you can simply install this whole parent maven project with all submodule.

1. add benchmark module dependency
-----------------------

Add below XML snippet to your pom file.

```xml
<dependency>
	<groupId>com.taobao.rpc</groupId>
	<artifactId>rpc-benchmark</artifactId>
</dependency>
```

2. implemetation class
-----------------------

Implement the interface `com.taobao.rpc.ri.RiRpcFactory`.

3.Supply rpc.properties file
------------------------

Please supply a **`rpc.properties`** file at *the root of classpath*:

> The value of key `factory.impl` is the implementaion class of interface `com.taobao.rpc.benchmark.RpcFactory`,
> so the benchmark program can load and run your implementaion.

How to Run Benchmark
=============================

benchmark module contains main class.

- Main class for server: `com.taobao.rpc.benchmark.main.ServerMain`
- Main class for client: `com.taobao.rpc.benchmark.main.ClientMain`

Maven Style
-----------------------

Use Maven can simple run use below command line under your implementaion maven module:
\# Of course you need install the whole maven project at first.

```bash
# Run Server
mvn install exec:java -Dexec.mainClass=com.taobao.rpc.benchmark.main.ServerMain

# Run Client
mvn install exec:java -Dexec.mainClass=com.taobao.rpc.benchmark.main.ClientMain

# or with arguments
mvn install exec:java -Dexec.mainClass=com.taobao.rpc.benchmark.main.ClientMain -Dexec.args="add main arguments if need"
```
Command Line Style
-----------------------

Use shell script to run benchmark via java command under your implementaion maven module:
\# Of course you need install the whole maven project at first.

Via java command, the benchmark is **more** precise since there is no noise from maven.

```bash
# Run Server
./run-server.sh

# Run Client
./run-client.sh

# or with arguments
./run-client.sh add arguments if need
```

\# run-client.sh accepts several command line options, see the doc of benchmark module.
