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

class KoinSPProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor{
    override fun process(resolver: Resolver): List<KSAnnotated> {
        println("Running processor")
        val koinModules = resolver
            .getSymbolsWithAnnotation(KoinSPModule::class.qualifiedName!!)
            .filter { it is KSFunctionDeclaration || it is KSPropertyGetter || it is KSPropertyDeclaration}

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