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
package org.apache.geronimo.remoting.jmx;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.proxy.ProxyContainer;
import org.apache.geronimo.proxy.ReflexiveInterceptor;
import org.apache.geronimo.remoting.DeMarshalingInterceptor;
import org.apache.geronimo.remoting.router.JMXTarget;

/**
 * @version $Revision: 1.5 $ $Date: 2003/12/30 21:19:22 $
 */
public class MBeanServerStub
        implements GeronimoMBeanTarget, JMXTarget {

    private ProxyContainer serverContainer;
    private DeMarshalingInterceptor demarshaller;
    private GeronimoMBeanContext geronimoMBeanContext;

    public void doStart() {

        // Setup the server side contianer..
        Interceptor firstInterceptor = new ReflexiveInterceptor(geronimoMBeanContext.getServer());
        firstInterceptor = new DeMarshalingInterceptor(firstInterceptor, getClass().getClassLoader());
        serverContainer = new ProxyContainer(firstInterceptor);
    }

    public void doStop() {
        serverContainer = null;
        demarshaller = null;
    }

    /**
     * @see org.apache.geronimo.remoting.router.JMXTarget#getRemotingEndpointInterceptor()
     */
    public Interceptor getRemotingEndpointInterceptor() {
        return demarshaller;
    }

    /**
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#setMBeanContext(org.apache.geronimo.kernel.service.GeronimoMBeanContext)
     */
    public void setMBeanContext(GeronimoMBeanContext geronimoMBeanContext) {
        this.geronimoMBeanContext = geronimoMBeanContext;
    }

    /**
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#canStart()
     */
    public boolean canStart() {
        return true;
    }

    /**
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#canStop()
     */
    public boolean canStop() {
        return true;
    }

    /**
     * @see org.apache.geronimo.kernel.service.GeronimoMBeanTarget#doFail()
     */
    public void doFail() {
    }
}
