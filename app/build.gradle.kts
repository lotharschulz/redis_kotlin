plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.redisson:redisson:3.19.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    // https://github.com/lotharschulz/redis_kotlin/issues/21
    implementation("org.slf4j:slf4j-nop:2.0.7")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
}

application {
    mainClass.set("redis_kotlin.AppKt")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
