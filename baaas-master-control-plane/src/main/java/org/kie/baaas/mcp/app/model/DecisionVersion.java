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

package org.kie.baaas.mcp.app.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

import org.kie.baaas.mcp.app.model.eventing.KafkaTopics;

/**
 * Encapsulates a version of a Decision.
 */
@NamedQueries({
        @NamedQuery(name = "DecisionVersion.countByCustomerAndName", query = "from DecisionVersion dv where dv.decision.customerId=:customerId and dv.decision.name=:name"),
        @NamedQuery(name = "DecisionVersion.byCustomerAndNameAndVersion", query = "from DecisionVersion dv where dv.decision.customerId=:customerId and dv.decision.name=:name and dv.version=:version")
})
@Entity
@Table(name = "DECISION_VERSION")
public class DecisionVersion {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    private Decision decision;

    @Basic
    @Column(updatable = false, nullable = false, name = "dmn_location")
    private String dmnLocation;

    @Basic
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DecisionVersionStatus status;

    @Basic
    @Column(name = "status_message")
    private String statusMessage;

    @Column(updatable = false, nullable = false)
    private long version;

    @Column(name = "submitted_at", updatable = false, nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime submittedAt;

    @Column(name = "published_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime publishedAt;

    @Version
    @Column(name = "lock_version", nullable = false)
    private int lockVersion = 0;

    @Basic
    @Column
    private String url;

    @Basic
    @Column(name = "dmn_md5", updatable = false)
    private String dmnMd5;

    @ElementCollection
    @CollectionTable(
            name = "DECISION_VERSION_TAG",
            joinColumns = @JoinColumn(name = "decision_version_id")
    )
    @MapKeyColumn(name = "name")
    @Column(name = "value", updatable = false, nullable = false)
    private Map<String, String> tags = new HashMap<>();

    @ElementCollection
    @CollectionTable(
            name = "DECISION_VERSION_CONFIG",
            joinColumns = @JoinColumn(name = "decision_version_id")
    )
    @MapKeyColumn(name = "name")
    @Column(name = "value", nullable = false, updatable = false)
    private Map<String, String> configuration = new HashMap<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "sourceTopic", column = @Column(name = "kafka_source_topic", updatable = false)),
            @AttributeOverride(name = "sinkTopic", column = @Column(name = "kafka_sink_topic", updatable = false))
    })
    private KafkaTopics kafkaTopics;

    public KafkaTopics getKafkaTopics() {
        return kafkaTopics;
    }

    public void setKafkaTopics(KafkaTopics kafkaTopics) {
        this.kafkaTopics = kafkaTopics;
    }

    public String getId() {
        return id;
    }

    public Decision getDecision() {
        return decision;
    }

    public String getDmnLocation() {
        return dmnLocation;
    }

    public DecisionVersionStatus getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public long getVersion() {
        return version;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public String getUrl() {
        return url;
    }

    public String getDmnMd5() {
        return dmnMd5;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public void setDmnLocation(String dmnLocation) {
        this.dmnLocation = dmnLocation;
    }

    public void setStatus(DecisionVersionStatus status) {
        this.status = status;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDmnMd5(String dmnMd5) {
        this.dmnMd5 = dmnMd5;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DecisionVersion that = (DecisionVersion) o;
        return version == that.version && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }
}
