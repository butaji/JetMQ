FROM java:8
MAINTAINER Vitaly Baum <vitaly.baum@gmail.com> 

ENV SCALA_VERSION 2.11.7
ENV SBT_VERSION 0.13.9

# Install Scala
RUN \
  cd /root && \
    curl -o scala-$SCALA_VERSION.tgz http://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz && \
      tar -xf scala-$SCALA_VERSION.tgz && \
        rm scala-$SCALA_VERSION.tgz && \
          echo >> /root/.bashrc && \
            echo 'export PATH=~/scala-$SCALA_VERSION/bin:$PATH' >> /root/.bashrc

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
    dpkg -i sbt-$SBT_VERSION.deb && \
      rm sbt-$SBT_VERSION.deb && \
        apt-get update && \
          apt-get install sbt


RUN mkdir /app
ADD . /app
WORKDIR /app

EXPOSE 1883

RUN sbt compile
CMD sbt "run-main net.jetmq.broker.Main"
