/*
 * Copyright 2022 Johannes Doll
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.johannesdoll.koin.koinsp

import com.johannesdoll.koin.koinsp.api.ProvideModule
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import org.koin.core.module.Module
import org.koin.dsl.module

object TestModules {

    val functionModule = module { single { "Function" } }

    @ProvideModule
    fun barModule(): Module = functionModule

    @ProvideModule
    val propertyModule: Module = module { single { "Property" } }
}

class GeneratedCodeTest : BehaviorSpec({

    Given("A module providing function") {
        When("Annotated with @KoinSPModule") {
            Then("The koinSPModules function is generated") {
                koinSPModules()
            }
            Then("The koinSPModules function contains the module") {
                koinSPModules() shouldContain TestModules.functionModule
            }
        }
    }

    Given("A module providing property") {
        When("Annotated with @KoinSPModule") {
            Then("The koinSPModules function contains the module") {
                koinSPModules() shouldContain TestModules.propertyModule
            }
        }
    }
})
