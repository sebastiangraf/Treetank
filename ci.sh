#!/bin/sh

###SCRIPT FOR CI

#getting disy data
hg pull
hg update
hg merge 2>/dev/null
#getting sf data
hg pull ssh://sebastiangraf@treetank.hg.sourceforge.net/hgroot/treetank/treetank
hg update
hg merge 2>/dev/null
hg commit -m "merged disy and sf data"

#building maven stuff
mvn clean deploy -U

# comitting changes
hg push
