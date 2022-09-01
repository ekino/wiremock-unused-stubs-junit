package com.ekino.oss.wiremock

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.each
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.ekino.oss.wiremock.sample.SampleTestClassSilentOff
import com.ekino.oss.wiremock.sample.SampleTestClassSilentOn
import com.ekino.oss.wiremock.sample.SampleTestClassWithExtension
import com.ekino.oss.wiremock.sample.SampleTestClassWithProgrammaticExtension
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
        createMethodSelector(SampleTestClassSilentOff::class.java, "someTestWithOneStubDefinedButUsed"),
        createMethodSelector(
            SampleTestClassWithExtension::class.java,
            "someTestWithNoStubsDefinedWithParameter",
            "com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo"
        ),
        createMethodSelector(
            SampleTestClassWithExtension::class.java,
            "someTestWithOneStubDefinedButUsedWithParameter",
            "com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo"
        ),
        createMethodSelector(SampleTestClassWithExtension::class.java, "someTestWithNoStubsDefined"),
        createMethodSelector(SampleTestClassWithExtension::class.java, "someTestWithOneStubDefinedButUsed"),
        createMethodSelector(SampleTestClassWithProgrammaticExtension::class.java, "someTestWithNoStubsDefined"),
        createMethodSelector(SampleTestClassWithProgrammaticExtension::class.java, "someTestWithOneStubDefinedButUsed")
    ).map {
        DynamicTest.dynamicTest("should passed successfully test for ${it.methodName}") {
            val results = EngineTestKit
                .engine("junit-jupiter")
                .selectors(it)
                .execute()

            results.allEvents().assertThatEvents()
                .allMatch { event ->
                    event.type == EventType.FINISHED || event.getPayload(TestExecutionResult::class.java)
                        .toNullable()?.status != TestExecutionResult.Status.FAILED
                }
        }
    }

    @TestFactory
    fun `Should get error when unused are found with silent mode off`() = listOf(
        createMethodSelector(SampleTestClassSilentOff::class.java, "someTestWithOneStubDefinedButNotUsed"),
        createMethodSelector(SampleTestClassSilentOff::class.java, "someTestWithMultipleStubsButNotAllUsed"),
        createMethodSelector(
            SampleTestClassWithExtension::class.java,
            "someTestWithOneStubDefinedButNotUsedWithParameter",
            "com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo"
        ),
        createMethodSelector(
            SampleTestClassWithExtension::class.java,
            "someTestWithMultipleStubsButNotAllUsedWithParameter",
            "com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo"
        ),
        createMethodSelector(SampleTestClassWithExtension::class.java, "someTestWithOneStubDefinedButNotUsed"),
        createMethodSelector(SampleTestClassWithExtension::class.java, "someTestWithMultipleStubsButNotAllUsed"),
        createMethodSelector(
            SampleTestClassWithProgrammaticExtension::class.java,
            "someTestWithOneStubDefinedButNotUsed"
        ),
        createMethodSelector(
            SampleTestClassWithProgrammaticExtension::class.java,
            "someTestWithMultipleStubsButNotAllUsed"
        )
    ).map {
        DynamicTest.dynamicTest("should get failed test for ${it.methodName}") {
            val results = EngineTestKit
                .engine("junit-jupiter")
                .selectors(it)
                .execute()

            results.allEvents()
                .assertThatEvents()
                .areExactly(
                    1,
                    Condition(
                        { event -> event.getPayload(TestExecutionResult::class.java).toNullable()?.status == TestExecutionResult.Status.FAILED },
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
                .selectors(it)
                .execute()

            val logger: TestLogger = TestLoggerFactory.getTestLogger(WireMockExtension::class.java)

            assertThat(logger.allLoggingEvents)
                .each { assert ->
                    run {
                        assert.prop("level") { extension -> extension.level }.isEqualTo(Level.WARN)
                        assert.prop("message") { extension -> extension.message }
                            .contains("Unnecessary wiremock stub has been found :")
                    }
                }

            results.allEvents().assertThatEvents()
                .allMatch { event ->
                    event.type == EventType.FINISHED || event.getPayload(TestExecutionResult::class.java)
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
