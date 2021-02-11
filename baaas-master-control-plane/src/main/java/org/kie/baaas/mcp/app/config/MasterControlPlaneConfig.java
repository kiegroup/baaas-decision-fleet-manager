/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.kie.baaas.mcp.app.config;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotBlank;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Stores the configuration of the single CCP that is allowed to be registered for the
 * current implementation of the MCP.
 */
@ApplicationScoped
public class MasterControlPlaneConfig {

    @NotBlank
    @ConfigProperty(name = "baaas.mcp.urls.api-base")
    String apiBaseUrl;

    @NotBlank
    @ConfigProperty(name = "baaas.ccp.urls.dmn-jit")
    String ccpDmnJitUrl;

    @NotBlank
    @ConfigProperty(name = "baaas.ccp.urls.k8s-api")
    String ccpKubernetesApiUrl;

    @NotBlank
    @ConfigProperty(name = "baaas.ccp.namespace")
    String ccpNamespace;

    @NotBlank
    @ConfigProperty(name = "baaas.kafka.bootstrap-urls")
    String kafkaBootstrapServers;

    @NotBlank
    @ConfigProperty(name = "baaas.kafka.secret-name")
    String kafkaSecretName;

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public String getCcpDmnJitUrl() {
        return ccpDmnJitUrl;
    }

    public String getCcpKubernetesApiUrl() {
        return ccpKubernetesApiUrl;
    }

    public String getCcpNamespace() {
        return ccpNamespace;
    }

    public String getKafkaBootstrapServers() {
        return this.kafkaBootstrapServers;
    }

    public String getKafkaSecretName() {
        return kafkaSecretName;
    }
}