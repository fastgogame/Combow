plugins {
    id("java")
    application
}

group = "io.fastgogame"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.dv8tion:JDA:5.0.0-beta.12")
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("org.seleniumhq.selenium:selenium-java:4.11.0")
    implementation("org.postgresql:postgresql:42.6.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("io.fastgogame.CombowBot")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes["Main-Class"] = "io.fastgogame.CombowBot"
    }
    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })
}