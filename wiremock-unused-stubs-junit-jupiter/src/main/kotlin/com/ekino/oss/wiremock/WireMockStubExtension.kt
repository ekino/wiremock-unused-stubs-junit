package com.ekino.oss.wiremock

import com.ekino.oss.wiremock.WireMockExtension.displayMessage
import com.ekino.oss.wiremock.resolver.AdminResolver
import com.ekino.oss.wiremock.resolver.DefaultResolver
import com.ekino.oss.wiremock.resolver.WireMockRuntimeResolver
import com.ekino.oss.wiremock.resolver.WiremockResolver
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method

class WireMockStubExtension(
    private val silent: Boolean = false,
    private val servers: List<WireMockServer> = emptyList(),
    private val wireMockRuntimeInfos: List<WireMockExtension> = emptyList(),
) : AfterEachCallback, InvocationInterceptor {

    private var wireMockRuntimeInfo: WireMockRuntimeInfo? = null

    override fun afterEach(context: ExtensionContext) {

        val resolvers = ArrayList<WiremockResolver>()

        resolvers.addAll(servers.map { AdminResolver(it) })
        resolvers.addAll(wireMockRuntimeInfos.map { WireMockRuntimeResolver(it.runtimeInfo) })
        resolvers.add(DefaultResolver())
        wireMockRuntimeInfo
            ?.let { resolvers.add(WireMockRuntimeResolver(it)) }

        resolvers.forEach {
            it.getUnusedStubs().displayMessage(silent)
        }
    }

    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        if (!extensionContext.isAnnotatedWithJunitExtension()) {
            invocation.proceed()
            return
        }

        wireMockRuntimeInfo = invocationContext.arguments
            .filterIsInstance<WireMockRuntimeInfo>()
            .firstOrNull()

        invocation.proceed()
    }

    private fun ExtensionContext.isAnnotatedWithJunitExtension() =
        this.requiredTestClass.getDeclaredAnnotation(WireMockTest::class.java) != null
}
