package com.ekino.oss.wiremock

import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.ekino.oss.wiremock.WireMockExtension.displayMessage
import com.ekino.oss.wiremock.resolver.AdminResolver
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpUriRequest
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

class WireMockExtensionTest {

    private val wireMockServer = WireMockServer(1234)

    @BeforeEach
    fun setUp() = wireMockServer.start()

    @AfterEach
    fun tearDown() = wireMockServer.stop()

    @Test
    fun `Should not get any error message all stubs are used`() {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/some-url"))
                .willReturn(ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)

        assertDoesNotThrow { AdminResolver(wireMockServer).getUnusedStubs().displayMessage() }
    }

    @Test
    fun `Should get an exception when silent mode is off`() {
        val expectedMessage = """
            Unnecessary wiremock stub has been found :
                url request : /some-url,
                method request : GET,
                body request : null,
                header request : [],
                body response : null,
                header response : null
        """.trimIndent()

        wireMockServer.stubFor(
            get(urlPathEqualTo("/some-url"))
                .willReturn(ok())
        )

        assertThat {
            AdminResolver(wireMockServer).getUnusedStubs().displayMessage()
        }.isFailure().isInstanceOf(AssertionError::class).hasMessage(expectedMessage)
    }

    @Test
    fun `Should get a warning message when silent mode is on`() {
        val logger: TestLogger = TestLoggerFactory.getTestLogger(WireMockExtension::class.java)

        val expectedMessage = """
            Unnecessary wiremock stub has been found :
                url request : /some-url,
                method request : GET,
                body request : null,
                header request : [],
                body response : null,
                header response : null
        """.trimIndent()

        wireMockServer.stubFor(
            get(urlPathEqualTo("/some-url"))
                .willReturn(ok())
        )

        assertDoesNotThrow { AdminResolver(wireMockServer).getUnusedStubs().displayMessage(true) }
        assertThat(logger.allLoggingEvents)
            .each { assert ->
                run {
                    assert.prop("level") { it.level }.isEqualTo(Level.WARN)
                    assert.prop("message") { it.message }.isEqualTo(expectedMessage)
                }
            }
    }
}
