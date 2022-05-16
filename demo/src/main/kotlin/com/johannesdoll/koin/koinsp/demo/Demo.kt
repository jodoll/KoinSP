package com.johannesdoll.koin.koinsp.demo

import com.johannesdoll.koin.koinsp.koinSPModules
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(koinSPModules())
    }
}