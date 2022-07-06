# primihub management platform service
primihub management platform is based on spring cloud,use maven to compile and package.
## Getting Started
First of all ,when we run the project, we need some service dependencies like this:
- jdk 1.8
- maven
- nacos 2.0.3 or 2.0.4
- mysql 5.0+
- redis 5.0+
- RabbitMQ

##Modify Configuration
Now we should locate the next two pathes:

    ./application/src/main/resources/
    ./gateway/src/main/resources/

edit the "application.yaml" and modify the configuration to be the configuration of service dependencies which you have deployed.

especially those items should be paid attention literally.

    server:
      port: 
    spring:
      profiles:
        active: 
    ...
      nacos:
        discovery:
          server-addr: 
          namespace:
    ...
    nacos:
      config:
        server-addr:

then we should locate this path:

    ./script
        base.json
        database.yaml
        ddl.sql
        init.sql
        redis.yaml

In the next step, we enter the nacos management(usually http://localhost:8848/nacos) ,create base.json,database.yaml,redis.yaml in your target namespace.

Also modify the configuration which is just created in nacos.

    spring:
      datasource:
        druid:
          ...:
            username: 
            url: 
            password: 

At last, you should execute the "ddl.sql" in your mysql.

## Compile and Package
if your environment in linux,you should run this command:

    mvn clean install -Dmaven.test.skip=true -Dasciidoctor.skip=true -Dos.detected.classifier=linux-x86_64

and when your environment in windows or mac ,you should change the param "-Dos.detected.classifier" to "windows-x86_64" or "osx-x86_64".

As long as the finished infos show up, the project have been compiled and packaged successfully.

## How to run
Before run, make sure that your service dependencies are available and the configuration is correct.

    java -jar -Dfile.encoding=UTF-8 ./application/target/*-SNAPSHOT.jar --server.port=8090
    java -jar -Dfile.encoding=UTF-8 ./gateway/target/*-SNAPSHOT.jar --server.port=8088

execute that two commands in different terminal,when get started,you can check the url:
    
    http://localhost:8088/sys/user/login
