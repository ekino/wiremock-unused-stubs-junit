package com.ekino.oss.wiremock

import com.github.tomakehurst.wiremock.matching.MultiValuePattern
import com.github.tomakehurst.wiremock.matching.RequestPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object WireMockExtension {

    fun List<StubMapping>.displayMessage(silent: Boolean = false) {
        if (this.isNotEmpty()) {
            val message = this.joinToString("\n") { it.displayInfo() }
            if (silent) displayWarningMessage(message) else displayErrorMessage(message)
        }
    }

    private fun StubMapping.displayInfo() = """
        Unnecessary wiremock stub has been found :
            url request : ${this.request.getDefinedUrl()},
            method request : ${this.request.method},
            body request : ${this.request.bodyPatterns},
            header request : ${this.request.headers?.let { prettyHeader(it) } ?: emptyList()},
            body response : ${this.response.body},
            header response : ${this.response.body}
    """.trimIndent()

    private fun RequestPattern.getDefinedUrl() = listOfNotNull(
        this.url,
        this.urlPathPattern,
        this.urlPattern,
        this.urlPath,
        this.urlMatcher.pattern.toString()
    ).first()

    private fun prettyHeader(headers: Map<String, MultiValuePattern>) =
        headers.entries.map { "${it.key} ${it.value.valuePattern}" }

    private fun displayErrorMessage(message: String): Unit = throw AssertionError(message)
    private fun displayWarningMessage(message: String): Unit = logger.warn(message)
}
