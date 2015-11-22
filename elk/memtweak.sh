echo "elasticsearch hard memlock 100000" > /etc/security/limits.conf

echo "ES_HEAP_SIZE=128m
MAX_LOCKED_MEMORY=100000
ES_JAVA_OPTS=-server" > /etc/default/elasticsearch

echo "index.number_of_shards: 1
index.number_of_replicas: 0" > /etc/elasticsearch/elasticsearch.yml 
