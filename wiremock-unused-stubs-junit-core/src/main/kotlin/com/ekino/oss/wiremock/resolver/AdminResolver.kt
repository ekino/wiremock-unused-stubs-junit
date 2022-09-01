package com.ekino.oss.wiremock.resolver

import com.github.tomakehurst.wiremock.core.Admin

class AdminResolver(private val admin: Admin) : WiremockResolver() {

    override fun getAllStubMappings() = admin.listAllStubMappings()

    override fun getServeEvents() = admin.serveEvents.requests
}
