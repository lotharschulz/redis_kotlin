package redis_kotlin

import org.redisson.api.RedissonClient

sealed class RClient {
    data class Success(val redissonClient: RedissonClient) : RClient()
    data class Failure(val error: String) : RClient()
}
