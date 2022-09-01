package com.ekino.oss.wiremock

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.each
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.ekino.oss.wiremock.sample.SampleTestClassSilentOff
import com.ekino.oss.wiremock.sample.SampleTestClassSilentOn
import org.assertj.core.api.Condition
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.discovery.MethodSelector
import org.junit.platform.testkit.engine.EngineTestKit
import org.junit.platform.testkit.engine.EventType
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory
import java.util.Optional

class WireMockStubExtensionTest {

    @TestFactory
    fun `Should passed all tests when all stubs are used`() = listOf(
        createMethodSelector(SampleTestClassSilentOn::class.java, "someTestWithNoStubsDefined"),
        createMethodSelector(SampleTestClassSilentOn::class.java, "someTestWithOneStubDefinedButUsed"),
        createMethodSelector(SampleTestClassSilentOff::class.java, "someTestWithNoStubsDefined"),
        createMethodSelector(SampleTestClassSilentOff::class.java, "someTestWithOneStubDefinedButUsed")
    ).map {
        DynamicTest.dynamicTest("should passed successfully test for ${it.methodName}") {
            val results = EngineTestKit
                .engine("junit-vintage")
                .selectors(DiscoverySelectors.selectMethod(it.className, it.methodName))
                .execute()

            results.allEvents().assertThatEvents()
                .allMatch {
                    it.type == EventType.FINISHED || it.getPayload(TestExecutionResult::class.java)
                        .toNullable()?.status != TestExecutionResult.Status.FAILED
                }
        }
    }

    @TestFactory
    fun `Should get error when unused are found with silent mode off`() = listOf(
        createMethodSelector(SampleTestClassSilentOff::class.java, "someTestWithOneStubDefinedButNotUsed"),
        createMethodSelector(SampleTestClassSilentOff::class.java, "someTestWithMultipleStubsButNotAllUsed")
    ).map {
        DynamicTest.dynamicTest("should get failed test for ${it.methodName}") {
            val results = EngineTestKit
                .engine("junit-vintage")
                .selectors(DiscoverySelectors.selectMethod(it.className, it.methodName))
                .execute()

            results.allEvents().assertThatEvents()
                .areExactly(
                    1,
                    Condition(
                        {
                            it.getPayload(TestExecutionResult::class.java)
                                .toNullable()?.status == TestExecutionResult.Status.FAILED
                        },
                        "isTestInFailedState"
                    )
                )
        }
    }

    @TestFactory
    fun `Should get waning when unused are found with silent mode on`() = listOf(
        createMethodSelector(SampleTestClassSilentOn::class.java, "someTestWithOneStubDefinedButNotUsed"),
        createMethodSelector(SampleTestClassSilentOn::class.java, "someTestWithMultipleStubsButNotAllUsed")
    ).map {
        DynamicTest.dynamicTest("should passed test with warning for ${it.methodName}") {
            val results = EngineTestKit
                .engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(it.className, it.methodName))
                .execute()

            val logger: TestLogger = TestLoggerFactory.getTestLogger(WireMockExtension::class.java)

            assertThat(logger.allLoggingEvents)
                .each { assert ->
                    run {
                        assert.prop("level") { it.level }.isEqualTo(Level.WARN)
                        assert.prop("message") { it.message }.contains("Unnecessary wiremock stub has been found :")
                    }
                }

            results.allEvents().assertThatEvents()
                .allMatch {
                    it.type == EventType.FINISHED || it.getPayload(TestExecutionResult::class.java)
                        .toNullable()?.status != TestExecutionResult.Status.FAILED
                }
        }
    }

    private fun createMethodSelector(
        sampleClass: Class<*>,
        methodName: String,
        parameters: String? = null
    ): MethodSelector {
        return parameters
            ?.let { DiscoverySelectors.selectMethod(sampleClass, methodName, parameters) }
            ?: DiscoverySelectors.selectMethod(sampleClass, methodName)
    }

    private fun <T : Any> Optional<T>.toNullable(): T? {
        return if (this.isPresent) {
            this.get()
        } else {
            null
        }
    }
}
