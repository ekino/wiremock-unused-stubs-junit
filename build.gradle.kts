import net.researchgate.release.ReleasePlugin

plugins {
    base
    kotlin("jvm") version "1.6.0" apply false
    id("com.ekino.oss.plugin.kotlin-quality") version "3.2.0" apply false
    id("net.researchgate.release") version "3.0.1"
    id("org.jetbrains.dokka") version "1.5.31"
}

allprojects {
    group = "com.ekino.oss.wiremock"

    repositories {
        mavenCentral()
    }

    project.extra.set("junit5.version", "5.7.2")
    project.extra.set("wiremock.version", "2.27.2")
    project.extra.set("assertk.version", "0.24")
}

tasks.create("printVersion") {
    doLast {
        val version: String by project
        println(version)
    }
}

subprojects {

    apply<MavenPublishPlugin>()
    apply<ReleasePlugin>()

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                pom {
                    name.set("Wiremock unused stubs junit extension")
                    description.set("Junit extension for tracking unused wiremock stubs.")
                    url.set("https://github.com/ekino/wiremock-unused-stubs-junit")
                    licenses {
                        license {
                            name.set("MIT License (MIT)")
                            url.set("https://opensource.org/licenses/mit-license")
                        }
                    }
                    developers {
                        developer {
                            name.set("Nicolas Gunther")
                            email.set("nicolas.gunther@ekino.com")
                            organization.set("ekino")
                            organizationUrl.set("https://www.ekino.com/")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/ekino/jcv-db.git")
                        developerConnection.set("scm:git:ssh://github.com:ekino/wiremock-unused-stubs-junit.git")
                        url.set("https://github.com/ekino/jcv-db")
                    }
                    organization {
                        name.set("ekino")
                        url.set("https://www.ekino.com/")
                    }
                }
                repositories {
                    maven {
                        val ossrhUrl: String? by project
                        val ossrhUsername: String? by project
                        val ossrhPassword: String? by project

                        url = uri(ossrhUrl ?: "")

                        credentials {
                            username = ossrhUsername
                            password = ossrhPassword
                        }
                    }
                }
            }
        }
    }
}
