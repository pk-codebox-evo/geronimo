/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.connector.outbound.connectionmanagerconfig;

import org.apache.geronimo.connector.outbound.ConnectionInterceptor;
import org.apache.geronimo.connector.outbound.LocalXAResourceInsertionInterceptor;
import org.apache.geronimo.connector.outbound.TransactionCachingInterceptor;
import org.apache.geronimo.connector.outbound.TransactionEnlistingInterceptor;
import org.apache.geronimo.transaction.context.TransactionContextManager;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class LocalTransactions extends TransactionSupport {
    public static final TransactionSupport INSTANCE = new LocalTransactions();

    private LocalTransactions() {
    }

    public ConnectionInterceptor addXAResourceInsertionInterceptor(ConnectionInterceptor stack, String name) {
        return new LocalXAResourceInsertionInterceptor(stack, name);
    }

    public ConnectionInterceptor addTransactionInterceptors(ConnectionInterceptor stack, TransactionContextManager transactionContextManager) {
        stack = new TransactionEnlistingInterceptor(stack, transactionContextManager);
        return new TransactionCachingInterceptor(stack, transactionContextManager);
    }
}
