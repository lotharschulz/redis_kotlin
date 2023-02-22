https://redis.io/docs/stack/get-started/install/docker/

run docker locally: 

```shell
docker run -d --name my-redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```

Connect with redis-cli:

```shell
docker exec -it my-redis-stack redis-cli
```

stop the container

```shell
docker stop my-redis-stack && docker rm my-redis-stack
```
