FROM centos
MAINTAINER Vitaly Baum <vitaly.baum@gmail.com> 

RUN yum update -y

RUN yum install curl -y

RUN curl https://bintray.com/sbt/rpm/rpm | tee /etc/yum.repos.d/bintray-sbt-rpm.repo

RUN yum install git sbt -y

RUN mkdir -p /root/.ssh/
RUN echo -e "Host github.com\n\tStrictHostKeyChecking no\n" >> /root/.ssh/config

RUN git clone http://github.com/butaji/JetMQ ~/app

WORKDIR ~/app
RUN sbt "run-main net.jetmq.broker.Main" | tee app.log

EXPOSE 1883
