import org.gradle.api.JavaVersion.VERSION_17

plugins {
    id("java")
    application
}

group = "io.github.marcusdunn"
val artifact = "csc480_group_project"
version = "1.0-SNAPSHOT"


application {
    mainClass.set("${group}.$artifact.Main")
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.apache.commons:commons-csv:1.9.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.3.0.202209071007-r")
    implementation("org.slf4j:slf4j-simple:2.0.3")

}

tasks.withType<JavaCompile>() {
    sourceCompatibility = VERSION_17.toString()
    targetCompatibility = VERSION_17.toString()
}