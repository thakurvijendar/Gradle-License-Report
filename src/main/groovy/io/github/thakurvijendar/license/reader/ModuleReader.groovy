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
package io.github.thakurvijendar.license.reader

import io.github.thakurvijendar.license.LicenseReportExtension
import io.github.thakurvijendar.license.ModuleData
import io.github.thakurvijendar.license.task.ReportTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

interface ModuleReader {
    ModuleData read(Project project, ResolvedDependency dependency)
}

class ModuleReaderImpl implements ModuleReader {
    private Logger LOGGER = Logging.getLogger(ReportTask.class)

    private LicenseReportExtension config
    private PomReader pomReader
    private ManifestReader manifestReader
    private LicenseFilesReader filesReader

    ModuleReaderImpl(LicenseReportExtension config) {
        this.config = config
        this.pomReader = new PomReader(config)
        this.manifestReader = new ManifestReader(config)
        this.filesReader = new LicenseFilesReader(config)
    }

    ModuleData read(Project project, ResolvedDependency dependency) {
        ModuleData moduleData = new ModuleData(dependency.moduleGroup, dependency.moduleName, dependency.moduleVersion)
        dependency.moduleArtifacts.each { ResolvedArtifact artifact ->
            LOGGER.info("Processing artifact: $artifact ($artifact.file)")
            if (artifact.file.exists()){
                def pom = pomReader.readPomData(project, artifact)
                def manifest = manifestReader.readManifestData(artifact)
                def licenseFile = filesReader.read(artifact)

                if (pom) moduleData.poms << pom
                if (manifest) moduleData.manifests << manifest
                if (licenseFile) moduleData.licenseFiles << licenseFile
            } else {
                LOGGER.info("Skipping artifact file $artifact.file as it does not exist")
            }
        }
        return moduleData
    }
}

class CachedModuleReader implements ModuleReader {
    private Map<String, ModuleData> moduleDataCache = [:]
    private ModuleReader actualReader

    CachedModuleReader(LicenseReportExtension config) {
        this.actualReader = new ModuleReaderImpl(config)
    }

    ModuleData read(Project project, ResolvedDependency dependency) {
        String dataName = "${dependency.moduleGroup}:${dependency.moduleName}:${dependency.moduleVersion}"
        return moduleDataCache.computeIfAbsent(dataName) {
            actualReader.read(project, dependency)
        }
    }
}
