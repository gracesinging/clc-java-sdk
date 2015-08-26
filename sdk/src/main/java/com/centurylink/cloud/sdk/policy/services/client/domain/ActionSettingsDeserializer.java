/*
 * (c) 2015 CenturyLink. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.centurylink.cloud.sdk.policy.services.client.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aliaksandr Krasitski
 */
public class ActionSettingsDeserializer  extends JsonDeserializer<ActionSettingsMetadata> {

    private static final String RECIPIENTS = "recipients";

    @Override
    public ActionSettingsMetadata deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        TreeNode node = jsonParser.getCodec().readTree(jsonParser);

        //email settings
        if (node.get(RECIPIENTS).isArray()) {
            ArrayNode recipientsNode = ((ArrayNode)node.get(RECIPIENTS));
            List<String> recipients = new ArrayList<>(recipientsNode.size());

            recipientsNode.forEach(recipient -> recipients.add(recipient.asText()));

            return new ActionSettingsEmailMetadata().recipients(recipients);
        }
        return null;
    }
}
