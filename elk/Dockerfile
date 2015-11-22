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

RUN gpg --keyserver ha.pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4
RUN arch="$(dpkg --print-architecture)" \
	&& set -x \
	&& curl -o /usr/local/bin/gosu -fSL "https://github.com/tianon/gosu/releases/download/1.3/gosu-$arch" \
	&& curl -o /usr/local/bin/gosu.asc -fSL "https://github.com/tianon/gosu/releases/download/1.3/gosu-$arch.asc" \
	&& gpg --verify /usr/local/bin/gosu.asc \
	&& rm /usr/local/bin/gosu.asc \
	&& chmod +x /usr/local/bin/gosu

RUN apt-key adv --keyserver ha.pool.sks-keyservers.net --recv-keys 46095ACC8548582C1A2699A9D27D666CD88E42B4

ENV ELASTICSEARCH_MAJOR 2.0
ENV ELASTICSEARCH_VERSION 2.0.0
ENV ELASTICSEARCH_REPO_BASE http://packages.elasticsearch.org/elasticsearch/2.x/debian

RUN echo "deb $ELASTICSEARCH_REPO_BASE stable main" > /etc/apt/sources.list.d/elasticsearch.list

RUN set -x \
	&& apt-get update \
	&& apt-get install -y --no-install-recommends elasticsearch=$ELASTICSEARCH_VERSION \
	&& rm -rf /var/lib/apt/lists/*

ENV PATH /usr/share/elasticsearch/bin:$PATH

RUN set -ex \
	&& for path in \
		/usr/share/elasticsearch/data \
		/usr/share/elasticsearch/logs \
		/usr/share/elasticsearch/config \
		/usr/share/elasticsearch/config/scripts \
	; do \
		mkdir -p "$path"; \
		chown -R elasticsearch:elasticsearch "$path"; \
	done

COPY elastic/config /usr/share/elasticsearch/config

VOLUME /usr/share/elasticsearch/data

RUN chown -R elasticsearch:elasticsearch /usr/share/elasticsearch/data

EXPOSE 9200 9300

# Logstash

ENV LOGSTASH_MAJOR 2.0
ENV LOGSTASH_VERSION 1:2.0.0-1

RUN echo "deb http://packages.elasticsearch.org/logstash/${LOGSTASH_MAJOR}/debian stable main" > /etc/apt/sources.list.d/logstash.list

RUN set -x \
  && apt-get update \
    && apt-get install -y --no-install-recommends logstash=$LOGSTASH_VERSION \
      && rm -rf /var/lib/apt/lists/*

ENV PATH /opt/logstash/bin:$PATH

COPY logstash/logstash-syslog.conf logstash-syslog.conf

EXPOSE 5000

# Kibana

RUN groupadd -r kibana && useradd -r -m -g kibana kibana

ENV KIBANA_VERSION 4.2.1
ENV KIBANA_SHA1 1c8d19d39a9ba10dc2431068e08497fbb416eba4

RUN set -x \
	&& curl -fSL "https://download.elastic.co/kibana/kibana/kibana-${KIBANA_VERSION}-linux-x64.tar.gz" -o kibana.tar.gz \
	&& echo "${KIBANA_SHA1}  kibana.tar.gz" | sha1sum -c - \
	&& mkdir -p /opt/kibana \
	&& tar -xz --strip-components=1 -C /opt/kibana -f kibana.tar.gz \
	&& chown -R kibana:kibana /opt/kibana \
	&& rm kibana.tar.gz

ENV PATH /opt/kibana/bin:$PATH

EXPOSE 5601

# Mem tweaks

ADD memtweak.sh memtweak.sh

RUN sudo bash memtweak.sh
 
# Ending

CMD gosu elasticsearch elasticsearch & \
          gosu logstash logstash agent -f logstash-syslog.conf & \
          gosu kibana kibana
