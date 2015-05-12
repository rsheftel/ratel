package com.malbec.tsdb.markit;

import tsdb.*;

public interface CdsData {

    AttributeValue cdsTicker();

    AttributeValue docClause();

    AttributeValue ccy();

    AttributeValue tier();

    AttributeValue ticker();

}