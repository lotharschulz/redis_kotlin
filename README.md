# How to Redis & Redisson with Kotlin

```shell
./start-docker-run-code-stop-docker.sh 
```
([*nix](https://en.wikipedia.org/wiki/Unix-like) systems)

---

_individual steps_

### start redis with docker

```shell
docker run -d --name my-redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```

optional: connect with redis-cli:

```shell
docker exec -it my-redis-stack redis-cli
```

### run the code

```shell
./gradlew run
```

### stop redis with docker

```shell
docker stop my-redis-stack && docker rm my-redis-stack
```
