package com.ekino.oss.wiremock.resolver

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpUriRequest
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.junit.jupiter.api.Test

@WireMockTest(httpPort = 1234)
class DefaultResolverTest {

    @Test
    fun `Should not find any stubs when none is defined`() {
        assertThat(DefaultResolver().getUnusedStubs()).isEmpty()
    }

    @Test
    fun `Should find one unused stub`() {
        stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        assertThat(DefaultResolver().getUnusedStubs())
            .hasSize(1)
    }

    @Test
    fun `Should not find any stubs when all stubs are used`() {
        stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)

        assertThat(DefaultResolver().getUnusedStubs()).isEmpty()
    }

    @Test
    fun `Should find that one stub is unused among multiple stub`() {
        stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/other-url"))
                .willReturn(WireMock.ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)

        assertThat(DefaultResolver().getUnusedStubs())
            .hasSize(1)

        assertThat(DefaultResolver().getUnusedStubs()[0].request.urlPath)
            .isEqualTo("/other-url")
    }
}
