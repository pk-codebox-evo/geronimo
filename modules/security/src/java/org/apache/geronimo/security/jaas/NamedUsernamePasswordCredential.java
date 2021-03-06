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
package org.apache.geronimo.security.jaas;

/**
 * @version $Rev$ $Date$
 */
public class NamedUsernamePasswordCredential extends UsernamePasswordCredential{

    private final String name;

    public NamedUsernamePasswordCredential(String username, char[] password, String name) {
        super(username, password);
        this.name = name;
        if (name == null) {
            throw new IllegalStateException("Must supply a name");
        }
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object o) {
        if (!(o instanceof NamedUsernamePasswordCredential)) {
            return false;
        }
        return super.equals(o) && name.equals(((NamedUsernamePasswordCredential)o).name);
    }

    public int hashCode() {
        return name.hashCode() * 37 ^ super.hashCode();
    }

}
