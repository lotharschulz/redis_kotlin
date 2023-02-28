package redis_kotlin

import org.redisson.Redisson
import org.redisson.api.RBucket
import org.redisson.api.RKeys
import org.redisson.api.RedissonClient
import org.redisson.client.RedisConnectionException
import org.redisson.config.Config
import java.io.Serializable
import javax.xml.crypto.dsig.keyinfo.KeyName

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

    private fun keysSetGet(redissonClient: RedissonClient, keyName1: String, keyName2: String) {
        printHelper("keysSetGet")
        val rKeys: RKeys = redissonClient.keys
        redissonClient.getBucket<Int>(keyName1).set(1)
        redissonClient.getBucket<Int>(keyName2).set(1)
        val keys = rKeys.keys
        println("default sorted (almost lexicographically) keys:")
        keys.sorted().forEach { println(it) }
    }

    private fun bucketGetSet(redissonClient: RedissonClient, bucketName: String, value: String) {
        printHelper("bucketGetSet")
        val bucket = redissonClient.getBucket<String>(bucketName)
        bucket.set(value)
        println("bucket.get(): ${bucket.get()}")
    }

    private fun objectGetSet(redissonClient: RedissonClient, bookPages: Int, bookChapter: Int, bookAuthor: String) {
        printHelper("objectGetSet")
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
        val redissonNotNull = checkNotNull(redisson) { "State must be set beforehand" }
        return if (redisson != null) {
            bucketGetSet(redisson, "foo", "bar") // buckets
            objectGetSet(redisson, 100, 10, "some author")
            keysSetGet(redisson, "test1", "test2")
            redissonNotNull.shutdown()
            true
        } else {
            false
        }
    }
}

fun main() {
    // make sure REDIS runs on local host first
    App().doRedisStuff()
}
