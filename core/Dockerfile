FROM ubuntu:15.04
MAINTAINER Vitaly Baum <vitaly.baum@gmail.com> 

# Oracle Java 8

RUN apt-get install software-properties-common -y

RUN apt-get install sudo curl -y

RUN sudo add-apt-repository ppa:webupd8team/java -y

RUN sudo apt-get update -y

RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections

RUN sudo apt-get install oracle-java8-installer -y

# Scala

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

WORKDIR /app

RUN sbt reload plugins -mem 512

ADD ./project/plugins.sbt /app/project/plugins.sbt
ADD ./build.sbt /app/build.sbt

RUN sbt compile -mem 512
#cache everything before if wasnt changed
ADD . /app

EXPOSE 1883

RUN sbt compile
RUN sbt stage

CMD /app/target/universal/stage/bin/jetmq -J-Xmx512m
