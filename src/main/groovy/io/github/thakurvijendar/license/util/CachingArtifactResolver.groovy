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
package io.github.thakurvijendar.license.util

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolvedArtifact

import java.util.concurrent.atomic.AtomicInteger

class CachingArtifactResolver {

    private Map<Map<String, String>, Collection<ResolvedArtifact>> cache = new HashMap<>()
    private Project project

    CachingArtifactResolver(Project project) {
        this.project = project
    }

    Collection<ResolvedArtifact> resolveArtifacts(Map<String, String> spec) {
        Map<String, String> copy = new HashMap<String, String>()
        spec.each { copy.put(it.key.trim(), it.value.trim()) }
        if (!cache.containsKey(copy)) {
            cache.put(copy, doResolveArtifact(copy))
        }
        return cache.get(copy)
    }

    private Collection<ResolvedArtifact> doResolveArtifact(Object spec) {
        Dependency dependency = project.dependencies.create(spec)
        Configuration config = project.configurations.detachedConfiguration(dependency).setTransitive(false)
        try {
            Collection<ResolvedArtifact> artifacts = config.resolvedConfiguration.resolvedArtifacts
            if (artifacts != null) {
                // Exercise #getFile() to download the file and catch exceptions here
                for (ResolvedArtifact artifact : artifacts) {
                    artifact.getFile()
                }
            }
            return artifacts
        } finally {
            project.configurations.remove(config)
        }
    }
}
