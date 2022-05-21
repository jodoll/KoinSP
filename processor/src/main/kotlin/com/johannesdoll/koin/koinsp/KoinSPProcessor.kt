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

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.johannesdoll.koin.koinsp.api.ProvideModule
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.typeNameOf
import org.koin.core.module.Module

@OptIn(KotlinPoetKspPreview::class)
class KoinSPProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {

    companion object {
        private val moduleAnnotation = ProvideModule::class
        private const val functionName = "koinSPModules"
        private const val fileName = "KoinSpModules"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val koinModules = resolver
            .getSymbolsWithAnnotation(moduleAnnotation.qualifiedName!!)
            .filter { it is KSFunctionDeclaration || it is KSPropertyDeclaration }

        if (koinModules.iterator().hasNext()) {
            createKoinModulesFile(koinModules, resolver)
        }

        return emptyList()
    }

    private fun createKoinModulesFile(
        koinModules: Sequence<KSAnnotated>,
        resolver: Resolver
    ) {
        val file = FileSpec.builder(KoinSPProcessor::class.java.packageName, fileName)
            .addImport(moduleAnnotation, "")
            .addFunction(koinModulesFunSpec(koinModules, resolver))
            .build()

        file.writeTo(codeGenerator, koinModules.toDependencies(aggregating = true))
    }

    private fun Sequence<KSAnnotated>.toDependencies(aggregating: Boolean) =
        mapNotNull { it.containingFile }.toList().toTypedArray().let { Dependencies(aggregating, *it) }

    private fun koinModulesFunSpec(
        koinModules: Sequence<KSNode>,
        resolver: Resolver
    ): FunSpec {
        val resolvedModules = mutableListOf<String>()
        koinModules.forEach { it.accept(KoinSPVisitor(resolver, resolvedModules), Unit) }

        return FunSpec.builder(functionName)
            .returns(typeNameOf<List<Module>>())
            .addKdoc("Collection of all Modules provided by [${moduleAnnotation.simpleName}].")
            .addStatement("return listOf( ${resolvedModules.joinToString(", ")}\n)")
            .build()
    }
}
