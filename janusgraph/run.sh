#!/bin/env bash

set -e
rm -f /var/run/apache2/apache2.pid
/usr/sbin/apache2ctl -D FOREGROUND
/work/janusgraph/bin/gremlin-server.sh
