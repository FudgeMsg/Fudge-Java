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
package org.fudgemsg.taxon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link FudgeTaxonomy} where all lookups are specified
 * at construction time and held in a {@link Map}.
 * This is extremely useful in a case where the taxonomy is generated dynamically,
 * or as a building block for loading taxonomy definitions from persistent
 * storage.
 *
 * @author kirk
 */
public class MapFudgeTaxonomy implements FudgeTaxonomy {
  private final Map<Integer, String> _namesByOrdinal;
  private final Map<String, Integer> _ordinalsByName;
  
  public MapFudgeTaxonomy() {
    this(Collections.<Integer,String>emptyMap());
  }
  
  public MapFudgeTaxonomy(Map<Integer, String> namesByOrdinal) {
    if(namesByOrdinal == null) {
      namesByOrdinal = Collections.emptyMap();
    }
    _namesByOrdinal = new HashMap<Integer, String>(namesByOrdinal);
    _ordinalsByName = new HashMap<String, Integer>(namesByOrdinal.size());
    for(Map.Entry<Integer, String> entry : namesByOrdinal.entrySet()) {
      _ordinalsByName.put(entry.getValue(), entry.getKey());
    }
  }
  
  public MapFudgeTaxonomy(int[] ordinals, String[] names) {
    if(ordinals == null) {
      throw new NullPointerException("Must provide ordinals.");
    }
    if(names == null) {
      throw new NullPointerException("Must provide names.");
    }
    if(ordinals.length != names.length) {
      throw new IllegalArgumentException("Arrays of ordinals and names must be of same length.");
    }
    _namesByOrdinal = new HashMap<Integer, String>(ordinals.length);
    _ordinalsByName = new HashMap<String, Integer>(ordinals.length);
    for(int i = 0; i < ordinals.length; i++) {
      //AIWG: Should we check for null names and throw exceptions at this stage?
      _namesByOrdinal.put(ordinals[i], names[i]);
      _ordinalsByName.put(names[i], ordinals[i]);
    }
  }

  @Override
  public String getFieldName(short ordinal) {
    return _namesByOrdinal.get((int)ordinal);
  }

  @Override
  public Short getFieldOrdinal(String fieldName) {
    Integer ordinal = _ordinalsByName.get(fieldName);
    if(ordinal == null) {
      return null;
    }
    return ordinal.shortValue();
  }

}
