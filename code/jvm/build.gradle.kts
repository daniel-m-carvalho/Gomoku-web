import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
}

group = "pt.isel.daw"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.1.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")

    // For validation
    implementation("org.springframework.boot:spring-boot-starter-validation:3.0.4")

    // for JDBI
    implementation("org.jdbi:jdbi3-core:3.37.1")
    implementation("org.jdbi:jdbi3-kotlin:3.37.1")
    implementation("org.jdbi:jdbi3-postgres:3.37.1")
    implementation("org.postgresql:postgresql:42.5.4")

    // To use Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // To get password encode
    implementation("org.springframework.security:spring-security-core:6.0.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux:3.0.4")
    testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

/**
 * DB related tasks
 * - To run `psql` inside the container, do
 *      docker exec -ti db-tests psql -d db -U dbuser -W
 *   and provide it with the same password as define on `tests/Dockerfile-db-test`
 */
task<Exec>("dbTestsUp") {
    commandLine("docker-compose", "up", "-d", "--build", "--force-recreate", "db-tests")
}

task<Exec>("dbTestsWait") {
    commandLine("docker", "exec", "db-tests", "/app/bin/wait-for-postgres.sh", "localhost")
    dependsOn("dbTestsUp")
}

task<Exec>("dbTestsDown") {
    commandLine("docker-compose", "down")
}

tasks.named("check") {
    dependsOn("dbTestsWait")
    finalizedBy("dbTestsDown")
}

/**
 * Docker related tasks
 * */

tasks.register<Copy>("extractUberJar") {
    dependsOn("assemble")
    from(zipTree("$buildDir/libs/${rootProject.name}-$version.jar"))
    into("build/dependency")
}

task<Exec>("composeUp") {
    commandLine("docker-compose", "up", "--build", "--force-recreate", "--scale", "spring-service=3")
    dependsOn("extractUberJar")
}

task<Exec>("composeDown") {
    commandLine("docker-compose", "down")
}