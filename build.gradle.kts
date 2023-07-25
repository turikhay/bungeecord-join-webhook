plugins {
    java
    id("io.freefair.lombok") version "8.1.0"
    id("net.minecrell.plugin-yml.bungee") version "0.6.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

bungee {
    name = "BungeeCordJoinWebhook"
    main = "com.turikhay.mc.bungee.BungeeCordJoinWebhook"
    author = "turikhay"
}

repositories {
    mavenCentral()
    maven {
        name = "Sonatype"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation("com.github.spotbugs:spotbugs-annotations:4.7.3")
    compileOnly("net.md-5:bungeecord-api:1.20-R0.1-SNAPSHOT")
}

tasks {
    jar {
        archiveFileName = "BungeeCordJoinWebhook.jar"
    }
}
