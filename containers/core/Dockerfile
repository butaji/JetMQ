FROM java:openjdk-8-jdk
MAINTAINER Vitaly Baum <vitaly.baum@gmail.com> 

ENV SBT_VERSION 0.13.9

RUN wget http://dl.bintray.com/sbt/debian/sbt-${SBT_VERSION}.deb -O /tmp/sbt.deb && \
      dpkg -i /tmp/sbt.deb && \
      rm -f /tmp/sbt.deb

RUN mkdir -p /root/.ssh/
RUN echo -e "Host github.com\n\tStrictHostKeyChecking no\n" >> /root/.ssh/config

RUN mkdir /app
WORKDIR /app

RUN git clone http://github.com/butaji/JetMQ

WORKDIR JetMQ
EXPOSE 1883

RUN sbt compile
CMD sbt "run-main net.jetmq.broker.Main"
