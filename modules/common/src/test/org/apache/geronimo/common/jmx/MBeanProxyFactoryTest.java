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

package org.apache.geronimo.common.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * Unit test for {@link MBeanProxyFactory} class.
 *
 * @version $Revision: 1.4 $ $Date: 2003/09/01 15:15:03 $
 */
public class MBeanProxyFactoryTest
    extends TestCase
{
    protected MBeanServer server;
    protected ObjectName target;
    protected MockObject targetObject;
    
    protected void setUp() throws Exception
    {
        server = MBeanServerFactory.createMBeanServer("geronimo.test");
        
        target = new ObjectName("geronimo.test:bean=test");
        targetObject = new MockObject();
        server.registerMBean(targetObject, target);
    }
    
    protected void tearDown() throws Exception
    {
        MBeanServerFactory.releaseMBeanServer(server);
        server = null;
    }
    
    public void testCreate() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);
    }
    
    public void testGetString() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);
        
        String value = bean.getString();
        assertEquals(targetObject.getString(), value);
    }
    
    public void testSetString() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);
        
        String value = "newvalue";
        bean.setString(value);
        assertEquals(value, targetObject.getString());
    }
    
    public void testOperation_Simple() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);
        
        String value = bean.doIt();
        assertEquals(targetObject.doIt(), value);
    }
    
    public void testOperation_PoorlyNamed() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);
        
        String value = bean.setPoorlyNameOperation();
        assertEquals(targetObject.setPoorlyNameOperation(), value);
    }
    
    public void testOperation_SameNameDiffArgs() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);
        
        String value = bean.someOperation();
        assertEquals(targetObject.someOperation(), value);
        
        value = bean.someOperation("foo");
        assertEquals(targetObject.someOperation("foo"), value);
        
        value = bean.someOperation(true);
        assertEquals(targetObject.someOperation(true), value);
    }
    
    public void testMBeanProxyContext() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);
        
        assertTrue(bean instanceof MBeanProxyContext);
        
        MBeanProxyContext ctx = (MBeanProxyContext)bean;
        assertNotNull(ctx.getObjectName());
        assertEquals(target, ctx.getObjectName());
        assertNotNull(ctx.getMBeanServer());
    }
    
    //
    // Mock MBean
    //
    
    public static interface MockObjectMBean
    {
        String getString();
        
        boolean isSomething();
        
        void setString(String value);
        
        String doIt();
        
        String setPoorlyNameOperation();
        
        String someOperation();
        
        String someOperation(Object arg);
        
        String someOperation(boolean arg);
    }
    
    public static class MockObject
        implements MockObjectMBean
    {
        String string = "MyString";
        boolean something;
        
        public void setString(String value)
        {
            this.string = value;
        }
        
        public String getString()
        {
            return string;
        }
        
        public void setSomething(boolean flag)
        {
            something = flag;
        }
        
        public boolean isSomething()
        {
            return something;
        }
        
        public String doIt()
        {
            return "done";
        }
        
        public String setPoorlyNameOperation()
        {
            return "bad";
        }
        
        public String someOperation()
        {
            return "someop";
        }
        
        public String someOperation(Object arg)
        {
            return "someop" + arg;
        }
        
        public String someOperation(boolean arg)
        {
            return "somebooleanop" + arg;
        }
    }
}
