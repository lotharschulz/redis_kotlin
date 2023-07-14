package redis_kotlin

import org.redisson.api.RedissonClient

sealed interface RClientInterface
sealed class RClient {
    data class Success(val redissonClient: RedissonClient) : RClient(), RClientInterface
    data class Failure(val error: String) : RClient(), RClientInterface
}
