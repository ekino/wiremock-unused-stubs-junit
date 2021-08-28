package com.ekino.oss.wiremock

import com.ekino.oss.wiremock.WireMockExtension.displayMessage
import com.ekino.oss.wiremock.WireMockExtension.getUnusedStubs
import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class WireMockStubExtension(
    private val servers: List<WireMockServer>,
    private val silent: Boolean = false
) : AfterEachCallback {

    override fun afterEach(context: ExtensionContext?) {
        servers.forEach {
            it.getUnusedStubs().displayMessage(silent)
        }
    }
}
