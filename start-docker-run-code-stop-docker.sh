#!/bin/bash
# http://redsymbol.net/articles/unofficial-bash-strict-mode/
set -euo pipefail
IFS=$'\n\t'

dockerContainerName=my-redis-stack

if [ ! "$(docker ps -a -q -f name=$dockerContainerName)" ]; then
    # run your container
    docker run -d --name $dockerContainerName -p 6379:6379 -p 8001:8001 redis/redis-stack:latest

    # run the code
    ./gradlew run

    # cleanup
    docker stop $dockerContainerName && docker rm $dockerContainerName
fi
