# primihub fusion
primihub fusion is based on spring boot,use maven to compile and package.
## Getting Started
First of all ,when we run the project, we need some service dependencies like this:
- jdk 1.8
- maven
- mysql 5.0+


##Modify Configuration
Now we should locate the next path:

    ./fusion-api/src/main/resources/

edit the "application.yaml" and modify the configuration to be the configuration of service dependencies which you have deployed.

especially those items should be paid attention literally.

    server:
      port: 
    spring:
      profiles:
        active: 
    ...
    spring:
      datasource:
        druid:
          ...:
            username: 
            url: 
            password: 

then we should locate this path:

    ./script
        init.sql


At last, you should execute "init.sql" in your mysql.

## Compile and Package
run this command:

    mvn clean install -Dmaven.test.skip=true 

As long as the finished infos show up, the project have been compiled and packaged successfully.

## How to run
Before run, make sure that your service dependencies are available and the configuration is correct.

    java -jar -Dfile.encoding=UTF-8 ./fusion-api/target/*-SNAPSHOT.jar --server.port=8090

execute that the command,you can check the url:
    
    http://localhost:8090/fusion/healthConnection
