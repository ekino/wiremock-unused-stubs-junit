# Wiremock unused stubs junit extension

This extension will help you to track unnecessary wiremock stubs defined in your tests.
It supports junit4 and junit5 engine.

## Dependencies

### Maven

Junit 5 :
```xml
<dependency>
    <groupId>com.ekino.oss</groupId>
    <artifactId>wiremock-unused-stubs-junit-core</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.ekino.oss</groupId>
    <artifactId>wiremock-unused-stubs-junit-jupiter</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

Junit 4 :
```xml
<dependency>
    <groupId>com.ekino.oss</groupId>
    <artifactId>wiremock-unused-stubs-junit-core</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.ekino.oss</groupId>
    <artifactId>wiremock-unused-stubs-junit-vintage</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

### Gradle

Junit 5 :
```kotlin
testImplementation("com.ekino.oss:wiremock-unused-stubs-junit-core:1.0,0")
testImplementation("com.ekino.oss:wiremock-unused-stubs-junit-jupiter:1.0,0")
```

Junit 4 :
```kotlin
testImplementation("com.ekino.oss:wiremock-unused-stubs-junit-core:1.0,0")
testImplementation("com.ekino.oss:wiremock-unused-stubs-junit-vintage:1.0,0")
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
