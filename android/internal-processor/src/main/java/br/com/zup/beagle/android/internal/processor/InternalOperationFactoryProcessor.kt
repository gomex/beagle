/*
 * Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
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
 */

package br.com.zup.beagle.android.internal.processor

import br.com.zup.beagle.compiler.shared.ANDROID_OPERATION
import br.com.zup.beagle.compiler.shared.RegisteredOperationGenerator
import br.com.zup.beagle.compiler.shared.error
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import java.io.IOException
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class InternalOperationFactoryProcessor(
    private val processingEnv: ProcessingEnvironment,
    private val operationGenerator: RegisteredOperationGenerator =
        RegisteredOperationGenerator()) {

    fun process(
        basePackageName: String,
        roundEnvironment: RoundEnvironment
    ) {
        val className = "InternalOperationFactory"

        val typeSpec = TypeSpec.objectBuilder(className)
            .addModifiers(KModifier.INTERNAL)
            .addFunction(operationGenerator.generate(roundEnvironment, processingEnv))
            .build()

        val beagleSetupFile = FileSpec.builder(
            basePackageName,
            className
        )
            .addImport(ANDROID_OPERATION.packageName, ANDROID_OPERATION.className)
            .addType(typeSpec)
            .build()

        try {
            beagleSetupFile.writeTo(processingEnv.filer)
        } catch (e: IOException) {
            val errorMessage = "Error when trying to generate code.\n${e.message!!}"
            processingEnv.messager.error(errorMessage)
        }
    }
}