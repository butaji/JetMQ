FROM ubuntu:15.04

RUN mkdir /stack
WORKDIR /stack

# Oracle Java 8

RUN apt-get install software-properties-common -y

RUN apt-get install sudo curl -y

RUN sudo add-apt-repository ppa:webupd8team/java -y

RUN sudo apt-get update -y

RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections

RUN sudo apt-get install oracle-java8-installer -y

# Elastic

RUN sudo wget -qO - https://packages.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -

RUN echo "deb http://packages.elastic.co/elasticsearch/2.x/debian stable main" | sudo tee -a /etc/apt/sources.list.d/elasticsearch-2.x.list

RUN sudo apt-get update

RUN sudo apt-get -y install elasticsearch

RUN sudo echo "network.host: localhost" > /etc/elasticsearch/elasticsearch.yml

RUN sudo service elasticsearch restart

RUN sudo update-rc.d elasticsearch defaults 95 10

RUN sudo service elasticsearch stop

# Kibana

RUN sudo groupadd -g 999 kibana

RUN sudo useradd -u 999 -g 999 kibana

WORKDIR ~

RUN wget https://download.elastic.co/kibana/kibana/kibana-4.2.0-linux-x64.tar.gz

RUN tar xvf kibana-4.2.0-linux-x64.tar.gz

RUN sudo echo "server.host: \"localhost\"" | tee kibana-4.2.0-linux-x64/config/kibana.yml

RUN sudo mkdir -p /opt/kibana

RUN sudo cp -R kibana-4.2.0-linux-x64/* /opt/kibana/

RUN sudo chown -R kibana: /opt/kibana

RUN cd /etc/init.d && sudo curl -o kibana https://gist.githubusercontent.com/thisismitch/8b15ac909aed214ad04a/raw/fc5025c3fc499ad8262aff34ba7fde8c87ead7c0/kibana-4.x-init

RUN cd /etc/default && sudo curl -o kibana https://gist.githubusercontent.com/thisismitch/8b15ac909aed214ad04a/raw/fc5025c3fc499ad8262aff34ba7fde8c87ead7c0/kibana-4.x-default

RUN sudo chmod +x /etc/init.d/kibana

RUN sudo service kibana restart

RUN sudo update-rc.d kibana defaults 96 9

RUN sudo service kibana stop

# Nginx
RUN sudo apt-get install nginx apache2-utils -y

RUN sudo htpasswd -c /etc/nginx/htpasswd.users kibanaadmin

ADD nginx /etc/nginx/sites-available/default

RUN sudo service nginx restart

RUN sudo service nginx stop

# Logstash 

RUN echo 'deb http://packages.elasticsearch.org/logstash/2.0/debian stable main' | sudo tee /etc/apt/sources.list.d/logstash.list

RUN sudo apt-get update -y

RUN sudo apt-get install logstash -y

ADD 01-lumberjack-input.conf /etc/logstash/conf.d/01-lumberjack-input.conf

ADD 10-syslog.conf /etc/logstash/conf.d/10-syslog.conf

ADD 30-lumberjack-output.conf /etc/logstash/conf.d/30-lumberjack-output.conf

RUN sudo service logstash restart

RUN sudo update-rc.d logstash defaults 96 9

RUN sudo service logstash stop

CMD sudo service elasticsearch restart
#CMD sudo service nginx restart && sudo service elasticsearch restart 
# && restart sudo service logstash restart && sudo service kibana restart 
