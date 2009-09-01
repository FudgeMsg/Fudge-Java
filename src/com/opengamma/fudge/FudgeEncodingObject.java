/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.fudge.taxon.FudgeTaxonomy;

/**
 * The base type for all objects which can be encoded using Fudge. 
 *
 * @author kirk
 */
/*package*/ abstract class FudgeEncodingObject {
  private Map<FudgeTaxonomy, Integer> _sizesByTaxonomy;
  private volatile int _noTaxonomySize = -1; 

  public final int getSize(FudgeTaxonomy taxonomy) {
    if(taxonomy == null) {
      if(_noTaxonomySize == -1) {
        _noTaxonomySize = computeSize(null);
      }
      return _noTaxonomySize;
    }
    synchronized(this) {
      if(_sizesByTaxonomy == null) {
        _sizesByTaxonomy = new HashMap<FudgeTaxonomy, Integer>();
      }
      Integer result = _sizesByTaxonomy.get(taxonomy);
      if(result == null) {
        result = computeSize(taxonomy);
        _sizesByTaxonomy.put(taxonomy, result);
      }
      assert result != null;
      return result;
    }
  }
  
  abstract int computeSize(FudgeTaxonomy taxonomy);


}
