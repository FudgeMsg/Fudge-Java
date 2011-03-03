/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and other contributors.
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
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * An immutable taxonomy implementation based on a bidirectional map.
 * <p>
 * This is the standard implementation.
 */
public class MapFudgeTaxonomy implements FudgeTaxonomy {

  /**
   * An empty taxonomy.
   */
  public static final FudgeTaxonomy EMPTY = new MapFudgeTaxonomy();

  /**
   * The map keyed by ordinal.
   */
  private final Map<Integer, String> _ordinalToNameMap;
  /**
   * The map keyed by name.
   */
  private final Map<String, Integer> _nameToOrdinalMap;

  /**
   * Creates a new empty taxonomy.
   */
  private MapFudgeTaxonomy() {
    _ordinalToNameMap = Collections.emptyMap();
    _nameToOrdinalMap = Collections.emptyMap();
  }

  /**
   * Creates a new taxonomy initialized by the supplied map.
   * 
   * @param ordinalToNameMap  the map of ordinal to field names, not null, no nulls
   */
  public MapFudgeTaxonomy(Map<Integer, String> ordinalToNameMap) {
    if (ordinalToNameMap == null) {
      ordinalToNameMap = Collections.emptyMap();
    }
    _ordinalToNameMap = new HashMap<Integer, String>(ordinalToNameMap);
    _nameToOrdinalMap = new HashMap<String, Integer>(ordinalToNameMap.size());
    for (Map.Entry<Integer, String> entry : ordinalToNameMap.entrySet()) {
      if (entry.getKey() == null || entry.getValue() == null) {
        throw new NullPointerException("Map must not contain null");
      }
      _nameToOrdinalMap.put(entry.getValue(), entry.getKey());
    }
    if (_nameToOrdinalMap.size() != _ordinalToNameMap.size()) {
      throw new IllegalArgumentException("Map must not contain duplicate name");
    }
  }

  /**
   * Creates a new taxonomy initialized by a list of ordinals and corresponding names.
   * The ordinal and name arrays must be the same length.
   * 
   * @param ordinals  the array of ordinal values, not null
   * @param names  the array of field names, not null, no nulls
   */
  public MapFudgeTaxonomy(int[] ordinals, String[] names) {
    if (ordinals == null) {
      throw new NullPointerException("Ordinal array must not be null");
    }
    if (names == null) {
      throw new NullPointerException("Name array must not be null");
    }
    if (ordinals.length != names.length) {
      throw new IllegalArgumentException("Ordinal and Name array must be same length");
    }
    _ordinalToNameMap = new HashMap<Integer, String>(ordinals.length);
    _nameToOrdinalMap = new HashMap<String, Integer>(ordinals.length);
    for (int i = 0; i < ordinals.length; i++) {
      if (names[i] == null) {
        throw new NullPointerException("Name array must not contain null");
      }
      _ordinalToNameMap.put(ordinals[i], names[i]);
      _nameToOrdinalMap.put(names[i], ordinals[i]);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String getFieldName(short ordinal) {
    return _ordinalToNameMap.get((int) ordinal);
  }

  @Override
  public Short getFieldOrdinal(String fieldName) {
    Integer ordinal = _nameToOrdinalMap.get(fieldName);
    if (ordinal == null) {
      return null;
    }
    return ordinal.shortValue();
  }

  //-------------------------------------------------------------------------
  /**
   * Encodes the taxonomy as a Fudge message as per the specification.
   * <p>
   * An encoded taxonomy can be decoded back to a taxonomy object by the
   * MapFudgeTaxonomy.fromFudgeMsg method on this class or equivalent function
   * in any other language implementation.
   * 
   * @param context  the message context, not null
   * @return the created message, not null
   */
  public MutableFudgeFieldContainer toFudgeMsg(final FudgeMessageFactory context) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    for (Map.Entry<Integer, String> entry : _ordinalToNameMap.entrySet()) {
      msg.add(entry.getKey(), entry.getValue());
    }
    return msg;
  }

  /**
   * Decodes a taxonomy from a Fudge message as per the specification.
   * <p>
   *  that is backed by a MapFudgeTaxonomy object.
   * 
   * @param msg  the message to decode, not null
   * @return the encoded taxonomy, not null
   */
  public static FudgeTaxonomy fromFudgeMsg(final FudgeFieldContainer msg) {
    final List<FudgeField> fields = msg.getAllFields();
    final Map<Integer, String> ordinalToNameMap = new HashMap<Integer, String>(fields.size());
    int i = 0;
    for (FudgeField field : fields) {
      final Short ordinal = field.getOrdinal();
      if (ordinal == null) {
        throw new IllegalArgumentException("Fudge message does not contain a FudgeTaxonomy - field at index " + i + " has no ordinal");
      }
      final Object value = field.getValue();
      if (!(value instanceof String)) {
        throw new IllegalArgumentException("Fudge message does not contain a FudgeTaxonomy - field at index " + i + " (ordinal " + ordinal + ") does not contain a string");
      }
      if (ordinalToNameMap.put(ordinal.intValue(), (String) value) != null) {
        throw new IllegalArgumentException("Fudge message does not contain a FudgeTaxonomy - field at index " + i + " redefines ordinal " + ordinal);
      }
      i++;
    }
    return new MapFudgeTaxonomy(ordinalToNameMap);
  }

}
