package redis_kotlin

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.client.RedisConnectionException
import org.redisson.config.Config


class App {
    private fun redissonClient(): RedissonClient? { // TODO no nullable return value as in https://www.lotharschulz.info/2022/07/28/replace-null-with-amazing-kotlin-and-java-sealed-classes-interfaces/
        // connects to 127.0.0.1:6379 by default
        val config = Config()
        config.useSingleServer().address = "redis://localhost:6379" // in case you need to set it
        return try {
            Redisson.create(config)
        } catch (rce: RedisConnectionException) {
            null
        }
    }

    private fun bucketSetGet(redissonClient: RedissonClient, bucketName: String, value: String){
        val bucket = redissonClient.getBucket<String>(bucketName)
        bucket.set(value)
        println( "bucket.get(): ${bucket.get()}" )
    }

    fun doRedisStuff(): Boolean{
        val redisson = redissonClient()
        // keys
        // objects
        // AtomicLong
        // Topic
        // Collections
        // Map
        // Set
        // List
        // Multi(Lock)
        // Services
        // Pipelining
        // Scripting
        return try {
            val redissonNotNull = checkNotNull(redisson) { "State must be set beforehand" }
            bucketSetGet(redissonNotNull, "foo", "bar") // buckets
            // close the client
            redissonNotNull.shutdown()
            true;
        } catch (e: Exception) {
            println("can not connect to redis, please check if redis container is running")
            false
        }
    }

}

fun main() {
    // make sure REDIS runs on local host first
    App().doRedisStuff()
}
