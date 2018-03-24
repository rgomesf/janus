# janus

Dockerfile with janusgraph + graphexp + apache2 + sample data 

Thanks to:
https://github.com/bricaud/graphexp
https://github.com/krlawrence/graph



git config --global core.autocrlf true

git clone https://github.com/rgomesf/janus.git

docker build --rm -f Dockerfile -t janus:latest . 

docker run --rm --name=workshop -d -p 80:80 -p 8182:8182 janus 

docker exec -it workshop bash

/work/janusgraph/bin/gremlin.sh

:remote connect tinkerpop.server conf/remote.yaml session-managed

:remote console





docker stop workshop
docker build --rm -f Dockerfile -t janus:latest .
docker run --rm --name=workshop -d -p 80:80 -p 8182:8182 janus
docker exec -it workshop bash
/work/janusgraph/bin/gremlin.sh
:remote connect tinkerpop.server conf/remote.yaml session-managed
:remote console
