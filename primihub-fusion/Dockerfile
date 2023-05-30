FROM maven:3.8.1-openjdk as build

WORKDIR /opt

ADD . /opt/

RUN ARCH=`arch | sed s/arm64/aarch_64/ | sed s/aarch64/aarch_64/ | sed s/amd64/x86_64/` \
  && mvn clean install -Dmaven.test.skip=true -Dos.detected.classifier=linux-${ARCH}

FROM openjdk:8-jre

ENV DEBIAN_FRONTEND=noninteractive

RUN apt update \
  && apt -y install tzdata telnet \
  && ln -fs /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

COPY --from=build /opt/fusion-api/target/*-SNAPSHOT.jar /applications/fusion.jar
COPY --from=build /opt/fusion-simple/target/*-SNAPSHOT.jar /applications/fusion-simple.jar

ENTRYPOINT ["/bin/sh","-c","java -jar -Dfile.encoding=UTF-8 /applications/fusion.jar --spring.profiles.active=test --server.port=8080"]