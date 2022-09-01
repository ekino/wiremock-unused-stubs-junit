# Wiremock unused stubs junit extension

This extension will help you to track unnecessary wiremock stubs defined in your tests.
It supports junit4 and junit5 engine.

[![Build Status](https://github.com/ekino/wiremock-unused-stubs-junit/workflows/Build%20branch/badge.svg?branch=master)](https://github.com/ekino/wiremock-unused-stubs-junit/actions?query=workflow%3A%22Build+branch%22+branch%3Amaster)
[![GitHub (pre-)release](https://img.shields.io/github/release/ekino/wiremock-unused-stubs-junit/all.svg)](https://github.com/ekino/wiremock-unused-stubs-junit/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.ekino.oss.wiremock/wiremock-unused-stubs-junit-core)](https://search.maven.org/search?q=g:com.ekino.oss.wiremock)
## Dependencies

### Maven

Junit 5 :
```xml
<dependency>
    <groupId>com.ekino.oss.wiremock</groupId>
    <artifactId>wiremock-unused-stubs-junit-jupiter</artifactId>
    <version>1.1.1</version>
    <scope>test</scope>
</dependency>
```

Junit 4 :
```xml
<dependency>
    <groupId>com.ekino.oss.wiremock</groupId>
    <artifactId>wiremock-unused-stubs-junit-vintage</artifactId>
    <version>1.1.1</version>
    <scope>test</scope>
</dependency>
```

### Gradle

Junit 5 :
```kotlin
testImplementation("com.ekino.oss.wiremock:wiremock-unused-stubs-junit-jupiter:1.1.1")
```

Junit 4 :
```kotlin
testImplementation("com.ekino.oss.wiremock:wiremock-unused-stubs-junit-vintage:1.1.1")
```

## How to use it ?

For use this library you will need to register your extension using junit.
You will need to give two information to the extensions :
* A list of the wiremock server you want to monitor
* If you want or not activate the silent mode. It will display a warning message when unnecessary stubs are found.

### Junit 5

``` java
import com.ekino.oss.wiremock.WireMockStubExtension
import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.extension.RegisterExtension

public class SampleClassTest {
    private WireMockServer wireMockServer = WireMockServer(1234);
    
    @RegisterExtension
    private static WireMockStubExtension wireMockStubExtension = WireMockStubExtension(listOf(wireMockServer), silent = true);
}
```

### Junit 4

``` java
import com.ekino.oss.wiremock.WireMockJunit4Extension
import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.ClassRule

public class SampleClassTest {
    private WireMockServer wireMockServer = WireMockServer(1234);
    
    @ClassRule
    private static WireMockJunit4Extension wireMockStubExtension = WireMockJunit4Extension(listOf(wireMockServer), silent = true);
}
```

### Junit 5 Wiremock Extension

This extension now the support, the junit 5 extension provided by wiremock.

You can use the extension with the static DSL :

```java
@WireMockTest(httpPort = 1234)
@ExtendWith(WireMockStubExtension::class)
public class ClassTest {

    // failed test
    @Test
    void test1() {
        stubFor(
                get(WireMock.urlPathEqualTo("/some-url"))
                        .willReturn(WireMock.ok())
        );
    }

    // Success test
    @Test
    void test2() {
        stubFor(
                get(WireMock.urlPathEqualTo("/some-url"))
                        .willReturn(WireMock.ok())
        );

        HttpUriRequest request = new HttpGet("http://localhost:1234/some-url");
        HttpClientBuilder.create().build().execute(request);
    }
}
```

You can use the extension by injecting `WireMockRuntimeInfo` in test parameter :

```java
@WireMockTest(httpPort = 1234)
@ExtendWith(WireMockStubExtension::class)
class ClassTest {

    // failed test
    @Test
    void test1(WireMockRuntimeInfo wmRuntimeInfo) {
        WireMock wireMock = wmRuntimeInfo.getWireMock();

        wireMock.register(
            get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        );
    }

    // success test
    @Test
    void test2(WireMockRuntimeInfo wmRuntimeInfo) {
        WireMock wireMock = wmRuntimeInfo.getWireMock();

        wireMock.register(
            get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        );

        HttpUriRequest request = new HttpGet("http://localhost:1234/some-url");
        HttpClientBuilder.create().build().execute(request);
    }
}
```

You can still use the programmatic way to define junit 5 wiremock extension like this :

```java
class ClassTest {
    
    @RegisterExtension 
    private static WireMockExtension wiremockExtension = WireMockExtension.newInstance()
        .options(wireMockConfig().port(1234))
        .build();

    @RegisterExtension 
    private static  WireMockStubExtension wireMockStubExtension = WireMockStubExtension(wireMockRuntimeInfos = listOf(wiremockExtension));

    // failed test
    @Test
    void test1() {
        wiremockExtension.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        );
    }

    // success test
    @Test
    void test2() {
        wiremockExtension.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        );

        HttpUriRequest request = new HttpGet("http://localhost:1234/some-url");
        HttpClientBuilder.create().build().execute(request);
    }
}
```
