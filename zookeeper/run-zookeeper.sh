#!/usr/bin/env bash

zk_servers=$(cat - <<EOF
server.1=localhost:2888:3888;2181
server.2=localhost:2889:3889;2182
server.3=localhost:2890:3890;2183
EOF
)

docker run -it --network=host \
    -e ZOO_MY_ID="$1" \
    -e ZOO_SERVERS="${zk_servers}" \
    zookeeper:3.5.5
