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
package io.github.thakurvijendar.license.filter

import io.github.thakurvijendar.license.ImportedModuleBundle
import io.github.thakurvijendar.license.LicenseFileData
import io.github.thakurvijendar.license.PomData
import io.github.thakurvijendar.license.ProjectData

class ReduceDuplicateLicensesFilter implements DependencyFilter {

    @Override
    ProjectData filter(ProjectData projectData) {
        // remove pom duplicates
        projectData.configurations*.dependencies.flatten().poms.flatten().forEach { PomData pom ->
            pom.licenses = pom.licenses.unique()
        }

        // remove license-file duplicates
        projectData.configurations*.dependencies.flatten().licenseFiles.flatten().forEach { LicenseFileData files ->
            files.fileDetails = files.fileDetails.unique()
        }

        // remove imported modules duplicates
        projectData.importedModules.forEach { ImportedModuleBundle bundle ->
            bundle.modules = bundle.modules.unique()
        }

        return projectData
    }
}
