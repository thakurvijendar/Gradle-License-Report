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
package io.github.thakurvijendar.license.render

import io.github.thakurvijendar.license.LicenseReportExtension
import io.github.thakurvijendar.license.ProjectData
import groovy.json.JsonBuilder

class RawProjectDataJsonRenderer implements ReportRenderer {
    static final String RAW_PROJECT_JSON_NAME = "raw-project-data.json"

    @Override
    void render(ProjectData data) {
        LicenseReportExtension config = data.project?.licenseReport
        File outputFile = new File(config.outputDir, RAW_PROJECT_JSON_NAME)
        outputFile.createNewFile()

        def project = data.project
        data.project = null

        def json = new JsonBuilder(data).toPrettyString()
        outputFile << json

        data.project = project
    }
}
