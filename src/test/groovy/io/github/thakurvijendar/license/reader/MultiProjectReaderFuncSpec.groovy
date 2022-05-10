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

import io.github.thakurvijendar.license.AbstractGradleRunnerFunctionalSpec
import org.gradle.testkit.runner.TaskOutcome

class MultiProjectReaderFuncSpec  extends AbstractGradleRunnerFunctionalSpec {

    def setup() {
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
                outputDir = "${fixPathForBuildFile(outputDir.absolutePath)}"
                renderers = [new io.github.thakurvijendar.license.render.RawProjectDataJsonRenderer()]
                configurations = []
            }
            dependencies {
                mainConfig "org.apache.commons:commons-lang3:3.7"
            }
        """
    }

    def "same dependencies of the same configuration are merged"() {
        setup:
        newSubBuildFile("sub1") << """
            configurations {
                mainConfig
            }
            repositories {
                mavenCentral()
            }
            dependencies {
                mainConfig "org.apache.commons:commons-lang3:3.7"
                mainConfig "org.jetbrains:annotations:16.0.1"
            }
        """

        when:
        def runResult = runGradleBuild()
        def resultFileGPath = jsonSlurper.parse(rawJsonFile)
        removeDevelopers(resultFileGPath)
        removeLicenseFiles(resultFileGPath)
        def configurationsGPath = resultFileGPath.configurations
        def configurationsString = prettyPrintJson(configurationsGPath)

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        configurationsString == """[
    {
        "dependencies": [
            {
                "group": "org.apache.commons",
                "manifests": [
                    {
                        "licenseUrl": "https://www.apache.org/licenses/LICENSE-2.0.txt",
                        "vendor": "The Apache Software Foundation",
                        "hasPackagedLicense": false,
                        "version": "3.7.0",
                        "license": null,
                        "description": "Apache Commons Lang, a package of Java utility classes for the  classes that are in java.lang's hierarchy, or are considered to be so  standard as to justify existence in java.lang.",
                        "url": "http://commons.apache.org/proper/commons-lang/",
                        "name": "Apache Commons Lang"
                    }
                ],
                "version": "3.7",
                "poms": [
                    {
                        "inceptionYear": "2001",
                        "projectUrl": "http://commons.apache.org/proper/commons-lang/",
                        "description": "\\n  Apache Commons Lang, a package of Java utility classes for the\\n  classes that are in java.lang's hierarchy, or are considered to be so\\n  standard as to justify existence in java.lang.\\n  ",
                        "name": "Apache Commons Lang",
                        "organization": {
                            "url": "https://www.apache.org/",
                            "name": "The Apache Software Foundation"
                        },
                        "licenses": [
                            {
                                "url": "https://www.apache.org/licenses/LICENSE-2.0.txt",
                                "name": "Apache License, Version 2.0"
                            }
                        ]
                    }
                ],
                "empty": false,
                "name": "commons-lang3"
            },
            {
                "group": "org.jetbrains",
                "manifests": [
                    {
                        "licenseUrl": null,
                        "vendor": null,
                        "hasPackagedLicense": false,
                        "version": null,
                        "license": null,
                        "description": null,
                        "url": null,
                        "name": null
                    }
                ],
                "version": "16.0.1",
                "poms": [
                    {
                        "inceptionYear": "",
                        "projectUrl": "https://github.com/JetBrains/java-annotations",
                        "description": "A set of annotations used for code inspection support and code documentation.",
                        "name": "JetBrains Java Annotations",
                        "organization": null,
                        "licenses": [
                            {
                                "url": "http://www.apache.org/license/LICENSE-2.0.txt",
                                "name": "The Apache Software License, Version 2.0"
                            }
                        ]
                    }
                ],
                "empty": false,
                "name": "annotations"
            }
        ],
        "name": "mainConfig"
    }
]"""
    }

    def "different configurations are kept"() {
        setup:
        newSubBuildFile("sub1") << """
            configurations {
                subConfig
            }
            repositories {
                mavenCentral()
            }
            dependencies {
                subConfig "org.apache.commons:commons-lang3:3.7"
            }
        """

        when:
        def runResult = runGradleBuild()
        def resultFileGPath = jsonSlurper.parse(rawJsonFile)
        removeDevelopers(resultFileGPath)
        def configurationsGPath = resultFileGPath.configurations
        def configurationsString = prettyPrintJson(configurationsGPath)

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        configurationsString == """[
    {
        "dependencies": [
            {
                "group": "org.apache.commons",
                "manifests": [
                    {
                        "licenseUrl": "https://www.apache.org/licenses/LICENSE-2.0.txt",
                        "vendor": "The Apache Software Foundation",
                        "hasPackagedLicense": false,
                        "version": "3.7.0",
                        "license": null,
                        "description": "Apache Commons Lang, a package of Java utility classes for the  classes that are in java.lang's hierarchy, or are considered to be so  standard as to justify existence in java.lang.",
                        "url": "http://commons.apache.org/proper/commons-lang/",
                        "name": "Apache Commons Lang"
                    }
                ],
                "version": "3.7",
                "poms": [
                    {
                        "inceptionYear": "2001",
                        "projectUrl": "http://commons.apache.org/proper/commons-lang/",
                        "description": "\\n  Apache Commons Lang, a package of Java utility classes for the\\n  classes that are in java.lang's hierarchy, or are considered to be so\\n  standard as to justify existence in java.lang.\\n  ",
                        "name": "Apache Commons Lang",
                        "organization": {
                            "url": "https://www.apache.org/",
                            "name": "The Apache Software Foundation"
                        },
                        "licenses": [
                            {
                                "url": "https://www.apache.org/licenses/LICENSE-2.0.txt",
                                "name": "Apache License, Version 2.0"
                            }
                        ]
                    }
                ],
                "licenseFiles": [
                    {
                        "fileDetails": [
                            {
                                "licenseUrl": "https://www.apache.org/licenses/LICENSE-2.0",
                                "file": "commons-lang3-3.7.jar/META-INF/LICENSE.txt",
                                "license": "Apache License, Version 2.0"
                            },
                            {
                                "licenseUrl": null,
                                "file": "commons-lang3-3.7.jar/META-INF/NOTICE.txt",
                                "license": null
                            }
                        ]
                    }
                ],
                "empty": false,
                "name": "commons-lang3"
            }
        ],
        "name": "mainConfig"
    },
    {
        "dependencies": [
            {
                "group": "org.apache.commons",
                "manifests": [
                    {
                        "licenseUrl": "https://www.apache.org/licenses/LICENSE-2.0.txt",
                        "vendor": "The Apache Software Foundation",
                        "hasPackagedLicense": false,
                        "version": "3.7.0",
                        "license": null,
                        "description": "Apache Commons Lang, a package of Java utility classes for the  classes that are in java.lang's hierarchy, or are considered to be so  standard as to justify existence in java.lang.",
                        "url": "http://commons.apache.org/proper/commons-lang/",
                        "name": "Apache Commons Lang"
                    }
                ],
                "version": "3.7",
                "poms": [
                    {
                        "inceptionYear": "2001",
                        "projectUrl": "http://commons.apache.org/proper/commons-lang/",
                        "description": "\\n  Apache Commons Lang, a package of Java utility classes for the\\n  classes that are in java.lang's hierarchy, or are considered to be so\\n  standard as to justify existence in java.lang.\\n  ",
                        "name": "Apache Commons Lang",
                        "organization": {
                            "url": "https://www.apache.org/",
                            "name": "The Apache Software Foundation"
                        },
                        "licenses": [
                            {
                                "url": "https://www.apache.org/licenses/LICENSE-2.0.txt",
                                "name": "Apache License, Version 2.0"
                            }
                        ]
                    }
                ],
                "licenseFiles": [
                    {
                        "fileDetails": [
                            {
                                "licenseUrl": "https://www.apache.org/licenses/LICENSE-2.0",
                                "file": "commons-lang3-3.7.jar/META-INF/LICENSE.txt",
                                "license": "Apache License, Version 2.0"
                            },
                            {
                                "licenseUrl": null,
                                "file": "commons-lang3-3.7.jar/META-INF/NOTICE.txt",
                                "license": null
                            }
                        ]
                    }
                ],
                "empty": false,
                "name": "commons-lang3"
            }
        ],
        "name": "subConfig"
    }
]"""
    }

    def "project filtering is respected"() {
        setup:
        buildFile.text = """
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
                outputDir = "${fixPathForBuildFile(outputDir.absolutePath)}"
                projects = [project]
                renderers = [new io.github.thakurvijendar.license.render.RawProjectDataJsonRenderer()]
                configurations = []
            }
            dependencies {
                mainConfig "org.apache.commons:commons-lang3:3.7"
            }
        """

        newSubBuildFile("sub1") << """
            configurations {
                subConfig
            }
            repositories {
                mavenCentral()
            }
            dependencies {
                subConfig "org.apache.commons:commons-lang3:3.7"
            }
        """

        when:
        def runResult = runGradleBuild()
        def resultFileGPath = jsonSlurper.parse(rawJsonFile)
        removeDevelopers(resultFileGPath)
        def configurationsGPath = resultFileGPath.configurations
        def configurationsString = prettyPrintJson(configurationsGPath)

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        configurationsString == """[
    {
        "dependencies": [
            {
                "group": "org.apache.commons",
                "manifests": [
                    {
                        "licenseUrl": "https://www.apache.org/licenses/LICENSE-2.0.txt",
                        "vendor": "The Apache Software Foundation",
                        "hasPackagedLicense": false,
                        "version": "3.7.0",
                        "license": null,
                        "description": "Apache Commons Lang, a package of Java utility classes for the  classes that are in java.lang's hierarchy, or are considered to be so  standard as to justify existence in java.lang.",
                        "url": "http://commons.apache.org/proper/commons-lang/",
                        "name": "Apache Commons Lang"
                    }
                ],
                "version": "3.7",
                "poms": [
                    {
                        "inceptionYear": "2001",
                        "projectUrl": "http://commons.apache.org/proper/commons-lang/",
                        "description": "\\n  Apache Commons Lang, a package of Java utility classes for the\\n  classes that are in java.lang's hierarchy, or are considered to be so\\n  standard as to justify existence in java.lang.\\n  ",
                        "name": "Apache Commons Lang",
                        "organization": {
                            "url": "https://www.apache.org/",
                            "name": "The Apache Software Foundation"
                        },
                        "licenses": [
                            {
                                "url": "https://www.apache.org/licenses/LICENSE-2.0.txt",
                                "name": "Apache License, Version 2.0"
                            }
                        ]
                    }
                ],
                "licenseFiles": [
                    {
                        "fileDetails": [
                            {
                                "licenseUrl": "https://www.apache.org/licenses/LICENSE-2.0",
                                "file": "commons-lang3-3.7.jar/META-INF/LICENSE.txt",
                                "license": "Apache License, Version 2.0"
                            },
                            {
                                "licenseUrl": null,
                                "file": "commons-lang3-3.7.jar/META-INF/NOTICE.txt",
                                "license": null
                            }
                        ]
                    }
                ],
                "empty": false,
                "name": "commons-lang3"
            }
        ],
        "name": "mainConfig"
    }
]"""
    }

    def "repositories of the sub-projects are used"() {
        setup:
        buildFile.text = """
            plugins {
                id 'io.github.thakurvijendar.dependency-license-report'
            }

            import io.github.thakurvijendar.license.render.*
            licenseReport {
                outputDir = "${fixPathForBuildFile(outputDir.absolutePath)}"
                renderers = [new io.github.thakurvijendar.license.render.RawProjectDataJsonRenderer()]
                configurations = []
            }
        """

        newSubBuildFile("sub1") << """
            configurations {
                subConfig
            }
            repositories {
                mavenCentral()
            }
            dependencies {
                subConfig "org.jetbrains:annotations:16.0.1"
            }
        """

        when:
        def runResult = runGradleBuild()
        def resultFileGPath = jsonSlurper.parse(rawJsonFile)
        removeDevelopers(resultFileGPath)
        removeLicenseFiles(resultFileGPath)
        def configurationsGPath = resultFileGPath.configurations
        def configurationsString = prettyPrintJson(configurationsGPath)

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        configurationsString == """[
    {
        "dependencies": [
            {
                "group": "org.jetbrains",
                "manifests": [
                    {
                        "licenseUrl": null,
                        "vendor": null,
                        "hasPackagedLicense": false,
                        "version": null,
                        "license": null,
                        "description": null,
                        "url": null,
                        "name": null
                    }
                ],
                "version": "16.0.1",
                "poms": [
                    {
                        "inceptionYear": "",
                        "projectUrl": "https://github.com/JetBrains/java-annotations",
                        "description": "A set of annotations used for code inspection support and code documentation.",
                        "name": "JetBrains Java Annotations",
                        "organization": null,
                        "licenses": [
                            {
                                "url": "http://www.apache.org/license/LICENSE-2.0.txt",
                                "name": "The Apache Software License, Version 2.0"
                            }
                        ]
                    }
                ],
                "empty": false,
                "name": "annotations"
            }
        ],
        "name": "subConfig"
    }
]"""
    }

    def "only defined configurations (and their extended forms) are considered"() {
        setup:
        newSubBuildFile("sub1") << """
            configurations {
                subConfig
            }
            repositories {
                mavenCentral()
            }
            dependencies {
                subConfig "org.jetbrains:annotations:16.0.1"
            }
        """

        buildFile.text = """
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
                outputDir = "${fixPathForBuildFile(outputDir.absolutePath)}"
                renderers = [new io.github.thakurvijendar.license.render.RawProjectDataJsonRenderer()]
                configurations = ['mainConfig']
            }
            dependencies {
                mainConfig "org.apache.commons:commons-lang3:3.7"
            }
        """

        when:
        def runResult = runGradleBuild()
        def resultFileGPath = jsonSlurper.parse(rawJsonFile)
        removeDevelopers(resultFileGPath)
        def configurationsGPath = resultFileGPath.configurations

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        configurationsGPath.name == ["mainConfig"]
    }

    def "use all configurations if none are defined"() {
        setup:
        newSubBuildFile("sub1") << """
            configurations {
                subConfig
            }
            repositories {
                mavenCentral()
            }
            dependencies {
                subConfig "org.jetbrains:annotations:16.0.1"
            }
        """

        when:
        def runResult = runGradleBuild()
        def resultFileGPath = jsonSlurper.parse(rawJsonFile)
        removeDevelopers(resultFileGPath)
        def configurationsGPath = resultFileGPath.configurations

        then:
        runResult.task(":generateLicenseReport").outcome == TaskOutcome.SUCCESS

        configurationsGPath.name == ["mainConfig", "subConfig"]
    }

    static void removeDevelopers(Map rawFile) {
        rawFile.configurations*.dependencies.flatten().poms.flatten().each { it.remove("developers") }
    }

    static void removeLicenseFiles(Map rawFile) {
        rawFile.configurations*.dependencies.flatten().each { it.remove("licenseFiles") }
    }
}
