/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.rmi.iiop.compiler;

import java.lang.reflect.Method;


public class StubClass {
    private Class stubClass;
    private Method getInstanceMethod;

    public StubClass( Class stubClass, Method getInstanceMethod )
    {
        this.stubClass = stubClass;
        this.getInstanceMethod = getInstanceMethod;
    }

    public Class getStubClass()
    {
        return stubClass;
    }

    public Method getGetInstanceMethod()
    {
        return getInstanceMethod;
    }
}
