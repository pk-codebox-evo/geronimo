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

package org.apache.geronimo.kernel.deployment.goal;

import org.apache.geronimo.kernel.deployment.GeronimoTargetModule;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

/**
 * Adds support for URL to the DeploymentGoal.
 * Can be used with or without normalizing of the file-protocl based URLs.
 */
abstract class URLDeploymentGoal extends DeploymentGoal {
    protected final URL url;

    /**
     * Initializes the URLDeploymentGoal.
     * File-protocol based URLs are normalized if the <code>normalize</code> parameter is set to <code>true</code>.
     * @param targetModule associated GeronimoTargetModule
     * @param url associated URL
     * @param normalize indicates if the provided URL must be normalized.
     * @throws IllegalArgumentException if urls are to be normalized and
     * <code>url</code> parameter is passed as <code>null</code>.
     */
    protected URLDeploymentGoal(GeronimoTargetModule targetModule, URL url, final boolean normalize) {
        super(targetModule);
        if (normalize) {
            if (url == null) {
                throw new IllegalArgumentException("URL is null");
            }
            this.url = normalizeURL(url);
        } else {
            this.url = url;
        }
    }

    /**
     * Gets the value of <code>url</code> attribute.
     * If the original url was file-protocol based a normalized form is returned.
     * @return the original or normalized url
     */
    public URL getUrl() {
        return url;
    }

    private static URL normalizeURL(URL url) {
        assert url != null;

        if (url.getProtocol().equals("file")) {
            String filename = url.getFile().replace('/', File.separatorChar);
            File file = new File(filename);
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException ignore) {
            }
        }

        return url;
    }
}
