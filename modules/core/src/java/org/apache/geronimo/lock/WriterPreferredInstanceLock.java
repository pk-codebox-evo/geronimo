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
package org.apache.geronimo.lock;


/**
 * Implementation of InstanceLock using a simple prefer-writer allocation
 * policy.
 * <ul>
 * <li>Contexts requesting exclusive locks will be scheduled before those requesting shared locks</li>
 * <li>A request for an exclusive lock causes requests for shared locks to block
 *     until all exclusive locks have been released.</li>
 * <li>When an exclusive lock is released, any other requests for exclusive locks
 *     will be scheduled before any requests for shared locks</li>
 * </ul>
 * If the workload makes many exclusive requests, this policy may result in
 * starvation of shared requests.
 *
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/11 17:59:12 $
 */
public class WriterPreferredInstanceLock implements InstanceLock {
    private Object exclActive;
    private int sharedCount = 0;
    private int exclWaiting = 0;
    private int sharedWaiting = 0;
    private final Object exclLock = new Object();
    private final Object sharedLock = new Object();

    public void sharedLock(Object context) throws InterruptedException {
        assert (context != null);
        synchronized (sharedLock) {
            synchronized (this) {
                // we can get the lock immediately if no-one has or is waiting
                // for an exclusive lock
                if (exclActive == null && exclWaiting == 0) {
                    sharedCount++;
                    return;
                }

                // we will have to wait...
                sharedWaiting++;
            }
            while (true) {
                try {
                    sharedLock.wait();
                    synchronized (this) {
                        // can we get the lock now?
                        if (exclActive == null && exclWaiting == 0) {
                            sharedWaiting--;
                            sharedCount++;
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                    // we were interrupted whilst waiting for the lock
                    // no-one else really cares
                    synchronized (this) {
                        sharedWaiting--;
                    }
                    throw e;
                }
            }
        }
    }

    public void exclusiveLock(Object context) throws InterruptedException {
        assert (context != null);
        synchronized (exclLock) {
            synchronized (this) {
                // we can get the lock immediately if no-one has it and
                // no-one has a shared lock
                if (exclActive == null && sharedCount == 0) {
                    exclActive = context;
                    return;
                }

                // we will have to wait...
                exclWaiting++;
            }
            while (true) {
                try {
                    exclLock.wait();
                    synchronized (this) {
                        // can we get the lock now?
                        if (exclActive == null && sharedCount == 0) {
                            exclWaiting--;
                            exclActive = context;
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                    // we were interrupted whilst waiting for the lock
                    synchronized (this) {
                        exclWaiting--;

                        // was the fact that we were waiting stopping anyone
                        // else from getting it?
                        if (exclWaiting > 0) {
                            // trade-off - context switch vs. length of hold
                            // we choose to notify everyone on the assumption
                            // the first holder will release before the
                            // second gets scheduled
                            exclLock.notify();
                        } else if (sharedWaiting > 0) {
                            sharedLock.notifyAll();
                        }
                    }
                    throw e;
                }
            }
        }
    }

    public void release(Object context) {
        assert (context != null);
        synchronized (exclLock) {
            synchronized (sharedLock) {
                synchronized (this) {
                    if (exclActive == context) {
                        exclActive = null;

                        // is anyone waiting for me to release?
                        // give priority to those requesting exclusive access
                        if (exclWaiting > 0) {
                            exclLock.notify();
                        } else if (sharedWaiting > 0) {
                            sharedLock.notifyAll();
                        }
                    } else {
                        sharedCount--;
                        if (sharedCount == 0 && exclWaiting > 0) {
                            // I just released the last shared lock and someone is
                            // waiting for an exclusive, to its time to notify them
                            // again wake everyone - see above
                            exclLock.notify();
                        }
                    }
                }
            }
        }
    }

    public synchronized int getSharedCount() {
        return sharedCount;
    }

    public synchronized int getSharedWaiting() {
        return sharedWaiting;
    }
}
