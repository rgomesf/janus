FROM openjdk:8-jdk

ARG version=0.2.0

RUN apt-get update && \
    apt-get install -y wget unzip vim && \
    mkdir /work && \
    cd /work && \
    wget https://github.com/JanusGraph/janusgraph/releases/download/v$version/janusgraph-${version}-hadoop2.zip && \
    unzip janusgraph-$version-hadoop2.zip && \
    rm janusgraph-$version-hadoop2.zip && \
    mv janusgraph-* janusgraph

#COPY janusgraph-0.2.0-hadoop2.zip /work/janusgraph-0.2.0-hadoop2.zip

#RUN cd /work && \
#    unzip janusgraph-0.2.0-hadoop2.zip && \
#    rm janusgraph-0.2.0-hadoop2.zip && \
#    mv janusgraph-* janusgraph


RUN apt-get -y install apache2

RUN cd /var/www/html && \ 
    git clone https://github.com/rgomesf/graphexp.git

WORKDIR /work/janusgraph

COPY janusgraph/graphConf.js /var/www/html/graphexp/scripts/graphConf.js
COPY janusgraph/memory.properties /work/janusgraph/conf/memory.properties
COPY janusgraph/gremlin-server.yaml /work/janusgraph/conf/gremlin-server/gremlin-server.yaml

COPY janusgraph/graphml/marvel.graphml /work/janusgraph/graphml/marvel.graphml
COPY janusgraph/graphml/events.graphml /work/janusgraph/graphml/events.graphml

COPY janusgraph/janus-inmemory-marvel.groovy /work/janusgraph/scripts/janus-inmemory-marvel.groovy
COPY janusgraph/janus-inmemory-events.groovy /work/janusgraph/scripts/janus-inmemory-events.groovy

COPY janusgraph/run.sh /work/janusgraph/run.sh

EXPOSE 80
EXPOSE 443
EXPOSE 8182

RUN ["chmod", "+x", "/work/janusgraph/run.sh"]

ENTRYPOINT ["sh", "/work/janusgraph/run.sh"]