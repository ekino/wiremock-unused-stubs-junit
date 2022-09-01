package com.ekino.oss.wiremock.resolver

import com.github.tomakehurst.wiremock.client.WireMock.getAllServeEvents
import com.github.tomakehurst.wiremock.client.WireMock.listAllStubMappings

class DefaultResolver : WiremockResolver() {

    override fun getAllStubMappings() = listAllStubMappings()

    override fun getServeEvents() = getAllServeEvents()
}
