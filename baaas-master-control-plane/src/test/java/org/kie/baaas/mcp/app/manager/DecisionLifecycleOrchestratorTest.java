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

package org.kie.baaas.mcp.app.manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.baaas.mcp.api.decisions.DecisionRequest;
import org.kie.baaas.mcp.app.ccp.ClusterControlPlaneClient;
import org.kie.baaas.mcp.app.ccp.ClusterControlPlaneSelector;
import org.kie.baaas.mcp.app.ccp.client.ClusterControlPlaneClientFactory;
import org.kie.baaas.mcp.app.exceptions.MasterControlPlaneException;
import org.kie.baaas.mcp.app.listener.ListenerManager;
import org.kie.baaas.mcp.app.model.ClusterControlPlane;
import org.kie.baaas.mcp.app.model.Decision;
import org.kie.baaas.mcp.app.model.DecisionVersion;
import org.kie.baaas.mcp.app.model.deployment.Deployment;
import org.kie.baaas.mcp.app.storage.DecisionDMNStorage;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DecisionLifecycleOrchestratorTest {

    @Mock
    private ClusterControlPlaneClient client;

    @Mock
    private ClusterControlPlaneSelector selector;

    @Mock
    private ClusterControlPlane clusterControlPlane;

    @Mock
    private DecisionManager decisionManager;

    @Mock
    private DecisionDMNStorage decisionDMNStorage;

    @Mock
    private ClusterControlPlaneClientFactory clientFactory;

    @Mock
    private ListenerManager listenerManager;

    @InjectMocks
    private DecisionLifecycleOrchestrator orchestrator;

    @Test
    public void createOrUpdateDecision() {
        String customerId = "foo";
        DecisionRequest request = mock(DecisionRequest.class);
        DecisionVersion decisionVersion = mock(DecisionVersion.class);
        Decision decision = mock(Decision.class);

        when(decisionVersion.getDecision()).thenReturn(decision);
        when(decisionManager.createOrUpdateVersion(customerId, request)).thenReturn(decisionVersion);
        when(selector.selectControlPlaneForDeployment(decision)).thenReturn(clusterControlPlane);
        when(clientFactory.createClientFor(clusterControlPlane)).thenReturn(client);

        DecisionVersion created = orchestrator.createOrUpdateVersion(customerId, request);
        assertThat(created, is(notNullValue()));
        assertThat(created, equalTo(decisionVersion));

        verify(client).deploy(decisionVersion);
    }

    @Test
    public void createOrUpdateDecision_recordsFailure() {

        String customerId = "foo";
        String decisionId = "bob";
        long version = 1l;
        DecisionRequest request = mock(DecisionRequest.class);
        DecisionVersion decisionVersion = mock(DecisionVersion.class);
        when(decisionVersion.getVersion()).thenReturn(version);
        Decision decision = mock(Decision.class);
        when(decision.getId()).thenReturn(decisionId);

        when(decisionVersion.getDecision()).thenReturn(decision);
        when(decisionManager.createOrUpdateVersion(customerId, request)).thenReturn(decisionVersion);
        when(selector.selectControlPlaneForDeployment(decision)).thenReturn(clusterControlPlane);
        when(clientFactory.createClientFor(clusterControlPlane)).thenReturn(client);
        doThrow(new RuntimeException("Nope!")).when(client).deploy(decisionVersion);

        Assertions.assertThrows(MasterControlPlaneException.class, () -> {
            orchestrator.createOrUpdateVersion(customerId, request);
        });

        verify(decisionManager).failed(eq(customerId), eq(decisionId), eq(version), any(Deployment.class));
    }

    @Test
    public void deleteDecision() {

        String customerId = "foo";
        String decisionName = "bar";
        Decision decision = mock(Decision.class);

        when(decisionManager.deleteDecision(customerId, decisionName)).thenReturn(decision);
        when(selector.selectControlPlaneForDeployment(decision)).thenReturn(clusterControlPlane);
        when(clientFactory.createClientFor(clusterControlPlane)).thenReturn(client);

        Decision deleted = orchestrator.deleteDecision(customerId, decisionName);
        assertThat(deleted, is(notNullValue()));
        assertThat(deleted, equalTo(decision));

        verify(client).delete(decision);
        verify(decisionDMNStorage).deleteDMN(customerId, deleted);
    }

    @Test
    public void deleteVersion() {
        String customerId = "foo";
        String decisionName = "bar";
        long version = 2l;

        DecisionVersion decisionVersion = mock(DecisionVersion.class);
        Decision decision = mock(Decision.class);
        when(decisionVersion.getDecision()).thenReturn(decision);

        when(decisionManager.deleteVersion(customerId, decisionName, version)).thenReturn(decisionVersion);
        when(selector.selectControlPlaneForDeployment(decision)).thenReturn(clusterControlPlane);
        when(clientFactory.createClientFor(clusterControlPlane)).thenReturn(client);

        DecisionVersion deleted = orchestrator.deleteVersion(customerId, decisionName, version);
        assertThat(deleted, is(notNullValue()));
        assertThat(deleted, equalTo(decisionVersion));

        verify(client).delete(decisionVersion);
    }

    @Test
    public void rollbackToVersion() {
        String customerId = "foo";
        String decisionName = "bar";
        long version = 2l;

        DecisionVersion decisionVersion = mock(DecisionVersion.class);
        Decision decision = mock(Decision.class);
        when(decisionVersion.getDecision()).thenReturn(decision);

        when(decisionManager.setCurrentVersion(customerId, decisionName, version)).thenReturn(decisionVersion);
        when(selector.selectControlPlaneForDeployment(decision)).thenReturn(clusterControlPlane);
        when(clientFactory.createClientFor(clusterControlPlane)).thenReturn(client);

        DecisionVersion rollback = orchestrator.setCurrentVersion(customerId, decisionName, version);
        assertThat(rollback, is(notNullValue()));
        assertThat(rollback, equalTo(decisionVersion));

        verify(client).deploy(decisionVersion);
    }
}
