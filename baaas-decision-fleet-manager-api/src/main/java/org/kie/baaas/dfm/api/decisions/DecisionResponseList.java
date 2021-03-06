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

package org.kie.baaas.dfm.api.decisions;

import java.util.ArrayList;
import java.util.List;

import org.kie.baaas.dfm.api.ResponseList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
public class DecisionResponseList extends ResponseList {

    @JsonProperty("kind")
    private String kind = "DecisionList";

    @JsonProperty("items")
    private List<DecisionResponse> items = new ArrayList<>();

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public List<DecisionResponse> getItems() {
        return items;
    }

    public void setItems(List<DecisionResponse> items) {
        this.items = items;
    }
}
