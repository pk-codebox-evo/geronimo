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
package org.apache.geronimo.validator.ejb;

import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import javax.enterprise.deploy.shared.ModuleType;
import org.apache.geronimo.validator.AbstractValidator;
import org.apache.geronimo.validator.Validator;
import org.apache.geronimo.xml.deployment.LoaderUtil;
import org.apache.geronimo.xml.deployment.EjbJarLoader;
import org.apache.geronimo.deployment.model.ejb.EjbJarDocument;
import org.apache.geronimo.deployment.model.DeploymentDescriptor;
import org.w3c.dom.Document;

/**
 * The validator class for validating an EJB JAR.  Right now just does enough
 * to prove that this whole thing works.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/02 17:04:20 $
 */
public class EjbValidator extends AbstractValidator {
    public Class[] getTestClasses() {
        return new Class[]{
            SessionBeanTests.class,
        };
    }

    /**
     * To try me, pass an EJB JAR file name as the only argument.
     */
    public static void main(String[] args) {
        try {
            ClassLoader loader = new URLClassLoader(new URL[]{new File(args[0]).toURL()});
            InputStream in = loader.getResourceAsStream("META-INF/ejb-jar.xml");
            Document doc = LoaderUtil.parseXML(new BufferedReader(new InputStreamReader(in)), "ejb-jar.xml");
            EjbJarDocument jar = EjbJarLoader.load(doc);
            Validator v =new EjbValidator();
            v.initialize(new PrintWriter(new OutputStreamWriter(System.out), true), args[0], loader, ModuleType.EJB, new DeploymentDescriptor[]{jar}, null);
            System.out.println("Validation Result: "+v.validate());
        } catch(java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
