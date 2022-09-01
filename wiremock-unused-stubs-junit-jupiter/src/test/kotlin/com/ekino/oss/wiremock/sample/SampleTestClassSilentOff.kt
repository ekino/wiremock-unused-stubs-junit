package com.ekino.oss.wiremock.sample

import com.ekino.oss.wiremock.WireMockStubExtension
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpUriRequest
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SampleTestClassSilentOff {

    companion object {
        val wireMockServer = WireMockServer(1234)

        @JvmStatic
        @RegisterExtension
        val wireMockStubExtension = WireMockStubExtension(servers = listOf(wireMockServer))
    }

    @BeforeEach
    fun setUp() = wireMockServer.start()

    @AfterEach
    fun tearDown() = wireMockServer.stop()

    @Test
    fun someTestWithNoStubsDefined() {
        // ktlint-disable no-empty-function
    }

    @Test
    fun someTestWithOneStubDefinedButNotUsed() {
        wireMockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )
    }

    @Test
    fun someTestWithOneStubDefinedButUsed() {
        wireMockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)
    }

    @Test
    fun someTestWithMultipleStubsButNotAllUsed() {
        wireMockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        wireMockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/other-url"))
                .willReturn(WireMock.ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)
    }
}
