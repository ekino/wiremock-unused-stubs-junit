package com.ekino.oss.wiremock

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.each
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.ekino.oss.wiremock.sample.SampleTestClassSilentOff
import com.ekino.oss.wiremock.sample.SampleTestClassSilentOn
import org.assertj.core.api.Condition
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.testkit.engine.EngineTestKit
import org.junit.platform.testkit.engine.EventType
import uk.org.lidalia.slf4jext.Level
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory
import java.util.Optional

class WireMockStubExtensionTest {

    companion object {
        @JvmStatic
        fun successfulUseCases() = listOf(
            Arguments.of(SampleTestClassSilentOn::class.java, "someTestWithNoStubsDefined"),
            Arguments.of(SampleTestClassSilentOn::class.java, "someTestWithOneStubDefinedButUsed"),
            Arguments.of(SampleTestClassSilentOff::class.java, "someTestWithNoStubsDefined"),
            Arguments.of(SampleTestClassSilentOff::class.java, "someTestWithOneStubDefinedButUsed")
        )

        @JvmStatic
        fun errorUseCases() = listOf(
            Arguments.of(SampleTestClassSilentOff::class.java, "someTestWithOneStubDefinedButNotUsed"),
            Arguments.of(SampleTestClassSilentOff::class.java, "someTestWithMultipleStubsButNotAllUsed")
        )

        @JvmStatic
        fun warningUseCases() = listOf(
            Arguments.of(SampleTestClassSilentOn::class.java, "someTestWithOneStubDefinedButNotUsed"),
            Arguments.of(SampleTestClassSilentOn::class.java, "someTestWithMultipleStubsButNotAllUsed")
        )
    }

    @ParameterizedTest
    @MethodSource("successfulUseCases")
    fun `Should not get any error when no stub is defined`(
        sampleClass: Class<Any>,
        methodName: String
    ) {
        val results = EngineTestKit
            .engine("junit-vintage")
            .selectors(DiscoverySelectors.selectMethod(sampleClass, methodName))
            .execute()

        results.allEvents().assertThatEvents()
            .allMatch {
                it.type == EventType.FINISHED || it.getPayload(TestExecutionResult::class.java)
                    .toNullable()?.status != TestExecutionResult.Status.FAILED
            }
    }

    @ParameterizedTest
    @MethodSource("errorUseCases")
    fun `Should get error when unused are found with silent mode off`(
        sampleClass: Class<Any>,
        methodName: String
    ) {
        val results = EngineTestKit
            .engine("junit-vintage")
            .selectors(DiscoverySelectors.selectMethod(sampleClass, methodName))
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

    @ParameterizedTest
    @MethodSource("warningUseCases")
    fun `Should get waning when unused are found with silent mode on`(
        sampleClass: Class<Any>,
        methodName: String
    ) {
        val results = EngineTestKit
            .engine("junit-jupiter")
            .selectors(DiscoverySelectors.selectMethod(sampleClass, methodName))
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

    private fun <T : Any> Optional<T>.toNullable(): T? {
        return if (this.isPresent) {
            this.get()
        } else {
            null
        }
    }
}
