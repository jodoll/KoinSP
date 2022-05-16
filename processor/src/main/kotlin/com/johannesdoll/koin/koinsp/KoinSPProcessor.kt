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
import com.google.devtools.ksp.symbol.*
import com.johannesdoll.koin.koinsp.api.KoinSPModule
import org.koin.core.module.Module
import java.io.BufferedWriter
import kotlin.reflect.KClass

class KoinSPProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        println("Running processor")
        val koinModules = resolver
            .getSymbolsWithAnnotation(KoinSPModule::class.qualifiedName!!)
            .filter { it is KSFunctionDeclaration || it is KSPropertyGetter || it is KSPropertyDeclaration }

        if (!koinModules.iterator().hasNext()) return emptyList()

        createKoinFile(koinModules.mapNotNull { it.containingFile }).use { writer ->
            writer.writePackage(KoinSPProcessor::class.java.packageName)
            writer.writeImports(Module::class)
            writer.writeKoinModulesFun(koinModules, resolver)
        }

        return emptyList()
    }

    private fun createKoinFile(dependencies: Sequence<KSFile>): BufferedWriter {
        println("Creating new file")
        return codeGenerator.createNewFile(
            Dependencies(true, *dependencies.toList().toTypedArray()),
            KoinSPProcessor::class.java.packageName,
            "KoinSpModules"
        ).bufferedWriter()
    }

    private fun BufferedWriter.writePackage(packageName: String) {
        appendLine("package $packageName")
        appendLine()
    }

    private fun BufferedWriter.writeImports(vararg classes: KClass<*>) {
        classes
            .mapNotNull { it.qualifiedName }
            .sorted()
            .forEach { appendLine("import $it") }
        appendLine()
    }

    private fun BufferedWriter.writeKoinModulesFun(
        koinModules: Sequence<KSNode>,
        resolver: Resolver
    ) {
        appendLine("fun koinSPModules() : ${List::class.simpleName}<${Module::class.simpleName}> = listOf(")
        koinModules.forEach { it.accept(KoinSPVisitor(resolver, this), Unit) }
        appendLine(")")

        appendLine()
    }
}