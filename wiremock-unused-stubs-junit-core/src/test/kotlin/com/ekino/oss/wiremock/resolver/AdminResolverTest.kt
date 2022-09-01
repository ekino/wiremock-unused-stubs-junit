package com.ekino.oss.wiremock.resolver

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpUriRequest
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AdminResolverTest {

    private val wireMockServer = WireMockServer(1234)

    @BeforeEach
    fun setUp() = wireMockServer.start()

    @AfterEach
    fun tearDown() = wireMockServer.stop()

    @Test
    fun `Should not find any stubs when none is defined`() {
        assertThat(AdminResolver(wireMockServer).getUnusedStubs()).isEmpty()
    }

    @Test
    fun `Should find one unused stub`() {
        wireMockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        assertThat(AdminResolver(wireMockServer).getUnusedStubs())
            .hasSize(1)
    }

    @Test
    fun `Should not find any stubs when all stubs are used`() {
        wireMockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)

        assertThat(AdminResolver(wireMockServer).getUnusedStubs()).isEmpty()
    }

    @Test
    fun `Should find that one stub is unused among multiple stub`() {
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

        assertThat(AdminResolver(wireMockServer).getUnusedStubs())
            .hasSize(1)

        assertThat(AdminResolver(wireMockServer).getUnusedStubs()[0].request.urlPath)
            .isEqualTo("/other-url")
    }
}
