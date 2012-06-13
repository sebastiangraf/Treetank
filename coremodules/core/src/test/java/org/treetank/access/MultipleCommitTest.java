/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.access;

import org.junit.After;
import org.junit.Before;
import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.exception.AbsTTException;

public class MultipleCommitTest {

    private Holder holder;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
        holder = Holder.generateWtx();
    }

    @After
    public void tearDown() throws AbsTTException {
        holder.close();
        TestHelper.closeEverything();
    }

    // @Test
    // public void test() throws AbsTTException {
    // Assert.assertEquals(0L, holder.getSession().getMostRecentVersion());
    //
    // holder.getNWtx().commit();
    //
    // holder.getNWtx().insertElementAsFirstChild(new QName("foo"));
    // assertEquals(1L, holder.getSession().getMostRecentVersion());
    // holder.getNWtx().moveTo(1);
    // assertEquals(new QName("foo"), holder.getNWtx().getQNameOfCurrentNode());
    // holder.getNWtx().abort();
    //
    // assertEquals(1L, holder.getSession().getMostRecentVersion());
    // }
    //
    // @Test
    // public void testAutoCommit() throws AbsTTException {
    // DocumentCreater.create(holder.getNWtx());
    // holder.getNWtx().commit();
    //
    // final INodeReadTrx rtx = holder.getNRtx();
    // rtx.close();
    // }
    //
    // @Test
    // public void testRemove() throws AbsTTException {
    // DocumentCreater.create(holder.getNWtx());
    // holder.getNWtx().commit();
    // assertEquals(1L, holder.getSession().getMostRecentVersion());
    //
    // holder.getNWtx().moveTo(ROOT_NODE);
    // holder.getNWtx().moveTo(((IStructNode)holder.getNWtx().getNode()).getFirstChildKey());
    // holder.getNWtx().remove();
    // holder.getNWtx().commit();
    // assertEquals(2L, holder.getSession().getMostRecentVersion());
    // }

}