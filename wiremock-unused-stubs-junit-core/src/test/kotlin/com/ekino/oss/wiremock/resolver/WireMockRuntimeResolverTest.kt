package com.ekino.oss.wiremock.resolver

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpUriRequest
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class WireMockRuntimeResolverTest {

    companion object {

        @JvmField
        @RegisterExtension
        val wiremockExtension = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(1234))
            .build()
    }

    @Test
    fun `Should not find any stubs when none is defined`() {
        assertThat(WireMockRuntimeResolver(wiremockExtension.runtimeInfo).getUnusedStubs()).isEmpty()
    }

    @Test
    fun `Should find one unused stub`() {
        wiremockExtension.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        assertThat(WireMockRuntimeResolver(wiremockExtension.runtimeInfo).getUnusedStubs())
            .hasSize(1)
    }

    @Test
    fun `Should not find any stubs when all stubs are used`() {
        wiremockExtension.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)

        assertThat(WireMockRuntimeResolver(wiremockExtension.runtimeInfo).getUnusedStubs()).isEmpty()
    }

    @Test
    fun `Should find that one stub is unused among multiple stub`() {
        wiremockExtension.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        wiremockExtension.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/other-url"))
                .willReturn(WireMock.ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)

        assertThat(WireMockRuntimeResolver(wiremockExtension.runtimeInfo).getUnusedStubs())
            .hasSize(1)

        assertThat(WireMockRuntimeResolver(wiremockExtension.runtimeInfo).getUnusedStubs()[0].request.urlPath)
            .isEqualTo("/other-url")
    }
}
