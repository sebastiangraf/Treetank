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

package org.treetank.service.xml.diff;

import java.io.IOException;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.Holder;
import org.treetank.NodeModuleFactory;
import org.treetank.TestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.exception.TTException;
import org.treetank.service.xml.diff.DiffFactory.EDiffOptimized;

import com.google.inject.Inject;

/**
 * FullDiff test.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = NodeModuleFactory.class)
public class FullDiffTest {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    private IDiffObserver mObserver;

    @BeforeMethod
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
        Properties props = TestHelper.createProperties();
        mResource = mResourceConfig.create(props, 10);
        holder = Holder.generateWtx(mResource);
        mObserver = DiffTestHelper.createMock();
    }

    @AfterMethod
    public void tearDown() throws TTException {
        TestHelper.closeEverything();
        TestHelper.deleteEverything();
    }

    @Test
    public void testFullDiffFirst() throws TTException, InterruptedException {
        DiffTestHelper.setUpFirst(holder);
        DiffTestHelper.check(holder, mObserver, EDiffOptimized.NO);
        DiffTestHelper.verifyDiffFirst(mObserver);
    }

    @Test
    public void testOptimizedFirst() throws InterruptedException, TTException {
        DiffTestHelper.setUpFirst(holder);
        DiffTestHelper.check(holder, mObserver, EDiffOptimized.NO);
        DiffTestHelper.verifyDiffFirst(mObserver);
    }

    @Test
    public void testFullDiffSecond() throws TTException, InterruptedException, IOException,
        XMLStreamException {
        DiffTestHelper.setUpSecond(holder);
        DiffTestHelper.check(holder, mObserver, EDiffOptimized.NO);
        DiffTestHelper.verifyDiffSecond(mObserver);
    }

    @Test
    public void testFullDiffThird() throws TTException, IOException, XMLStreamException, InterruptedException {
        DiffTestHelper.setUpThird(holder);
        DiffTestHelper.check(holder, mObserver, EDiffOptimized.NO);
        DiffTestHelper.verifyDiffThird(mObserver);
    }

    @Test
    public void testFullDiffFourth() throws Exception {
        DiffTestHelper.setUpFourth(holder);
        DiffTestHelper.check(holder, mObserver, EDiffOptimized.NO);
        DiffTestHelper.verifyDiffFourth(mObserver);
    }

    @Test
    public void testFullDiffFifth() throws Exception {
        DiffTestHelper.setUpFifth(holder);
        DiffTestHelper.check(holder, mObserver, EDiffOptimized.NO);
        DiffTestHelper.verifyDiffFifth(mObserver);
    }

    @Test
    public void testFullDiffSixth() throws Exception {
        DiffTestHelper.setUpSixth(holder);
        DiffTestHelper.check(holder, mObserver, EDiffOptimized.NO);
        DiffTestHelper.verifyDiffSixth(mObserver);
    }
}
