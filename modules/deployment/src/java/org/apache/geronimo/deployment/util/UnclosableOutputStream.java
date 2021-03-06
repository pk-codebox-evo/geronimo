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

package org.apache.geronimo.deployment.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @version $Rev$ $Date$
 */
public class UnclosableOutputStream extends OutputStream {

    private final OutputStream delegate;

    public UnclosableOutputStream(OutputStream delegate) {
        this.delegate = delegate;
    }

    public void close() {
    }

    public void write(int b) throws IOException {
        delegate.write(b);
    }

    public void write(byte b[]) throws IOException {
        delegate.write(b);
    }

    public void write(byte b[], int off, int len) throws IOException {
        delegate.write(b, off, len);
    }

    public void flush() throws IOException {
        delegate.flush();
    }
}