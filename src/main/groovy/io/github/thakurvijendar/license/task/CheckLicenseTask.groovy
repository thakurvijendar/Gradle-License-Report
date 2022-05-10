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

import io.github.thakurvijendar.license.LicenseReportExtension
import io.github.thakurvijendar.license.check.LicenseChecker
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.PathSensitivity

@CacheableTask
class CheckLicenseTask extends DefaultTask {

    final static String PROJECT_JSON_FOR_LICENSE_CHECKING_FILE = "project-licenses-for-check-license-task.json"
    final static String NOT_PASSED_DEPENDENCIES_FILE = "dependencies-without-allowed-license.json"

    private static Logger LOGGER = Logging.getLogger(CheckLicenseTask.class)
    private LicenseReportExtension config = getProject().licenseReport

    CheckLicenseTask() {
        group = 'Checking'
        description = 'Check if License could be used'
    }

    @Input
    Object getAllowedLicenseFile() {
        return config.allowedLicensesFile
    }

    @InputFile
    @PathSensitive(PathSensitivity.NAME_ONLY)
    File getProjectDependenciesData() {
        return new File("${config.outputDir}/${PROJECT_JSON_FOR_LICENSE_CHECKING_FILE}")
    }

    @OutputFile
    File getNotPassedDependenciesFile() {
        new File("${config.outputDir}/$NOT_PASSED_DEPENDENCIES_FILE")
    }

    @TaskAction
    void checkLicense() {
        LOGGER.info("Startup CheckLicense for ${getProject().name}")
        LicenseChecker licenseChecker = new LicenseChecker()
        LOGGER.info("Check licenses if they are allowed to use.")
        licenseChecker.checkAllDependencyLicensesAreAllowed(
            getAllowedLicenseFile(), getProjectDependenciesData(), notPassedDependenciesFile)
    }
}
