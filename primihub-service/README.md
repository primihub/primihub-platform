# primihub management platform service
primihub management platform is based on spring cloud,use maven to compile and package.
## Getting Started
First of all ,when we run the project, we need some service dependencies like this:
- [jdk 1.8](https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.html)
- [maven](https://maven.apache.org/download.cgi)
- [nacos 2.0.3](https://github.com/alibaba/nacos/releases/tag/2.0.3) or [2.0.4](https://github.com/alibaba/nacos/releases/tag/2.0.4)
- [mysql 5.0+](https://dev.mysql.com/downloads/mysql)
- [redis 5.0+](https://redis.io/download/)
- [RabbitMQ](https://github.com/rabbitmq/rabbitmq-server/releases/tag/v3.10.6)

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
        components.json
        database.yaml
        ddl.sql
        init.sql
        redis.yaml

In the next step, we enter the nacos management(usually http://localhost:8848/nacos) ,create base.json,components.json,database.yaml,redis.yaml in your target namespace.

Also modify the configuration which is just created in nacos.

    spring:
      datasource:
        druid:
          ...:
            username: 
            url: 
            password:

then we should locate this path:

    ./script
        init.sh

to this path,you can execute next command:

    cd ./script
    sh init.sh [your mysql username] [your mysql password]

or you can execute "ddl.sql" in your mysql management manually.

## Compile and Package
You should run this command:

    mvn clean install -Dmaven.test.skip=true -Dasciidoctor.skip=true

As long as the finished infos show up, the project have been compiled and packaged successfully.

## How to run
Before run, make sure that your service dependencies are available and the configuration is correct.

    java -jar -Dfile.encoding=UTF-8 ./application/target/*-SNAPSHOT.jar --server.port=8090
    java -jar -Dfile.encoding=UTF-8 ./gateway/target/*-SNAPSHOT.jar --server.port=8088

execute that two commands in different terminal,when get started,you can check the url:
    
    http://localhost:8088/sys/user/login
