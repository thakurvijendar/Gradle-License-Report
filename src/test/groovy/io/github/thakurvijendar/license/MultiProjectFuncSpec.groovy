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
package io.github.thakurvijendar.license

import org.gradle.testkit.runner.TaskOutcome

import static io.github.thakurvijendar.license.reader.ProjectReaderFuncSpec.removeDevelopers

class MultiProjectFuncSpec extends AbstractGradleRunnerFunctionalSpec {

    def "plugin is executed in each module independently if configured for submodules"() {
        setup:
        settingsGradle = new File(testProjectDir, "settings.gradle")

        newSubBuildFile("sub1") << """
            plugins {
                id 'io.github.thakurvijendar.dependency-license-report'
            }
            configurations {
                mainConfig
            }
            repositories {
                mavenCentral()
            }

            import io.github.thakurvijendar.license.render.*
            licenseReport {
                renderers = [new io.github.thakurvijendar.license.render.RawProjectDataJsonRenderer()]
                filters = new io.github.thakurvijendar.license.filter.LicenseBundleNormalizer()
                configurations = []
            }
            dependencies {
                mainConfig "org.apache.commons:commons-lang3:3.7"
            }
        """

        newSubBuildFile("sub2") << """
            plugins {
                id 'io.github.thakurvijendar.dependency-license-report'
            }
            configurations {
                mainConfig
            }
            repositories {
                mavenCentral()
            }

            import io.github.thakurvijendar.license.render.*
            licenseReport {
                renderers = [new io.github.thakurvijendar.license.render.RawProjectDataJsonRenderer()]
                filters = new io.github.thakurvijendar.license.filter.LicenseBundleNormalizer()
                configurations = []
            }
            dependencies {
                mainConfig "javax.annotation:javax.annotation-api:1.3.2"
            }
        """

        when:
        def runResult = runGradleBuild()
        def sub1RawGPath = jsonSlurper.parse(new File(testProjectDir, "sub1/build/reports/dependency-license/raw-project-data.json"))
        def sub2RawGPath = jsonSlurper.parse(new File(testProjectDir, "sub2/build/reports/dependency-license/raw-project-data.json"))
        removeDevelopers(sub1RawGPath)
        removeDevelopers(sub2RawGPath)
        def configurationsSub1String = prettyPrintJson(sub1RawGPath.configurations)
        def configurationsSub2String = prettyPrintJson(sub2RawGPath.configurations)

        then:
        runResult.task(":sub1:generateLicenseReport").outcome == TaskOutcome.SUCCESS
        runResult.task(":sub2:generateLicenseReport").outcome == TaskOutcome.SUCCESS

        !new File(testProjectDir, "build/reports/dependency-license").exists()
        new File(testProjectDir, "sub1/build/reports/dependency-license/commons-lang3-3.7.jar/META-INF/LICENSE.txt").exists()
        new File(testProjectDir, "sub1/build/reports/dependency-license/commons-lang3-3.7.jar/META-INF/NOTICE.txt").exists()
        new File(testProjectDir, "sub2/build/reports/dependency-license/javax.annotation-api-1.3.2.jar/META-INF/LICENSE.txt").exists()

        configurationsSub1String.contains("commons-lang3")
        !configurationsSub1String.contains("javax.annotation-api")

        !configurationsSub2String.contains("commons-lang3")
        configurationsSub2String.contains("javax.annotation-api")
    }

