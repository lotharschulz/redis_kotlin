package redis_kotlin

import org.redisson.Redisson
import org.redisson.api.*
import org.redisson.client.RedisConnectionException
import org.redisson.config.Config
import java.io.Serializable


data class Book(val pages: Int, val chapter: Int, val author: String) : Serializable
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

    private fun atomicLong(redisson: RedissonClient, newValue: Long) {
        printHelper("atomicLong")
        val myAtomicLong: RAtomicLong = redisson.getAtomicLong("myAtomicLong")
        println("initial myAtomicLong: $myAtomicLong")
        myAtomicLong.set(newValue)
        println("myAtomicLong after set to $newValue: $myAtomicLong")
        myAtomicLong.incrementAndGet()
        println("myAtomicLong after increment and set: $myAtomicLong")
        myAtomicLong.get()
        println("myAtomicLong after get: $myAtomicLong")
        myAtomicLong.unlink() // clean up, happens async
        println("myAtomicLong after unlink/cleanup: $myAtomicLong")
    }

    private fun atomicLongAsync(redisson: RedissonClient, newValue: Long) {
        printHelper("atomicLong async interface")
        val myAtomicLong: RAtomicLong = redisson.getAtomicLong("myAtomicLong")
        println("initial myAtomicLong: $myAtomicLong")
        val setFuture: RFuture<Void> = myAtomicLong.setAsync(newValue)
        try {
            val setResult = setFuture.toCompletableFuture().get()
            println("async setResult: $setResult")
        } catch (e: Exception) {
            println("after async set an exception happened: ${e.localizedMessage}")
        }

        val getFuture: RFuture<Long> = myAtomicLong.getAsync()
        try {
            val result = getFuture.toCompletableFuture().get()
            println("myAtomicLong after async get result: $result")
        } catch (e: Exception) {
            println("after async get an exception happened: ${e.localizedMessage}")
        }

        val igFuture: RFuture<Long> = myAtomicLong.incrementAndGetAsync()
        try {
            val result = igFuture.toCompletableFuture().get()
            println("myAtomicLong after async increment and set result: $result")
        } catch (e: Exception) {
            println("after async set an exception happened: ${e.localizedMessage}")
        }

        val getFuture2: RFuture<Long> = myAtomicLong.getAsync()
        try {
            val result = getFuture2.toCompletableFuture().get()
            println("myAtomicLong after async get result: $result")
        } catch (e: Exception) {
            println("after async get an exception happened: ${e.localizedMessage}")
        }

        myAtomicLong.unlink() // clean up, happens async
        println("myAtomicLong after unlink/cleanup: $myAtomicLong")
    }

    // todo: wrap into coroutines

    // todo reactive interface
    /*
    RedissonReactiveClient redisson = redissonClient.reactive();
    RAtomicLongReactive atomicLong = redisson.getAtomicLong("myAtomicLong");

    Mono<Void> setMono = atomicLong.set(3);
    Mono<Long> igMono = atomicLong.incrementAndGet();
    RFuture<Long> getMono = atomicLong.getAsync();
     */

    // todo RxJava3
    /*
    RedissonRxClient redisson = redissonClient.rxJava();
    RAtomicLongRx atomicLong = redisson.getAtomicLong("myAtomicLong");

    Completable setMono = atomicLong.set(3);
    Single<Long> igMono = atomicLong.incrementAndGet();
    Single<Long> getMono = atomicLong.getAsync();
     */

    private fun bucket(redissonClient: RedissonClient, bucketName: String, value: String) {
        printHelper("bucket")
        val bucket = redissonClient.getBucket<String>(bucketName)
        bucket.set(value)
        println("bucket.get(): ${bucket.get()}")
    }

    private fun `object`(redissonClient: RedissonClient, bookPages: Int, bookChapter: Int, bookAuthor: String) {
        printHelper("object")
        val bucket: RBucket<Book> = redissonClient.getBucket("book")
        val book1 = Book(bookPages, bookChapter, bookAuthor)
        bucket.set(book1)
        val getBook1: Book = bucket.get()
        println("getBook1 : $getBook1")
        val book2 = Book(bookPages + 2, bookChapter + 2, "$bookAuthor of book 2")
        val setIfAbsent: Boolean = bucket.setIfAbsent(book2)
        println("setIfAbsent : $setIfAbsent")
        val getBook2: Book = bucket.get()
        println("getBook2 : $getBook2")
        val book3 = Book(bookPages + 3, bookChapter + 3, "$bookAuthor of book 3")
        val book4 = Book(bookPages + 4, bookChapter + 4, "$bookAuthor of book 4")
        val compareAndSet: Boolean = bucket.compareAndSet(book3, book4)
        println("compareAndSet : $compareAndSet")
        val getBook3or4: Book = bucket.get()
        println("getBook3or4 : $getBook3or4")
        val book5 = Book(bookPages + 5, bookChapter + 5, "$bookAuthor of book 5")
        val currentBook: Book = bucket.getAndSet(book5)
        println("currentBook : $currentBook")
        val getBook5: Book = bucket.get()
        println("getBook5 : $getBook5")
    }

    private fun keys(redissonClient: RedissonClient, keyName1: String, keyName2: String) {
        printHelper("keys")
        val rKeys: RKeys = redissonClient.keys
        redissonClient.getBucket<Int>(keyName1).set(1)
        redissonClient.getBucket<Int>(keyName2).set(1)
        val keys = rKeys.keys
        println("default sorted (almost lexicographically) keys:")
        keys.sorted().forEach { println(it) }
    }

    private fun printHelper(content: String) {
        println("------------------------------")
        println("--- $content function output: ")
    }
    fun doRedisStuff(): Boolean {
        val redisson = redissonClient()
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
        return if (redisson != null) {
            atomicLong(redisson, 3L)
            atomicLongAsync(redisson, 3L)
            bucket(redisson, "foo", "bar") // buckets
            `object`(redisson, 100, 10, "some author")
            keys(redisson, "test1", "test2")
            redisson.shutdown()
            true
        } else {
            false
        }
    }
}

fun main() {
    App().doRedisStuff()
}
