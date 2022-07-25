FROM maven:3.8.1-openjdk as build

WORKDIR /opt

ADD . /opt/

RUN mvn clean install -Dmaven.test.skip=true

FROM java:8-jre

COPY --from=build /opt/fusion-api/target/*-SNAPSHOT.jar /applications/fusion.jar

ENTRYPOINT ["/bin/sh","-c","java -jar -Dfile.encoding=UTF-8 /applications/fusion.jar --spring.profiles.active=test --server.port=8080"]