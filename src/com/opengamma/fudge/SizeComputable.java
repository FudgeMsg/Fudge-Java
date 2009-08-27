/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import com.opengamma.fudge.taxon.FudgeTaxonomy;

/**
 * The primary interface through which {@link FudgeMsgField} and {@link FudgeMsg}
 * can contain a {@link SizeCache}.
 *
 * @author kirk
 */
/*package*/ interface SizeComputable {

  int computeSize(FudgeTaxonomy taxonomy);
}
