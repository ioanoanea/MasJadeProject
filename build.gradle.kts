plugins {
    id("java")
    kotlin("jvm")
    id("application")
}

application {
    mainClass.set("jade.Boot")
}

// Configure run task for Jade arguments
tasks.named<JavaExec>("run") {
    args = listOf(
        "-gui",
        "-agents",
        "agent1:org.example.DiscoveryAgent;agent2:org.example.DiscoveryAgent;agent3:org.example.DiscoveryAgent"
    )
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(files("lib/jade.jar"))
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(23)
}