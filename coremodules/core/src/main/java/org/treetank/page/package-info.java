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

/**
 * <h1>Page Layer for Treetank</h1>
 * <p>
 * Contains all page kinds and page utils. 
 * </p>
 * <p>
 * The pages are namely
 * <ul>
 * <li>UberPage: Representing the main-entrance point in the page-structure of the structure.</li>
 * <li>IndirectPage: Pointing to other pages to multiply the fanout within the tree-structure within the entire structure as well as within the sub-structures under each revision namely the RevisionRootPages.</li>
 * <li>RevisionRootPage: Representing a single version within the structure. Offers to the ability to point to any Indirect-/NodePage (even if they were created in former versions.)</li>
 * <li>MetaPage: Storing application-specific metadata in a map-structure. The entries must be provided by the application.</li>
 * <li>NodePage: Storing application-specific nodes in any structure. The node must be provided by the application.</li>
 * </ul>
 * </p>
 * @author Sebastian Graf, University of Konstanz
 * @author Marc Kramis, University of Konstanz
 * 
 */
package org.treetank.page;

