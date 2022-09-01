package com.ekino.oss.wiremock.sample

import com.ekino.oss.wiremock.WireMockStubExtension
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpUriRequest
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@WireMockTest(httpPort = 1234)
@ExtendWith(WireMockStubExtension::class)
class SampleTestClassWithExtension {

    @Test
    fun someTestWithNoStubsDefined() {
        // ktlint-disable no-empty-function
    }

    @Test
    fun someTestWithOneStubDefinedButNotUsed() {
        stubFor(
            get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )
    }

    @Test
    fun someTestWithOneStubDefinedButUsed() {
        stubFor(
            get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)
    }

    @Test
    fun someTestWithMultipleStubsButNotAllUsed() {
        stubFor(
            get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        stubFor(
            get(WireMock.urlPathEqualTo("/other-url"))
                .willReturn(WireMock.ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)
    }

    @Test
    @Suppress("UnusedPrivateMember")
    fun someTestWithNoStubsDefinedWithParameter(wmRuntimeInfo: WireMockRuntimeInfo) {
        // ktlint-disable no-empty-function
    }

    @Test
    fun someTestWithOneStubDefinedButNotUsedWithParameter(wmRuntimeInfo: WireMockRuntimeInfo) {
        val wireMock = wmRuntimeInfo.wireMock

        wireMock.register(
            get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )
    }

    @Test
    fun someTestWithOneStubDefinedButUsedWithParameter(wmRuntimeInfo: WireMockRuntimeInfo) {
        val wireMock = wmRuntimeInfo.wireMock

        wireMock.register(
            get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)
    }

    @Test
    fun someTestWithMultipleStubsButNotAllUsedWithParameter(wmRuntimeInfo: WireMockRuntimeInfo) {
        val wireMock = wmRuntimeInfo.wireMock

        wireMock.register(
            get(WireMock.urlPathEqualTo("/some-url"))
                .willReturn(WireMock.ok())
        )

        wireMock.register(
            get(WireMock.urlPathEqualTo("/other-url"))
                .willReturn(WireMock.ok())
        )

        val request: HttpUriRequest = HttpGet("http://localhost:1234/some-url")
        HttpClientBuilder.create().build().execute(request)
    }
}
