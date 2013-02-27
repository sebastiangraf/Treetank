/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import org.treetank.log.LogValue;
import org.treetank.page.NodePage;

/**
 * Differential versioning of {@link NodePage}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Differential implements IRevisioning {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePage combinePages(final NodePage[] pages) {
        // check to have only the newer version and the related fulldump to read on
        checkArgument(pages.length > 0, "At least one Nodepage must be provided");
        // create entire page..
        final NodePage returnVal = new NodePage(pages[0].getPageKey(), pages[0].getLastPagePointer());
        // ...and for all nodes...
        for (int i = 0; i < pages[0].getNodes().length; i++) {
            // ..check if node exists in newer version, and if not...
            if (pages[0].getNodes()[i] != null) {
                returnVal.setNode(i, pages[0].getNode(i));
            }// ...set the version from the last fulldump
            else if (pages.length > 1) {
                returnVal.setNode(i, pages[1].getNode(i));
            }
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogValue combinePagesForModification(int pRevisionsToRestore, long pNewPageKey, NodePage[] pages,
        boolean pFullDump) {
        // check to have only the newer version and the related fulldump to read on
        checkArgument(pages.length > 0, "At least one Nodepage must be provided");
        // create pages for container..
        final NodePage[] returnVal =
            {
                new NodePage(pages[0].getPageKey(), pages[0].getLastPagePointer()),
                new NodePage(pNewPageKey, pages[0].getPageKey())
            };

        // ...iterate through the nodes and check if it is stored..
        for (int j = 0; j < returnVal[0].getNodes().length; j++) {
            // ...check if the node was written within the last version, if so...
            if (pages[0].getNode(j) != null) {
                // ...set it in the read and write-version to be rewritten again...
                returnVal[0].setNode(j, pages[0].getNode(j));
                returnVal[1].setNode(j, pages[0].getNode(j));
            } else if (pages.length > 1) {
                // otherwise, just store then node from the fulldump to complete read-page except...
                returnVal[0].setNode(j, pages[1].getNode(j));
                // ..a fulldump becomes necessary.
                if (pFullDump) {
                    returnVal[1].setNode(j, pages[1].getNode(j));
                }
            }
        }
        // return the container
        return new LogValue(returnVal[0], returnVal[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

}
