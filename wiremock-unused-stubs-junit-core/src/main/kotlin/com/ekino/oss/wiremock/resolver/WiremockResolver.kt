package com.ekino.oss.wiremock.resolver

import com.github.tomakehurst.wiremock.admin.model.ListStubMappingsResult
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import com.github.tomakehurst.wiremock.stubbing.StubMapping

abstract class WiremockResolver {

    abstract fun getAllStubMappings(): ListStubMappingsResult

    abstract fun getServeEvents(): List<ServeEvent>

    fun getUnusedStubs(): List<StubMapping> {
        val usedStubIds = this.getUsedStubId()

        return this.getAllStubMappings()
            .mappings
            .filter { !usedStubIds.contains(it.id) }
            .toList()
    }

    private fun getUsedStubId() = getServeEvents()
        .mapNotNull { it.stubMapping?.id }
        .toSet()
}
