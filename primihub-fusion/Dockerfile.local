FROM ibmjava:8-jre

ADD fusion-api/target/*-SNAPSHOT.jar /applications/fusion.jar

ENTRYPOINT ["/bin/sh","-c","java -jar -Dfile.encoding=UTF-8 /applications/fusion.jar --spring.profiles.active=test --server.port=8080"]