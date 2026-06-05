plugins {
    id("java")
    id("checkstyle")
    id("com.github.spotbugs") version "6.0.9"
    id("jacoco")
    id("info.solidsoft.pitest") version "1.15.0"
}

// targetClasses only includes fully implemented classes.
// Add new classes here as they are implemented and tested.
pitest {
    junit5PluginVersion = "1.2.1"
    targetClasses = setOf("domain.*")
    targetTests = setOf("domain.*")
    outputFormats = setOf("HTML")
    mutationThreshold = 100
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
}

checkstyle {
    toolVersion = "10.12.4"
    configFile = file("config/checkstyle/checkstyle.xml")
}

spotbugs {
    toolVersion = "4.8.3"
    effort = com.github.spotbugs.snom.Effort.MAX
    reportLevel = com.github.spotbugs.snom.Confidence.LOW
    ignoreFailures = true
}

tasks.spotbugsMain {
    reports.create("html") {
        required = true
    }
}

group = "nu.csse.sqe"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.easymock:easymock:5.2.0")
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.8.3")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

tasks.compileJava {
    options.release = 11
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}