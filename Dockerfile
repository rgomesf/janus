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

COPY janusgraph/gremlin-server.yaml /work/janusgraph/conf/gremlin-server/gremlin-server.yaml
COPY janusgraph/janusgraph.properties /work/janusgraph/janusgraph.properties
COPY janusgraph/empty-sample.groovy /work/janusgraph/scripts/empty-sample.groovy
COPY janusgraph/air-routes-small.graphml /work/janusgraph/scripts/air-routes-small.graphml
COPY janusgraph/janus-inmemory.groovy /work/janusgraph/scripts/janus-inmemory.groovy
COPY janusgraph/run.sh /work/janusgraph/run.sh

RUN apt-get -y install apache2

RUN git clone https://github.com/rgomesf/graphexp.git /var/www/html

WORKDIR /work/janusgraph

RUN bin/gremlin-server.sh -i org.janusgraph janusgraph-all $version

EXPOSE 80
EXPOSE 443

ENTRYPOINT ["run.sh"]