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

package org.kie.baaas.mcp.app.controller.modelmappers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.baaas.mcp.api.decisions.DecisionResponse;
import org.kie.baaas.mcp.api.decisions.DecisionResponseList;
import org.kie.baaas.mcp.app.model.Decision;
import org.kie.baaas.mcp.app.model.DecisionVersion;
import org.kie.baaas.mcp.app.model.DecisionVersionStatus;
import org.kie.baaas.mcp.app.model.eventing.KafkaTopics;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DecisionMapperTest {

    private static final String DECISION_VERSION_HREF = "http://decision.version.redhat.com";

    private static final String DECISION_VERSION_DMN_HREF = "http://decision.version.dmn.redhat.com";

    @Mock
    private HrefGenerator hrefGenerator;

    @InjectMocks
    private DecisionMapper decisionMapper;

    @BeforeEach
    public void before() {
        when(hrefGenerator.generateDecisionHref(any(DecisionVersion.class))).thenReturn(DECISION_VERSION_HREF);
        when(hrefGenerator.generateDecisionDMNHref(any(DecisionVersion.class))).thenReturn(DECISION_VERSION_DMN_HREF);
    }

    private Decision createDecision(String name) {
        Decision decision = new Decision();
        decision.setName(name);
        decision.setDescription("my-description");
        decision.setCustomerId("1");
        return decision;
    }

    private DecisionVersion createDecisionVersion(Decision decision) {
        DecisionVersion decisionVersion = new DecisionVersion();
        decisionVersion.setDecision(decision);
        decisionVersion.setVersion(1l);
        decisionVersion.setStatus(DecisionVersionStatus.BUILDING);
        decisionVersion.setStatusMessage("my status message");
        decisionVersion.setSubmittedAt(LocalDateTime.now());
        decisionVersion.setPublishedAt(LocalDateTime.now());
        decisionVersion.setDmnLocation("s3://dmn-location");
        decisionVersion.setDmnMd5("md5");

        Map<String, String> config = new HashMap<>();
        config.put("config", "value");
        decisionVersion.setConfiguration(config);

        Map<String, String> tags = new HashMap<>();
        tags.put("tag", "tagValue");
        decisionVersion.setTags(tags);
        return decisionVersion;
    }

    private void assertDecisionResponse(DecisionResponse response, Decision decision, DecisionVersion decisionVersion, boolean hasKafka) {
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatus(), equalTo(decisionVersion.getStatus().name()));
        assertThat(response.getStatusMessage(), equalTo(decisionVersion.getStatusMessage()));
        assertThat(response.getPublishedAt(), equalTo(decisionVersion.getPublishedAt()));
        assertThat(response.getSubmittedAt(), equalTo(decisionVersion.getSubmittedAt()));
        assertThat(response.getVersion(), equalTo(decisionVersion.getVersion()));
        assertThat(response.getUrl(), equalTo(decisionVersion.getUrl()));
        assertThat(response.getResponseModel().getHref(), equalTo(DECISION_VERSION_DMN_HREF));
        assertThat(response.getResponseModel().getMd5(), equalTo(decisionVersion.getDmnMd5()));
        assertThat(response.getHref(), equalTo(DECISION_VERSION_HREF));
        assertThat(response.getConfiguration().containsKey("config"), is(true));
        assertThat(response.getTags().containsKey("tag"), is(true));
        assertThat(response.getDescription(), equalTo(decision.getDescription()));
        assertThat(response.getName(), equalTo(decision.getName()));
        if (!hasKafka) {
            assertThat(response.getEventing(), is(nullValue()));
        }
    }

    @Test
    public void mapVersionToDecisionResponse() {
        Decision decision = createDecision("my-first-decision");
        DecisionVersion decisionVersion = createDecisionVersion(decision);

        DecisionResponse response = decisionMapper.mapVersionToDecisionResponse(decisionVersion);
        assertDecisionResponse(response, decision, decisionVersion, false);
    }

    @Test
    public void mapVersionToDecisionResponse_withKafkaTopics() {

        Decision decision = createDecision("my-first-decision");
        DecisionVersion decisionVersion = createDecisionVersion(decision);

        KafkaTopics topics = new KafkaTopics();
        topics.setSinkTopic("sink-topic");
        topics.setSourceTopic("source-topic");
        decisionVersion.setKafkaTopics(topics);

        DecisionResponse response = decisionMapper.mapVersionToDecisionResponse(decisionVersion);
        assertDecisionResponse(response, decision, decisionVersion, true);

        assertThat(response.getEventing().getKafka().getSink(), equalTo(topics.getSinkTopic()));
        assertThat(response.getEventing().getKafka().getSource(), equalTo(topics.getSourceTopic()));
    }

    @Test
    public void mapVersionsToDecisionResponseList() {

        Decision decision = createDecision("my-first-decision");
        DecisionVersion version = createDecisionVersion(decision);
        DecisionVersion version2 = createDecisionVersion(decision);
        version2.setVersion(2l);

        DecisionResponseList responseList = decisionMapper.mapVersionsToDecisionResponseList(asList(version, version2));
        assertThat(responseList, is(notNullValue()));
        assertThat(responseList.getItems(), hasSize(2));

        assertDecisionResponse(responseList.getItems().get(0), decision, version, false);
        assertDecisionResponse(responseList.getItems().get(1), decision, version2, false);
    }

    @Test
    public void mapToDecisionResponse() {
        Decision decision = createDecision("my-first-decision");
        DecisionVersion decisionVersion = createDecisionVersion(decision);
        decision.setCurrentVersion(decisionVersion);

        DecisionResponse response = decisionMapper.mapToDecisionResponse(decision);
        assertDecisionResponse(response, decision, decisionVersion, false);
    }

    @Test
    public void mapToDecisionResponseList() {

        Decision decision = createDecision("my-first-decision");
        DecisionVersion decisionVersion = createDecisionVersion(decision);
        decision.setCurrentVersion(decisionVersion);

        Decision decision2 = createDecision("my-second-decision");
        DecisionVersion decisionVersion2 = createDecisionVersion(decision2);
        decision2.setCurrentVersion(decisionVersion2);

        DecisionResponseList responseList = decisionMapper.mapToDecisionResponseList(asList(decision, decision2));
        assertThat(responseList, is(notNullValue()));
        assertThat(responseList.getItems(), hasSize(2));

        assertDecisionResponse(responseList.getItems().get(0), decision, decisionVersion, false);
        assertDecisionResponse(responseList.getItems().get(1), decision2, decisionVersion2, false);
    }
}
