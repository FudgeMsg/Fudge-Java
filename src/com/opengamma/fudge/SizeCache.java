/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.fudge.taxon.FudgeTaxonomy;

/**
 * Stores the sizes maintained by {@link FudgeMsg} and {@link FudgeMsgField}
 * keyed by whether there's a taxonomy provided or not.
 *
 * @author kirk
 */
/*package*/ class SizeCache {
  private final Map<FudgeTaxonomy, Integer> _sizesByTaxonomy = new ConcurrentHashMap<FudgeTaxonomy, Integer>();
  private volatile int _noTaxonomySize = -1; 
  private final SizeComputable _sizeComputable;
  
  public SizeCache(SizeComputable sizeComputable) {
    assert sizeComputable != null;
    _sizeComputable = sizeComputable;
  }
  
  public int getSize(FudgeTaxonomy taxonomy) {
    if(taxonomy == null) {
      if(_noTaxonomySize == -1) {
        _noTaxonomySize = _sizeComputable.computeSize(null);
      }
      return _noTaxonomySize;
    }
    Integer result = _sizesByTaxonomy.get(taxonomy);
    if(result == null) {
      result = _sizeComputable.computeSize(taxonomy);
      _sizesByTaxonomy.put(taxonomy, result);
    }
    assert result != null;
    return result;
  }

}
