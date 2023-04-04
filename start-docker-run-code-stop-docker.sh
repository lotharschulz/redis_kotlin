#!/bin/bash
# http://redsymbol.net/articles/unofficial-bash-strict-mode/
set -euo pipefail
IFS=$'\n\t'

dockerContainerName=my-redis-stack

cleanup () {
    echo "cleanup"
    docker stop $dockerContainerName && docker rm $dockerContainerName
}

runcode () {
  ./gradlew run
}

if [ "$(docker ps -aq -f status=exited -f name=$dockerContainerName)" ];
then
  cleanup
fi

if [ ! "$(docker ps -a -q -f name=$dockerContainerName)" ];
then
  # run your container
  docker run -d --name $dockerContainerName -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
  runcode
  cleanup
else
  runcode
  cleanup
fi
