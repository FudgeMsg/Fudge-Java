/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.taxon;

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
      _namesByOrdinal.put(ordinals[i], names[i]);
      _ordinalsByName.put(names[i], ordinals[i]);
    }
  }

  @Override
  public String getFieldName(short ordinal) {
    return _namesByOrdinal.get(ordinal);
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
