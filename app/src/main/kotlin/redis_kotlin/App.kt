package redis_kotlin

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.redisson.Redisson
import org.redisson.RedissonMultiLock
import org.redisson.api.*
import org.redisson.client.RedisConnectionException
import org.redisson.config.Config
import reactor.core.publisher.Mono
import java.io.Serializable


data class Book(val pages: Int, val chapter: Int, val author: String) : Serializable

interface MyTestInterface {
    fun doubleStr(input: String): String
}

class MyTestImpl : MyTestInterface {
    override fun doubleStr(input: String): String {
        return "$input-$input"
    }
}

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
        val myAtomicLong: RAtomicLong = redisson.getAtomicLong("myAtomicLongAsync")
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

    private fun atomicLongReactive(redisson: RedissonClient, newValue: Long) {
        printHelper("atomicLong reactive interface")
        val redissonReactive: RedissonReactiveClient = redisson.reactive()
        val myAtomicLong: RAtomicLongReactive = redissonReactive.getAtomicLong("myAtomicLongReactive")

        val setMono: Mono<Void> = myAtomicLong.set(newValue)
        setMono.doOnNext {i -> println("setMono next i: $i")}
            .doOnSuccess{i -> println("setMono success i: $i")}
            .doOnError{e -> println("setMono error i: ${e.localizedMessage}")}
            .block()

        val getMono: Mono<Long> = myAtomicLong.get()
        getMono.doOnNext {i -> println("getMono next i: $i")}
            .doOnSuccess{i -> println("getMono success i: $i")}
            .doOnError{e -> println("getMono error i: ${e.localizedMessage}")}
            .block();

        val igMono: Mono<Long> = myAtomicLong.incrementAndGet()
        igMono.doOnNext {i -> println("igMono next i: $i")}
            .doOnSuccess{i -> println("igMono success i: $i")}
            .doOnError{e -> println("igMono error i: ${e.localizedMessage}")}
            .block()
    }

    // check one more time
    private fun atomicLongRXJava3(redisson: RedissonClient, newValue: Long) {
        printHelper("atomicLong RX Java3")
        val redissonReactive: RedissonRxClient = redisson.rxJava()
        val atomicLong: RAtomicLongRx = redissonReactive.getAtomicLong("myAtomicLongReactiveRX")
        val setMono: Completable = atomicLong.set(newValue)
        setMono.doOnError { e -> println("setMono error: ${e.localizedMessage}") }
        val getMono = atomicLong.get()
        getMono.subscribe(
            { i -> println("getMono: $i") },
            { e -> println("getMono error: ${e.localizedMessage}") }
        )
        val igMono: Single<Long> = atomicLong.incrementAndGet()
        igMono.subscribe(
            { i -> println("igMono: $i") },
            { e -> println("igMono error: ${e.localizedMessage}") }
        )
    }

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

    private fun topic(redissonClient: RedissonClient, message: String){
        printHelper("topic")
        val rTopic: RTopic = redissonClient.getTopic("myTopic")
        val listenerId = rTopic.addListener(String::class.java) { channel, msg ->
            println("channel: $channel, Message: $msg")
        }
        rTopic.publish(message)
        rTopic.removeListener(listenerId)
    }

    private fun collections(redissonClient: RedissonClient, key: String, value: String){
        printHelper("collections")
        // Map
        val map: RMap<String, String> = redissonClient.getMap("myMap")
        val prevString: String? = map.put(key, value)
        println("prevString: $prevString")
        println("map get: ${map[key]}")
        val putIfAbsentStringValue: String = "42"
        println("before putIfAbsent value: $putIfAbsentStringValue")
        val putIfAbsentString: String? = map.putIfAbsent(key, putIfAbsentStringValue)
        println("after putIfAbsentString: $putIfAbsentString")
        println("map get: ${map[key]}")
        val removedString: String? = map.remove(key)
        println("removedString: $removedString")
        println("map get: ${map[key]}")
        // distributed collections also include
        // Map, Multimap, SortedSet, ScoredSortedSet, LexSortedSet
        // Queue, Deque, BlockingQueue, BoundedBlockingQueue, BlockingDeque
        // BlockingFairQueue, DelayedQueue, PriorityQueue, PriorityDeque
        // https://github.com/redisson/redisson/wiki/7.-distributed-collections
        // down below Set & List
    }

    private fun set(redissonClient: RedissonClient, pages: Int, chapter: Int, author: String){
        printHelper("set")
        val book = Book(pages, chapter, author)
        val rSet: RSet<Book> = redissonClient.getSet("book-set")
        rSet.add(book)
        rSet.readAll()
        println( "book set contains $book: ${rSet.contains(book)}" )
        rSet.remove(book)
        // more on distributed collection set
        // https://github.com/redisson/redisson/wiki/7.-distributed-collections/#73-set
    }

    private fun list(redissonClient: RedissonClient, pages: Int, chapter: Int, author: String){
        printHelper("list")
        val book = Book(pages, chapter, author)
        val ledgerList: RList<Book> = redissonClient.getList("myList")
        ledgerList.add(book)
        val listData = ledgerList.get(0)
        println(listData)
        ledgerList.remove(book)
        // more on distributed collection list
        // https://github.com/redisson/redisson/wiki/7.-distributed-collections/#77-list
    }

    private fun scripting(redissonClient: RedissonClient, value: String){
        printHelper("scripting")
        val bucketName = "myScriptingBucket"
        redissonClient.getBucket<String>(bucketName).set(value)
        val result: String = redissonClient.script.eval(
            RScript.Mode.READ_ONLY,
            "return redis.call('get', '$bucketName')", RScript.ReturnType.VALUE
        )
        println(result)
    }

    private fun pipeline(redissonClient: RedissonClient, value1: Int, value2: Int, value3: Int, value4: Int){
        printHelper("pipeline")
        val batch: RBatch = redissonClient.createBatch()
        val rf1: RFuture<Boolean>? = batch.getMap<Int, Int>("pipeline").fastPutAsync(value1, value2)
        val rf2: RFuture<Int>? = batch.getMap<Int, Int>("pipeline").putAsync(value3, value4)
        batch.execute()
        rf1?.toCompletableFuture()?.thenApply { println("rf1 fastPutAsync result: $it") }
        rf2?.toCompletableFuture()?.thenApply { println("rf2 putAsync result: $it") }
    }

    private fun multiLock(redissonClient: RedissonClient){
        printHelper("multiLock")
        val lock1: RLock = redissonClient.getLock("l1")
        val lock2: RLock = redissonClient.getLock("l2")
        val lock3: RLock = redissonClient.getLock("l3")
        val lock4: RLock = redissonClient.getLock("l4")
        val lock5: RLock = redissonClient.getLock("l5")

        val lock = RedissonMultiLock(lock1, lock2, lock3, lock4, lock5)
        lock.lock()
        println("locked")
        // perform 1 second "long" running operation...
        println("wait")
        Thread.sleep(1000)
        lock.unlock()
        println("unlocked")
    }

    private fun remoteServiceServer(redissonClient: RedissonClient){
        val remoteService: RRemoteService = redissonClient.remoteService
        val myTestImpl = MyTestImpl()

        // register remote service before any remote invocation
        // can handle only 1 invocation concurrently

        // register remote service before any remote invocation
        // can handle only 1 invocation concurrently
        remoteService.register(MyTestInterface::class.java, myTestImpl)

        // register remote service able to handle up to 12 invocations concurrently
        // register remote service able to handle up to 12 invocations concurrently
        remoteService.register(MyTestInterface::class.java, myTestImpl, 12)
    }

    private fun printHelper(content: String) {
        println("------------------------------")
        println("--- $content function output: ")
    }
    fun doRedisStuff(): Boolean {
        val redisson = redissonClient()
        // Services
        // Pipelining
        return if (redisson != null) {
            atomicLong(redisson, 3L)
            atomicLongAsync(redisson, 3L)
            atomicLongReactive(redisson, 3L)
            atomicLongRXJava3(redisson, 3L)
            bucket(redisson, "foo", "bar") // buckets
            `object`(redisson, 100, 10, "some author")
            topic(redisson, "new message")
            keys(redisson, "test1", "test2")
            collections(redisson, "321", "value")
            set(redisson, 42, 88, "icke")
            list(redisson, 24, 33, "you")
            scripting(redisson, "foo-bar")
            pipeline(redisson, 1, 2, 3, 4)
            multiLock(redisson)
            redisson.shutdown()
            true
        } else {
            println("Could not connect to REDIS. Please check the connection and if it running.")
            false
        }
    }
}

fun main() {
    App().doRedisStuff()
}
