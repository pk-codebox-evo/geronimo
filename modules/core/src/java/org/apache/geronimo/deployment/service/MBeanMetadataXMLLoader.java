/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.deployment.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.DeploymentException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/11 17:59:11 $
 */
public class MBeanMetadataXMLLoader {
    public MBeanMetadata loadXML(Element element) throws DeploymentException {
        MBeanMetadata md = new MBeanMetadata();
        md.setCode(element.getAttribute("code").trim());
        String s = element.getAttribute("name").trim();
        try {
            if (s.length() > 0) {
                md.setName(new ObjectName(s));
            }
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Invalid ObjectName" + s, e);
        }

        NodeList nl = element.getElementsByTagName("constructor");
        Element consElement = (Element) nl.item(0);
        if (consElement != null) {
            List types = md.getConstructorTypes();
            List args = md.getConstructorArgs();
            nl = consElement.getElementsByTagName("arg");
            for (int i = 0; i < nl.getLength(); i++) {
                Element argElement = (Element) nl.item(i);
                String type = argElement.getAttribute("type");
                types.add(type);
                args.add(getValue(argElement));
            }
        }

        nl = element.getElementsByTagName("attribute");
        Map attrs = md.getAttributeValues();
        for (int i = 0; i < nl.getLength(); i++) {
            Element argElement = (Element) nl.item(i);
            String name = argElement.getAttribute("name");
            attrs.put(name, getValue(argElement));
        }

        nl = element.getElementsByTagName("relationship");
        Set relationships = md.getRelationships();
        for (int i = 0; i < nl.getLength(); i++) {
            Element argElement = (Element) nl.item(i);
            String name = argElement.getAttribute("name");
            String type = argElement.getAttribute("type");
            String role = argElement.getAttribute("role");
            String target = argElement.getAttribute("target");
            MBeanRelationship relationship = new MBeanRelationship(name, type, role, target);
            relationships.add(relationship);
        }

        return md;
    }

    /**
     * Return the value of an argument node.
     * If the node contains an Element, then the Element is returned;
     * otherwise the content of the node (after stripping comments and trimming)
     * is returned, or null if it is empty or zero length
     * @param element the element to extract the value from
     * @return the value of the node (a String or Element)
     */
    private Object getValue(Element element) {
        NodeList nl = element.getChildNodes();
        if (nl.getLength() == 0) {
            return null;
        }

        StringBuffer content = new StringBuffer();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                return node;
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                content.append(node.getNodeValue());
                break;
            }
        }
        String s = content.toString().trim();
        return (s.length() > 0) ? s : null;
    }
}
