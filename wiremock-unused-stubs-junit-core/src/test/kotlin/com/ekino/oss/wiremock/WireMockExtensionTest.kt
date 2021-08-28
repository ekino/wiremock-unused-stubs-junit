package com.ekino.oss.wiremock

import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.hasMessage
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.ekino.oss.wiremock.WireMockExtension.displayMessage
import com.ekino.oss.wiremock.WireMockExtension.getUnusedStubs
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.HttpClientBuilder
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
    fun `Should not find any stubs when none is defined`() {
        assertThat(wireMockServer.getUnusedStubs()).isEmpty()
    }

    @Test
    fun `Should find one unused stub`() {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/some-url"))
                .willReturn(ok())
        )

        assertThat(wireMockServer.getUnusedStubs())
            .hasSize(1)
    }

    @Test
    fun `Should not find any stubs when all stubs are used`() {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/some-url"))
                .willReturn(ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)

        assertThat(wireMockServer.getUnusedStubs()).isEmpty()
    }

    @Test
    fun `Should find that one stub is unused among multiple stub`() {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/some-url"))
                .willReturn(ok())
        )

        wireMockServer.stubFor(
            get(urlPathEqualTo("/other-url"))
                .willReturn(ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)

        assertThat(wireMockServer.getUnusedStubs())
            .hasSize(1)

        assertThat(wireMockServer.getUnusedStubs()[0].request.urlPath)
            .isEqualTo("/other-url")
    }

    @Test
    fun `Should not get any error message all stubs are used`() {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/some-url"))
                .willReturn(ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)

        assertDoesNotThrow { wireMockServer.getUnusedStubs().displayMessage() }
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
            wireMockServer.getUnusedStubs().displayMessage()
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

        assertDoesNotThrow { wireMockServer.getUnusedStubs().displayMessage(true) }
        assertThat(logger.allLoggingEvents)
            .each { assert ->
                run {
                    assert.prop("level") { it.level }.isEqualTo(Level.WARN)
                    assert.prop("message") { it.message }.isEqualTo(expectedMessage)
                }
            }
    }
}
