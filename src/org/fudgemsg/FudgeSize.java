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

import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * Collects all of the size calculations for Fudge stream elements together
 * to simplify size calculations without constructing a full FudgeMsg object.
 * 
 * @author Andrew Griffin
 */
public class FudgeSize {
  
  /**
   * Returns the size of a field (field header and value payload) in the Fudge stream in bytes.
   *
   * @param <T> underlying Java type of the field data
   * @param taxonomy the encoding taxonomy, or {@code null} for none
   * @param ordinal the ordinal, or {@code null} if none
   * @param name the name, or {@code null} if none
   * @param type the field type
   * @param value the field value
   * @return number of bytes
   */
  public static <T> int calculateFieldSize (final FudgeTaxonomy taxonomy, final Short ordinal, final String name, final FudgeFieldType<T> type, final T value) {
    int size = 0;
    // Field prefix
    size += 2;
    boolean hasOrdinal = ordinal != null;
    boolean hasName = name != null;
    if((name != null) && (taxonomy != null)) {
      if(taxonomy.getFieldOrdinal(name) != null) {
        hasOrdinal = true;
        hasName = false;
      }
    }
    if(hasOrdinal) {
      size += 2;
    }
    if(hasName) {
      // One for the size prefix
      size++;
      // Then for the UTF Encoding
      size += UTF8.getLengthBytes(name);
    }
    if(type.isVariableSize()) {
      int valueSize = type.getVariableSize (value, taxonomy);
      if(valueSize <= 255) {
        size += valueSize + 1;
      } else if(valueSize <= Short.MAX_VALUE) {
        size += valueSize + 2;
      } else {
        size += valueSize + 4;
      }
    } else {
      size += type.getFixedSize();
    }
    return size;
  }
  
  /**
   * Calculates the size of a field (field header and value payload) in the Fudge stream in bytes.
   * 
   * @param <T> underlying Java type of the field data
   * @param taxonomy the encoding taxonomy, or {@code null} for none
   * @param field the field to check
   * @return number of bytes
   */
  @SuppressWarnings("unchecked")
  public static <T> int calculateFieldSize (final FudgeTaxonomy taxonomy, final FudgeField field) {
    return calculateFieldSize (taxonomy, field.getOrdinal (), field.getName (), (FudgeFieldType<T>)field.getType (), (T)field.getValue ());
  }
  
  /**
   * Calculates the size of a field (field header and value payload) in the Fudge stream in bytes when no taxonomy is used.
   * 
   * @param <T> underlying Java type of the field data
   * @param field the field to check
   * @return number of bytes
   */
  @SuppressWarnings("unchecked")
  public static <T> int calculateFieldSize (final FudgeField field) {
    return calculateFieldSize (null, field.getOrdinal (), field.getName (), (FudgeFieldType<T>)field.getType (), (T)field.getValue ());
  }
  
  /**
   * Calculates the size of a field (field header and value payload) in the Fudge stream in bytes when no taxonomy is used.
   * 
   * @param <T> underlying Java type of the field data
   * @param ordinal ordinal index of the field (or {@code null} for none)
   * @param name field name {or {@code null} for none)
   * @param type {@link FudgeFieldType} of the field
   * @param value field value (so size can be calculated for non-fixed width or reducible types)
   * @return number of bytes
   */
  public static <T> int calculateFieldSize (final Short ordinal, final String name, final FudgeFieldType<T> type, final T value) {
    return calculateFieldSize (null, ordinal, name, type, value);
  }
  
  /**
   * Calculates the size of a field (field header and value payload) in the Fudge stream in bytes when no taxonomy is used.
   * 
   * @param <T> underlying Java type of the field data
   * @param ordinal ordinal index of the field (or {@code null} for none)
   * @param type {@link FudgeFieldType} of the field
   * @param value field value (so size can be calculated for non-fixed width or reducible types)
   * @return number of bytes
   */
  public static <T> int calculateFieldSize (final Short ordinal, final FudgeFieldType<T> type, final T value) {
    return calculateFieldSize (null, ordinal, null, type, value);
  }
  
  /**
   * Calculates the size of a field (field header and value payload) in the Fudge stream in bytes when no taxonomy is used.
   * 
   * @param <T> underlying Java type of the field data
   * @param name field name (or {@code null} for none)
   * @param type {@link FudgeFieldType} of the field
   * @param value field value (so size can be calculated for non-fixed width or reducible types)
   * @return number of bytes
   */
  public static <T> int calculateFieldSize (final String name, final FudgeFieldType<T> type, final T value) {
    return calculateFieldSize (null, null, name, type, value);
  }
  
  /**
   * Calculates the size of a field (field header and value payload) in the Fudge stream in bytes when no taxonomy is used.
   * 
   * @param <T> underlying Java type of the field data
   * @param type {@link FudgeFieldType} of the field
   * @param value field value (so size can be calculated for non-fixed width or reducible types)
   * @return number of bytes
   */
  public static <T> int calculateFieldSize (final FudgeFieldType<T> type, final T value) {
    return calculateFieldSize (null, null, null, type, value);
  }
  
  /**
   * Calculates the size of a message as the sum of the fields.
   *
   * @param taxon the taxonomy to use, or {@code null} for no taxonomy
   * @param fields the message data
   * @return number of bytes
   */
  public static int calculateMessageSize (final FudgeTaxonomy taxon, final FudgeFieldContainer fields) {
    int bytes = 0;
    for (FudgeField field : fields) {
      bytes += calculateFieldSize (taxon, field);
    }
    return bytes;
  }
  
  /**
   * Calculates the size of a message as the sum of the fields when no taxonomy is used.
   * 
   * @param fields the message data
   * @return number of bytes
   */
  public static int calculateMessageSize (final FudgeFieldContainer fields) {
    return calculateMessageSize (null, fields);
  }
  
  /**
   * Calculates the size of a message including the envelope header.
   * 
   * @param taxon the {@link FudgeTaxonomy} to use, or {@code null} for none
   * @param fields message contents
   * @return number of bytes
   */
  public static int calculateMessageEnvelopeSize (final FudgeTaxonomy taxon, final FudgeFieldContainer fields) {
    return 8 + calculateMessageSize (taxon, fields);
  }
  
  /**
   * Calculates the size of a message including the envelope header when no taxonomy is used.
   * 
   * @param fields message contents
   * @return number of bytes
   */
  public static int calculateMessageEnvelopeSize (final FudgeFieldContainer fields) {
    return 8 + calculateMessageSize (null, fields);
  }
  
  /**
   * Calculates the size of a message including the envelope header.
   * 
   * @param taxon the {@link FudgeTaxonomy} to use, or {@code null} for none
   * @param envelope message envelope
   * @return number of bytes
   */
  public static int calculateMessageEnvelopeSize (final FudgeTaxonomy taxon, final FudgeMsgEnvelope envelope) {
    return 8 + calculateMessageSize (taxon, envelope.getMessage ());
  }
  
  /**
   * Calculates the size of a message including the envelope header when no taxonomy is used.
   * 
   * @param envelope message envelope
   * @return number of bytes
   */
  public static int calculateMessageEnvelopeSize (final FudgeMsgEnvelope envelope) {
    return 8 + calculateMessageSize (null, envelope.getMessage ());
  }
  
}