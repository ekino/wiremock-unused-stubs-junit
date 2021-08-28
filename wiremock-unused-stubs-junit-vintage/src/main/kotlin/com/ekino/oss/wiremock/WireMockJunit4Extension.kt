package com.ekino.oss.wiremock

import com.ekino.oss.wiremock.WireMockExtension.displayMessage
import com.ekino.oss.wiremock.WireMockExtension.getUnusedStubs
import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.rules.ExternalResource

class WireMockJunit4Extension(
    private val servers: List<WireMockServer>,
    private val silent: Boolean = false
) : ExternalResource() {

    override fun after() {
        servers.forEach {
            it.getUnusedStubs().displayMessage(silent)
        }
    }
}
