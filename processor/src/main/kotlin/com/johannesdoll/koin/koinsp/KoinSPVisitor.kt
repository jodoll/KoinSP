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

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.johannesdoll.koin.koinsp.api.ProvideModule
import org.koin.core.module.Module

class KoinSPVisitor(
    private val resolver: Resolver,
    private val moduleNames: MutableList<String>
) : KSVisitorVoid() {
    companion object {
        fun KSDeclaration.wrongReturnType(): () -> String = {
            "Functions with ${ProvideModule::class.simpleName} must return ${Module::class.simpleName}: $sourceLocation"
        }
    }

    private val moduleType by lazy {
        resolver
            .getClassDeclarationByName(Module::class.qualifiedName!!)!!
            .asType(emptyList())
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        val returnType = requireNotNull(property.type, property.wrongReturnType())
        require(returnType.resolve().isAssignableFrom(moduleType), property.wrongReturnType())
        require(property.isStatic()) { "Functions with ${ProvideModule::class.simpleName} must be top level declarations or declared on objects: ${property.sourceLocation}" }
        require(property.isPublic()) { "Functions with ${ProvideModule::class.simpleName} must be public: ${property.sourceLocation}" }

        val qualifiedName =
            requireNotNull(property.qualifiedName?.asString()) { "Can't determine qualified name for ${property.sourceLocation}" }
        moduleNames.add(qualifiedName)
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        val returnType = requireNotNull(function.returnType, function.wrongReturnType())
        require(returnType.resolve().isAssignableFrom(moduleType), function.wrongReturnType())
        require(function.isCallableWithoutParameters()) { "Functions with ${ProvideModule::class.simpleName} must be callable without parameters: $function.sourceLocation" }
        require(function.isStatic()) { "Functions with ${ProvideModule::class.simpleName} must be top level declarations or declared on objects: $function.sourceLocation" }
        require(function.isPublic()) { "Functions with ${ProvideModule::class.simpleName} must be public: ${function.sourceLocation}" }

        val qualifiedName =
            requireNotNull(function.qualifiedName?.asString()) { "Can't determine qualified name for ${function.sourceLocation}" }
        moduleNames.add("$qualifiedName()")
    }

    private fun KSFunctionDeclaration.isCallableWithoutParameters(): Boolean =
        parameters.isEmpty() || parameters.all { it.hasDefault }

    private fun KSDeclaration.isStatic(): Boolean =
        parentDeclaration == null || closestClassDeclaration()?.classKind == ClassKind.OBJECT
}

val KSDeclaration.sourceLocation
    get() = "${qualifiedName?.asString()}(${containingFile?.fileName}:${(location as? FileLocation)?.lineNumber})"
