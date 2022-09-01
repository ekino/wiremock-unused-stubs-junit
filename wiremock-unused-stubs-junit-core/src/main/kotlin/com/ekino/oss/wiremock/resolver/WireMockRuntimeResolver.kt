package com.ekino.oss.wiremock.resolver

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo

class WireMockRuntimeResolver(private val wireMockRuntimeInfo: WireMockRuntimeInfo) : WiremockResolver() {

    override fun getAllStubMappings() = wireMockRuntimeInfo.wireMock.allStubMappings()

    override fun getServeEvents() = wireMockRuntimeInfo.wireMock.serveEvents
}
