FROM openjdk:8u181 as jcp

ENV JDK=/usr/lib/jvm/java-8-openjdk-amd64
ARG JCP_VERSION=jcp-2.0.40502

WORKDIR /opt/jcp

COPY dist/${JCP_VERSION} .

RUN chmod +x setup_console.sh && \
  bash setup_console.sh $JDK -jre $JDK/jre

COPY dist/${JCP_VERSION}/dependencies/commons-logging-1.1.1.jar $JDK/jre/lib/ext/
COPY dist/${JCP_VERSION}/dependencies/xmlsec-1.5.0.jar $JDK/jre/lib/ext/

FROM gradle:7.2-jdk8 as builder

WORKDIR /opt/smev

COPY . .
RUN gradle installDist --no-daemon

FROM jcp

WORKDIR /opt/smev

COPY --from=builder /opt/smev/build/install/smev/ ./

VOLUME [ "/var/opt/cprocsp/keys/root" ]

CMD ["bin/smev"]