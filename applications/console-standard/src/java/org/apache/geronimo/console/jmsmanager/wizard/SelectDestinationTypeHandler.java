/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.jmsmanager.wizard;

import java.io.IOException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * Handler for the screen that creates a new connection factory
 *
 * @version $Rev: 368994 $ $Date: 2006-01-14 02:07:18 -0500 (Sat, 14 Jan 2006) $
 */
public class SelectDestinationTypeHandler extends AbstractHandler {
    public SelectDestinationTypeHandler() {
        super(SELECT_DESTINATION_TYPE_MODE, "/WEB-INF/view/jmswizard/destinationType.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, JMSResourceData data) throws PortletException, IOException {
        data.createIfNecessary();
        JMSProviderData provider = JMSProviderData.getProviderData(data.getRarURI(), request);
        // If there's only one factory type, skip this step
        if(provider.getAdminObjectDefinitions().length == 1) {
            data.setDestinationType(0);
            return ADD_DESTINATION_MODE+BEFORE_ACTION;
        }
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, JMSResourceData data) throws PortletException, IOException {
        JMSProviderData provider = JMSProviderData.getProviderData(data.getRarURI(), request);
        request.setAttribute("provider", provider);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, JMSResourceData data) throws PortletException, IOException {
        return ADD_DESTINATION_MODE+BEFORE_ACTION;
    }
}
