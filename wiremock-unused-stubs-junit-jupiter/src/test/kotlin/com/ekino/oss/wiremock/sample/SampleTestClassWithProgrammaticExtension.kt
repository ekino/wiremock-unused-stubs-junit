package com.ekino.oss.wiremock.sample

import com.ekino.oss.wiremock.WireMockStubExtension
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpUriRequest
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SampleTestClassWithProgrammaticExtension {

    companion object {

        @JvmField
        @RegisterExtension
        val wiremockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().port(1234))
            .build()

        @JvmField
        @RegisterExtension
        val wireMockStubExtension = WireMockStubExtension(wireMockRuntimeInfos = listOf(wiremockExtension))
    }

    @Test
    fun someTestWithNoStubsDefined() {
        // ktlint-disable no-empty-function
    }

    @Test
    fun someTestWithOneStubDefinedButNotUsed() {
        wiremockExtension.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )
    }

    @Test
    fun someTestWithOneStubDefinedButUsed() {
        wiremockExtension.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)
    }

    @Test
    fun someTestWithMultipleStubsButNotAllUsed() {
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
    }
}
