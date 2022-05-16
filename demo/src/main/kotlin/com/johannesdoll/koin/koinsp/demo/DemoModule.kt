package com.johannesdoll.koin.koinsp.demo

import com.johannesdoll.koin.koinsp.api.KoinSPModule
import org.koin.core.module.Module
import org.koin.dsl.module

object DemoModule {

    @get:KoinSPModule
    val fooModule: Module = module { }

    @KoinSPModule
    fun barModule(): Module = module { }

    @KoinSPModule
    val bazModule: Module = module { }
}