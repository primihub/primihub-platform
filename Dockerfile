FROM maven:3.8.6-openjdk-8 as build

WORKDIR /opt

ADD . /opt/

RUN ARCH=`arch | sed s/arm64/aarch_64/ | sed s/aarch64/aarch_64/ | sed s/amd64/x86_64/` \
  && cd primihub-sdk \
  && mvn clean install -Dmaven.test.skip=true -Dasciidoctor.skip=true -Dos.detected.classifier=linux-${ARCH}
RUN cd primihub-service \
  && mvn clean install -Dmaven.test.skip=true -Dasciidoctor.skip=true

FROM openjdk:8-jre

ENV DEBIAN_FRONTEND=noninteractive

RUN apt update \
  && apt install tzdata \
  && ln -fs /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

COPY --from=build /opt/primihub-service/application/target/*-SNAPSHOT.jar /applications/application.jar
COPY --from=build /opt/primihub-service/gateway/target/*-SNAPSHOT.jar /applications/gateway.jar

ENTRYPOINT ["/bin/sh","-c","java -jar -Dfile.encoding=UTF-8 /applications/application.jar --spring.profiles.active=test --server.port=8080"]
