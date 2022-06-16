FROM maven:3.8.1-ibmjava-8-alpine as build

WORKDIR /opt

ADD . /opt/

RUN mvn clean install -Dmaven.test.skip=true -Dasciidoctor.skip=true -Dos.detected.classifier=linux-x86_64

FROM ibmjava:8-jre

COPY --from=build /opt/application/target/*-SNAPSHOT.jar /applications/application.jar
COPY --from=build /opt/gateway/target/*-SNAPSHOT.jar /applications/gateway.jar

ENTRYPOINT ["/bin/sh","-c","java -jar -Dfile.encoding=UTF-8 /applications/application.jar --spring.profiles.active=test --server.port=8080"]