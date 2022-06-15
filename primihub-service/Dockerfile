FROM ibmjava:8-jre

ADD application/target/*-SNAPSHOT.jar /applications/application.jar
ADD gateway/target/*-SNAPSHOT.jar /applications/gateway.jar

ENTRYPOINT ["/bin/sh","-c","java -jar -Dfile.encoding=UTF-8 /applications/application.jar --spring.profiles.active=test --server.port=8080"]
