package redis_kotlin

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config


class App {
    private fun redissonClient(): RedissonClient {
        // connects to 127.0.0.1:6379 by default
        val config = Config()
        config.useSingleServer().address = "redis://localhost:6379" // in case you need to set it
        return Redisson.create(config)
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
        bucketSetGet(redisson, "foo", "bar") // buckets
        // close the client
        redisson.shutdown()
        return true;
    }

}

fun main() {
    // make sure REDIS runs on local host first
    App().doRedisStuff()
}
