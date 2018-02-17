/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.plugins.jsoup

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.ClosureBackedAction
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskInputs
import org.jsoup.nodes.Document
import java.util.concurrent.Callable

val DEFAULT_TRANSFORM_EXTENSIONS = listOf("html")

open class JsoupCopyExtension(val task: Copy) {

    fun plugins(vararg plugins: Any) {
        task.project.files(plugins).forEach {
            task.inputs.file(it)
            task.project.apply {
                from(it)
                to(this@JsoupCopyExtension)
            }
        }
    }

    fun transform(action: Action<JsoupTransformTarget>) {
        transform(DEFAULT_TRANSFORM_EXTENSIONS.toTypedArray(), action)
    }

    fun transform(extensions: Array<String>, action: Closure<Any>) {
        transform(extensions, ClosureBackedAction(action))
    }

    fun transform(extensions: Array<String>,
                  action: Action<JsoupTransformTarget>) {

        task.eachFile {
            if (extensions.any { this.name.endsWith(".${it}") }) {
                this.filter(mapOf("fileCopyDetails" to this, "action" to action), JsoupFilterReader::class.java)
            }
        }
    }

    fun transformDocument(action: Action<Document>) {
        transformDocument(DEFAULT_TRANSFORM_EXTENSIONS.toTypedArray(), action)
    }

    fun transformDocument(extensions: Array<String>, action: Closure<Any>) {
        transformDocument(extensions, ClosureBackedAction(action))
    }

    fun transformDocument(extensions: Array<String>,
                          action: Action<Document>) {

        transform(extensions, Action<JsoupTransformTarget> {
            action.execute(this.document)
        })
    }

    fun getProject(): Project {
        return task.project
    }

    fun getInputs(): TaskInputs {
        return task.inputs
    }
}
