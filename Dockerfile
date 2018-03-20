FROM openjdk:8-jdk

ARG version=0.2.0

RUN apt-get update && \
    apt-get install -y wget unzip && \
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

COPY janusgraph/memory.properties /work/janusgraph/conf/memory.properties
COPY janusgraph/gremlin-server.yaml /work/janusgraph/conf/gremlin-server/gremlin-server.yaml
COPY janusgraph/empty-sample.groovy /work/janusgraph/scripts/empty-sample.groovy
COPY janusgraph/air-routes-small.graphml /work/janusgraph/scripts/air-routes-small.graphml
COPY janusgraph/marvel_movie_graph.graphml /work/janusgraph/scripts/marvel_movie_graph.graphml
COPY janusgraph/janus-inmemory.groovy /work/janusgraph/scripts/janus-inmemory.groovy
COPY janusgraph/janus-inmemory-marvel.groovy /work/janusgraph/scripts/janus-inmemory-marvel.groovy
COPY janusgraph/run.sh /work/janusgraph/run.sh

RUN apt-get -y install apache2

RUN cd /var/www/html && \ 
    git clone https://github.com/rgomesf/graphexp.git

WORKDIR /work/janusgraph

EXPOSE 80
EXPOSE 443
EXPOSE 8182

RUN ["chmod", "+x", "/work/janusgraph/run.sh"]

ENTRYPOINT ["sh", "/work/janusgraph/run.sh"]