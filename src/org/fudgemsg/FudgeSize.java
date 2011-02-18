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
package org.fudgemsg;

import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * Utility for calculating the size of a Fudge message.
 * <p>
 * These utilities calculate the size of a stream without constructing a full message.
 * <p>
 * This class is a static utility with no shared state.
 */
public class FudgeSize {

  /**
   * Calculates the size of a field (field header and value payload) in the Fudge stream in bytes.
   * <p>
   * The calculation takes account of the value being reduced to fit in a smaller space.
   *
   * @param <T> the underlying Java type of the field data
   * @param taxonomy  the taxonomy in use, null if no taxonomy
   * @param ordinal  the field ordinal, null if no ordinal
   * @param name  the field name, null if no name
   * @param type  the Fudge field type, not null
   * @param value  the field value
   * @return the number of bytes
   */
  public static <T> int calculateFieldSize(final FudgeTaxonomy taxonomy, final Short ordinal, final String name, final FudgeFieldType<T> type, final T value) {
    int size = 0;
    // field prefix
    size += 2;
    boolean hasOrdinal = ordinal != null;
    boolean hasName = name != null;
    if (name != null && taxonomy != null) {
      if (taxonomy.getFieldOrdinal(name) != null) {
        hasOrdinal = true;
        hasName = false;
      }
    }
    if (hasOrdinal) {
      size += 2;
    }
    if (hasName) {
      // one for the size prefix
      size++;
      // then for the UTF Encoding
      size += UTF8.getLengthBytes(name);
    }
    if (type.isVariableSize()) {
      int valueSize = type.getVariableSize(value, taxonomy);
      if (valueSize <= 255) {
        size += valueSize + 1;
      } else if (valueSize <= Short.MAX_VALUE) {
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
   * <p>
   * The calculation takes account of the value being reduced to fit in a smaller space.
   * 
   * @param <T> the underlying Java type of the field data
   * @param taxonomy  the taxonomy in use, null if no taxonomy
   * @param field  the field to calculate a size for, not null
   * @return the number of bytes
   */
  @SuppressWarnings("unchecked")
  public static <T> int calculateFieldSize(final FudgeTaxonomy taxonomy, final FudgeField field) {
    return calculateFieldSize(taxonomy, field.getOrdinal(), field.getName(), (FudgeFieldType<T>) field.getType(), (T) field.getValue());
  }

  /**
   * Calculates the size of a field (field header and value payload) in the Fudge stream in bytes when no taxonomy is used.
   * <p>
   * The calculation takes account of the value being reduced to fit in a smaller space.
   * 
   * @param <T> the underlying Java type of the field data
   * @param field  the field to calculate a size for, not null
   * @return the number of bytes
   */
  @SuppressWarnings("unchecked")
  public static <T> int calculateFieldSize(final FudgeField field) {
    return calculateFieldSize(null, field.getOrdinal(), field.getName(), (FudgeFieldType<T>) field.getType(), (T) field.getValue());
  }

  /**
   * Calculates the size of a field (field header and value payload) in the Fudge stream in bytes when no taxonomy is used.
   * <p>
   * The calculation takes account of the value being reduced to fit in a smaller space.
   * 
   * @param <T> the underlying Java type of the field data
   * @param ordinal  the field ordinal, null if no ordinal
   * @param name  the field name, null if no name
   * @param type  the Fudge field type, not null
   * @param value  the field value
   * @return the number of bytes
   */
  public static <T> int calculateFieldSize(final Short ordinal, final String name, final FudgeFieldType<T> type, final T value) {
    return calculateFieldSize(null, ordinal, name, type, value);
  }

  /**
   * Calculates the size of a field (field header and value payload) in the Fudge stream in bytes when no taxonomy is used.
   * <p>
   * The calculation takes account of the value being reduced to fit in a smaller space.
   * 
   * @param <T> the underlying Java type of the field data
   * @param ordinal  the field ordinal, null if no ordinal
   * @param type  the Fudge field type, not null
   * @param value  the field value
   * @return the number of bytes
   */
  public static <T> int calculateFieldSize(final Short ordinal, final FudgeFieldType<T> type, final T value) {
    return calculateFieldSize(null, ordinal, null, type, value);
  }

  /**
   * Calculates the size of a field (field header and value payload) in the Fudge stream in bytes when no taxonomy is used.
   * <p>
   * The calculation takes account of the value being reduced to fit in a smaller space.
   * 
   * @param <T> the underlying Java type of the field data
   * @param name  the field name, null if no name
   * @param type  the Fudge field type, not null
   * @param value  the field value
   * @return the number of bytes
   */
  public static <T> int calculateFieldSize(final String name, final FudgeFieldType<T> type, final T value) {
    return calculateFieldSize(null, null, name, type, value);
  }

  /**
   * Calculates the size of a field (field header and value payload) in the Fudge stream in bytes when no taxonomy is used.
   * <p>
   * The calculation takes account of the value being reduced to fit in a smaller space.
   * 
   * @param <T> the underlying Java type of the field data
   * @param type  the Fudge field type, not null
   * @param value  the field value
   * @return the number of bytes
   */
  public static <T> int calculateFieldSize(final FudgeFieldType<T> type, final T value) {
    return calculateFieldSize(null, null, null, type, value);
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the size of a message as the sum of the fields.
   *
   * @param taxonomy  the taxonomy in use, null if no taxonomy
   * @param fields  the fields to calculate a size for, not null
   * @return the number of bytes
   */
  public static int calculateMessageSize(final FudgeTaxonomy taxonomy, final FudgeFieldContainer fields) {
    int bytes = 0;
    for (FudgeField field : fields) {
      bytes += calculateFieldSize(taxonomy, field);
    }
    return bytes;
  }

  /**
   * Calculates the size of a message as the sum of the fields when no taxonomy is used.
   * 
   * @param fields  the fields to calculate a size for, not null
   * @return the number of bytes
   */
  public static int calculateMessageSize(final FudgeFieldContainer fields) {
    return calculateMessageSize(null, fields);
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the size of a message including the envelope header.
   * 
   * @param taxonomy  the taxonomy in use, null if no taxonomy
   * @param fields  the fields to calculate a size for, not null
   * @return the number of bytes
   */
  public static int calculateMessageEnvelopeSize(final FudgeTaxonomy taxonomy, final FudgeFieldContainer fields) {
    return 8 + calculateMessageSize(taxonomy, fields);
  }

  /**
   * Calculates the size of a message including the envelope header when no taxonomy is used.
   * 
   * @param fields  the fields to calculate a size for, not null
   * @return the number of bytes
   */
  public static int calculateMessageEnvelopeSize(final FudgeFieldContainer fields) {
    return 8 + calculateMessageSize(null, fields);
  }

  /**
   * Calculates the size of a message including the envelope header.
   * 
   * @param taxonomy  the taxonomy in use, null if no taxonomy
   * @param envelope  the message envelope to calculate a size for, not null
   * @return the number of bytes
   */
  public static int calculateMessageEnvelopeSize(final FudgeTaxonomy taxonomy, final FudgeMsgEnvelope envelope) {
    return 8 + calculateMessageSize(taxonomy, envelope.getMessage());
  }

  /**
   * Calculates the size of a message including the envelope header when no taxonomy is used.
   * 
   * @param envelope  the message envelope to calculate a size for, not null
   * @return the number of bytes
   */
  public static int calculateMessageEnvelopeSize(final FudgeMsgEnvelope envelope) {
    return 8 + calculateMessageSize(null, envelope.getMessage());
  }

}
