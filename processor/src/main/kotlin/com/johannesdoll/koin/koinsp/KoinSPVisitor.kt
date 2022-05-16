package com.johannesdoll.koin.koinsp

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.johannesdoll.koin.koinsp.api.KoinSPModule
import org.koin.core.module.Module
import java.io.BufferedWriter

class KoinSPVisitor(
    private val resolver: Resolver,
    private val writer: BufferedWriter,
    private var indent: String = "    "
) : KSVisitorVoid() {
    companion object {
        fun KSDeclaration.wrongReturnType(): () -> String = {
            "Functions with ${KoinSPModule::class.simpleName} must return ${Module::class.simpleName}: $sourceLocation"
        }
    }

    private val moduleType by lazy {
        resolver
            .getClassDeclarationByName(Module::class.qualifiedName!!)!!
            .asType(emptyList())
    }

    override fun visitPropertyGetter(getter: KSPropertyGetter, data: Unit) {
        val returnType = requireNotNull(getter.returnType, getter.receiver.wrongReturnType())
        require(returnType.resolve().isAssignableFrom(moduleType), getter.receiver.wrongReturnType())
        require(getter.receiver.isStatic()) { "Functions with ${KoinSPModule::class.simpleName} must be top level declarations or declared on objects: ${getter.receiver.sourceLocation}" }
        require(getter.receiver.isPublic()) { "Functions with ${KoinSPModule::class.simpleName} must be public: ${getter.receiver.sourceLocation}" }

        val qualifiedName =
            requireNotNull(getter.receiver.qualifiedName?.asString()) { "Can't determine qualified name for ${getter.receiver.sourceLocation}" }
        writer.appendLine("$indent$qualifiedName,")
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        val returnType = requireNotNull(property.type, property.wrongReturnType())
        require(returnType.resolve().isAssignableFrom(moduleType), property.wrongReturnType())
        require(property.isStatic()) { "Functions with ${KoinSPModule::class.simpleName} must be top level declarations or declared on objects: ${property.sourceLocation}" }
        require(property.isPublic()) { "Functions with ${KoinSPModule::class.simpleName} must be public: ${property.sourceLocation}" }

        val qualifiedName =
            requireNotNull(property.qualifiedName?.asString()) { "Can't determine qualified name for ${property.sourceLocation}" }
        writer.appendLine("$indent$qualifiedName,")
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        val returnType = requireNotNull(function.returnType, function.wrongReturnType())
        require(returnType.resolve().isAssignableFrom(moduleType), function.wrongReturnType())
        require(function.isCallableWithoutParameters()) { "Functions with ${KoinSPModule::class.simpleName} must be callable without parameters: $function.sourceLocation" }
        require(function.isStatic()) { "Functions with ${KoinSPModule::class.simpleName} must be top level declarations or declared on objects: $function.sourceLocation" }
        require(function.isPublic()) { "Functions with ${KoinSPModule::class.simpleName} must be public: ${function.sourceLocation}" }

        val qualifiedName =
            requireNotNull(function.qualifiedName?.asString()) { "Can't determine qualified name for ${function.sourceLocation}" }
        writer.appendLine("$indent$qualifiedName(),")
    }

    private fun KSFunctionDeclaration.isCallableWithoutParameters(): Boolean =
        parameters.isEmpty() || parameters.all { it.hasDefault }

    private fun KSDeclaration.isStatic(): Boolean =
        parentDeclaration == null || closestClassDeclaration()?.classKind == ClassKind.OBJECT

}

val KSDeclaration.sourceLocation
    get() = "${qualifiedName?.asString()}(${containingFile?.fileName}:${(location as? FileLocation)?.lineNumber})"