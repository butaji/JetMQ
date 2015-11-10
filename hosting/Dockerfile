FROM ubuntu
MAINTAINER Vitaly Baum <vitaly.baum@gmail.com> 

RUN mkdir -p /root/.ssh/
RUN echo -e "Host github.com\n\tStrictHostKeyChecking no\n" >> /root/.ssh/config

RUN mkdir /app
WORKDIR /app

RUN apt-get install git -y

RUN git clone http://github.com/butaji/JetMQ

WORKDIR JetMQ

RUN apt-get install apt-transport-https -y

RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list
RUN sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 642AC823
RUN apt-get update -y
RUN apt-get install sbt -y

RUN sbt compile -mem 500 | tee -a app.log
RUN sbt "run-main net.jetmq.broker.Main" -mem 500 | tee -a app.log

EXPOSE 1883
