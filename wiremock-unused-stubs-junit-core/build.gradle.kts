import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-library`
    signing
    id("com.ekino.oss.plugin.kotlin-quality")
    id("org.jetbrains.dokka")
}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

java {
    withSourcesJar()
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn("dokkaHtml")
    archiveClassifier.set("javadoc")
    from(buildDir.resolve("dokka"))
}

tasks {
    dokkaHtml {
        dokkaSourceSets {
            configureEach {
                jdkVersion.set(11)
                reportUndocumented.set(false)
            }
        }
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs("-Duser.language=en")
    }

    artifacts {
        archives(jar)
        archives(javadocJar)
    }
}

val publicationName = "mavenJava"

publishing {
    publications {
        named<MavenPublication>(publicationName) {
            artifact(javadocJar.get())
            from(components["java"])
        }
    }
}

signing {
    sign(publishing.publications[publicationName])
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.github.tomakehurst:wiremock-jre8:${project.extra["wiremock.version"]}")
    implementation("io.github.microutils:kotlin-logging:2.1.23")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:${project.extra["junit5.version"]}")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:${project.extra["assertk.version"]}")
    testImplementation("uk.org.lidalia:slf4j-test:1.2.0")
}
