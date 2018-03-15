#!/bin/sh

echo "Starting Apache2 server"
set -e
rm -f /var/run/apache2/apache2.pid
/usr/sbin/apache2ctl start
echo "Apache2 server started"
echo "Starting janusgraph"
/work/janusgraph/bin/gremlin-server.sh