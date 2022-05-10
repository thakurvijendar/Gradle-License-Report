/*
 * Copyright 2018 Evgeny Naumenko <jk.vc@mail.ru>
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
package io.github.thakurvijendar.license.task

import io.github.thakurvijendar.license.reader.ProjectReader
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath

@CacheableTask
class CacheableReportTask extends ReportTask {

    @Classpath
    FileCollection getClasspath() {
        getConfig().projects
            .collectMany { ProjectReader.findConfiguredConfigurations(it, getConfig()) }
            .inject(project.files(), { FileCollection memo, eachConfiguration -> memo + eachConfiguration })
    }
}
