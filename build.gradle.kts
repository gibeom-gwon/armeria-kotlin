import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.spring") version "2.4.21"
    id("org.springframework.boot") version "4.1.1"
    id("com.google.protobuf") version "0.10.0"
}

group = "dev.stackframe.armeria.kotlin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.9"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.84.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.3:jdk8@jar"
        }
    }

    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(platform("com.linecorp.armeria:armeria-bom:1.42.0"))
    implementation(platform("com.google.protobuf:protobuf-bom:3.25.9"))
    implementation(platform("io.grpc:grpc-bom:1.84.0"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

    implementation("com.linecorp.armeria:armeria")
    implementation("com.linecorp.armeria:armeria-kotlin")
    implementation("com.linecorp.armeria:armeria-grpc")
    implementation("com.linecorp.armeria:armeria-grpc-kotlin")
    implementation("com.linecorp.armeria:armeria-logback14")
    implementation("com.linecorp.armeria:armeria-spring-boot4-starter")

    implementation("com.google.protobuf:protobuf-kotlin")
    implementation("io.grpc:grpc-kotlin-stub:1.4.3")
    implementation("io.grpc:grpc-protobuf")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