    def "plugin is executed in module independently and globally on root project when configured in allprojects"() {
        setup:
        settingsGradle = new File(testProjectDir, "settings.gradle")

        buildFile << """
            plugins {
                id 'io.github.thakurvijendar.dependency-license-report'
            }
            configurations {
                mainConfig
            }
            repositories {
                mavenCentral()
            }

            import io.github.thakurvijendar.license.render.*
            licenseReport {
                renderers = [new io.github.thakurvijendar.license.render.RawProjectDataJsonRenderer()]
                filters = new io.github.thakurvijendar.license.filter.LicenseBundleNormalizer()
                configurations = []
            }
            dependencies {
                mainConfig "joda-time:joda-time:2.9.9"
            }
"""

        newSubBuildFile("sub1") << """
            plugins {
                id 'io.github.thakurvijendar.dependency-license-report'
            }
            configurations {
                mainConfig
            }
            repositories {
                mavenCentral()
            }

            import io.github.thakurvijendar.license.render.*
            licenseReport {
                renderers = [new io.github.thakurvijendar.license.render.RawProjectDataJsonRenderer()]
                filters = new io.github.thakurvijendar.license.filter.LicenseBundleNormalizer()
                configurations = []
            }
            dependencies {
                mainConfig "org.apache.commons:commons-lang3:3.7"
            }
        """

        newSubBuildFile("sub2") << """
            plugins {
                id 'io.github.thakurvijendar.dependency-license-report'
            }
            configurations {
                mainConfig
            }
            repositories {
                mavenCentral()
            }

            import io.github.thakurvijendar.license.render.*
            licenseReport {
                renderers = [new io.github.thakurvijendar.license.render.RawProjectDataJsonRenderer()]
                filters = new io.github.thakurvijendar.license.filter.LicenseBundleNormalizer()
                configurations = []
            }
            dependencies {
                mainConfig "javax.annotation:javax.annotation-api:1.3.2"
            }
        """

        when:
        def runResult = runGradleBuild()
        def rootRawGPath = jsonSlurper.parse(new File(testProjectDir, "build/reports/dependency-license/raw-project-data.json"))
        def sub1RawGPath = jsonSlurper.parse(new File(testProjectDir, "sub1/build/reports/dependency-license/raw-project-data.json"))
        def sub2RawGPath = jsonSlurper.parse(new File(testProjectDir, "sub2/build/reports/dependency-license/raw-project-data.json"))
        def configurationsRootString = prettyPrintJson(rootRawGPath.configurations)
        def configurationsSub1String = prettyPrintJson(sub1RawGPath.configurations)
        def configurationsSub2String = prettyPrintJson(sub2RawGPath.configurations)

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS
        runResult.task(":sub1:generateLicenseReport").outcome == TaskOutcome.SUCCESS
        runResult.task(":sub2:generateLicenseReport").outcome == TaskOutcome.SUCCESS

        // root project should contains all the deps
        new File(testProjectDir, "build/reports/dependency-license/joda-time-2.9.9.jar/META-INF/LICENSE.txt").exists()
        new File(testProjectDir, "build/reports/dependency-license/joda-time-2.9.9.jar/META-INF/NOTICE.txt").exists()
        new File(testProjectDir, "build/reports/dependency-license/commons-lang3-3.7.jar/META-INF/LICENSE.txt").exists()
        new File(testProjectDir, "build/reports/dependency-license/commons-lang3-3.7.jar/META-INF/NOTICE.txt").exists()
        new File(testProjectDir, "build/reports/dependency-license/javax.annotation-api-1.3.2.jar/META-INF/LICENSE.txt").exists()

        // sub1
        !new File(testProjectDir, "sub1/build/reports/dependency-license/joda-time-2.9.9.jar").exists()
        new File(testProjectDir, "sub1/build/reports/dependency-license/commons-lang3-3.7.jar/META-INF/LICENSE.txt").exists()
        new File(testProjectDir, "sub1/build/reports/dependency-license/commons-lang3-3.7.jar/META-INF/NOTICE.txt").exists()
        !new File(testProjectDir, "sub1/build/reports/dependency-license/javax.annotation-api-1.3.2.jar").exists()

        // sub2
        !new File(testProjectDir, "sub2/build/reports/dependency-license/joda-time-2.9.9.jar").exists()
        !new File(testProjectDir, "sub2/build/reports/dependency-license/commons-lang3-3.7.jar").exists()
        new File(testProjectDir, "sub2/build/reports/dependency-license/javax.annotation-api-1.3.2.jar/META-INF/LICENSE.txt").exists()

        configurationsRootString.contains("joda-time")
        configurationsRootString.contains("commons-lang3")
        configurationsRootString.contains("javax.annotation-api")

        !configurationsSub1String.contains("joda-time")
        configurationsSub1String.contains("commons-lang3")
        !configurationsSub1String.contains("javax.annotation-api")

        !configurationsSub2String.contains("joda-time")
        !configurationsSub2String.contains("commons-lang3")
        configurationsSub2String.contains("javax.annotation-api")
    }
}
