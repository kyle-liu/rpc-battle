
Benchmark Project
===============

Benchmark project contains below common parts:

- Rpc Api: RpcFactory interface, RpcExcption
- Data Object transported by rpc
- Benchmark Main classes

Server Main
==================

Zero configurtaion.

Client Main
===================

Client main has several command line options.

```bash
 -s,--server <arg>    serviceIndex server ip, default 127.0.0.1
 -d,--delay <arg>     count delay before start count(ms), default 0ms
 -t,--time <arg>      run duration(ms), default 3000ms
 -o,--output <arg>    benchmark result output file, default output result to console
 -h,--help            help
```

