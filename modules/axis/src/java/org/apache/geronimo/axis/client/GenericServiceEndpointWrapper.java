/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.axis.client;

import java.util.Iterator;
import java.io.Serializable;
import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;

import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.client.Call;
import org.apache.axis.NoEndPointException;

/**
 * this gets enhanced to be the proxy.
 * @version $Rev$ $Date$
 */
public class GenericServiceEndpointWrapper implements Stub, Serializable {
    private final GenericServiceEndpoint genericServiceEndpoint;

    public GenericServiceEndpointWrapper(GenericServiceEndpoint genericServiceEndpoint) {
        this.genericServiceEndpoint = genericServiceEndpoint;
    }

    public void _setProperty(String s, Object o) {
        genericServiceEndpoint._setProperty(s, o);
    }

    public Object _getProperty(String s) {
        return genericServiceEndpoint._getProperty(s);
    }

    public Object removeProperty(String s) {
        return genericServiceEndpoint.removeProperty(s);
    }

    public Iterator _getPropertyNames() {
        return genericServiceEndpoint._getPropertyNames();
    }

    public void setUsername(String s) {
        genericServiceEndpoint.setUsername(s);
    }

    public String getUsername() {
        return genericServiceEndpoint.getUsername();
    }

    public void setPassword(String s) {
        genericServiceEndpoint.setPassword(s);
    }

    public String getPassword() {
        return genericServiceEndpoint.getPassword();
    }

    public int getTimeout() {
        return genericServiceEndpoint.getTimeout();
    }

    public void setTimeout(int i) {
        genericServiceEndpoint.setTimeout(i);
    }

    public QName getPortName() {
        return genericServiceEndpoint.getPortName();
    }

    public void setPortName(QName qName) {
        genericServiceEndpoint.setPortName(qName);
    }

    public void setPortName(String s) {
        genericServiceEndpoint.setPortName(s);
    }

    public void setMaintainSession(boolean b) {
        genericServiceEndpoint.setMaintainSession(b);
    }

    public void setHeader(String s, String s1, Object o) {
        genericServiceEndpoint.setHeader(s, s1, o);
    }

    public void setHeader(SOAPHeaderElement soapHeaderElement) {
        genericServiceEndpoint.setHeader(soapHeaderElement);
    }

    public void extractAttachments(Call call) {
        genericServiceEndpoint.extractAttachments(call);
    }

    public void addAttachment(Object o) {
        genericServiceEndpoint.addAttachment(o);
    }

    public SOAPHeaderElement getHeader(String s, String s1) {
        return genericServiceEndpoint.getHeader(s, s1);
    }

    public SOAPHeaderElement getResponseHeader(String s, String s1) {
        return genericServiceEndpoint.getResponseHeader(s, s1);
    }

    public SOAPHeaderElement[] getHeaders() {
        return genericServiceEndpoint.getHeaders();
    }

    public SOAPHeaderElement[] getResponseHeaders() {
        return genericServiceEndpoint.getResponseHeaders();
    }

    public Object[] getAttachments() {
        return genericServiceEndpoint.getAttachments();
    }

    public void clearHeaders() {
        genericServiceEndpoint.clearHeaders();
    }

    public void clearAttachments() {
        genericServiceEndpoint.clearAttachments();
    }

    void checkCachedEndpoint() throws NoEndPointException {
        genericServiceEndpoint.checkCachedEndpoint();
    }
}
