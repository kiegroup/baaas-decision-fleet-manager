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

package org.kie.baaas.dfm.app.resolvers;

import java.security.Principal;

import javax.enterprise.context.ApplicationScoped;

/**
 * Resolves the current customer id given the thread of execution.
 */
@ApplicationScoped
public class CustomerIdResolver {

    /**
     * Returns the customerId for the current Thread of execution.
     *
     * @return - The current customer id
     */
    public String getCustomerId(Principal principal) {
        return principal.getName();
    }
}
