/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc. and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fudgemsg;

import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.taxon.FudgeTaxonomy;


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
