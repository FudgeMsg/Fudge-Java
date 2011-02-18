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

import java.util.Iterator;

import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.types.ByteArrayFieldType;
import org.fudgemsg.types.IndicatorFieldType;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.types.PrimitiveFieldTypes;
import org.fudgemsg.types.SecondaryFieldType;

/**
 * A mutable message in the Fudge system.
 * <p>
 * The message consists of a list of {@link FudgeMsgField Fudge fields}.
 * This class holds the entire message in memory.
 * <p>
 * Applications are recommended to store and manipulate a {@link FudgeFieldContainer}
 * instance or a {@link MutableFudgeFieldContainer} rather than this class
 * for future flexibility.
 * <p>
 * This class is mutable and not thread-safe.
 */
public class FudgeMsg extends FudgeMsgBase implements MutableFudgeFieldContainer {

  /**
   * Constructor taking a Fudge context.
   * 
   * @param fudgeContext the {@code FudgeContext} to use for type resolution and other services 
   */
  protected FudgeMsg(FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  /**
   * Constructor taking a set of fields and a Fudge context.
   * <p>
   * The fields from the container are copied into this message, creating a new
   * field for each supplied field.
   * 
   * @param fields  the initial set of fields, not null
   * @param fudgeContext  the context to use for type resolution and other services, not null
   */
  protected FudgeMsg(final FudgeFieldContainer fields, final FudgeContext fudgeContext) {
    super(fields, fudgeContext);
  }

  //-------------------------------------------------------------------------
  /**
   * {@inheritDoc}
   */
  @Override
  public void add(FudgeField field) {
    if (field == null) {
      throw new NullPointerException("FudgeField must not be null");
    }
    getFields().add(FudgeMsgField.of(field));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(String name, Object value) {
    add(name, null, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(Integer ordinal, Object value) {
    add(null, ordinal, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(String name, Integer ordinal, Object value) {
    FudgeFieldType<?> type = determineTypeFromValue(value);
    if (type == null) {
      throw new IllegalArgumentException("Cannot determine a Fudge type for value " + value + " of type " + value.getClass());
    } else if (type == IndicatorFieldType.INSTANCE) {
      add(name, ordinal, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
    } else {
      add(name, ordinal, type, value);
    }
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public void add(String name, Integer ordinal, FudgeFieldType<?> type, Object value) {
    if (type == null) {
      throw new NullPointerException("FudgeFieldType must not be null");
    }
    if (getFields().size() >= Short.MAX_VALUE) {
      throw new IllegalStateException("Can only add " + Short.MAX_VALUE + " to a single message");
    }
    if (ordinal != null && (ordinal > Short.MAX_VALUE || ordinal < Short.MIN_VALUE)) {
      throw new IllegalArgumentException("Ordinal must be within signed 16-bit range.");
    }
    
    // adjust integral values to the lowest possible representation
    switch (type.getTypeId()) {
      case FudgeTypeDictionary.SHORT_TYPE_ID:
      case FudgeTypeDictionary.INT_TYPE_ID:
      case FudgeTypeDictionary.LONG_TYPE_ID:
        if (type instanceof SecondaryFieldType<?, ?>) {
          value = ((SecondaryFieldType<Object, ?>) type).secondaryToPrimary(value);
          type = ((SecondaryFieldType<?, ?>) type).getPrimaryType();
        }
        long valueAsLong = ((Number) value).longValue();
        if (valueAsLong >= Byte.MIN_VALUE && valueAsLong <= Byte.MAX_VALUE) {
          value = new Byte((byte) valueAsLong);
          type = PrimitiveFieldTypes.BYTE_TYPE;
        } else if (valueAsLong >= Short.MIN_VALUE && valueAsLong <= Short.MAX_VALUE) {
          value = new Short((short) valueAsLong);
          type = PrimitiveFieldTypes.SHORT_TYPE;
        } else if (valueAsLong >= Integer.MIN_VALUE && valueAsLong <= Integer.MAX_VALUE) {
          value = new Integer((int) valueAsLong);
          type = PrimitiveFieldTypes.INT_TYPE;
        }
        break;
    }
    
    Short ordinalAsShort = null;
    if (ordinal != null) {
      ordinalAsShort = ordinal.shortValue();
    }
    FudgeMsgField field = FudgeMsgField.of(type, value, name, ordinalAsShort);
    getFields().add(field);
  }

  /**
   * Resolves an arbitrary Java object to an underlying Fudge type (if possible).
   * 
   * @param value  the object to resolve, null returns the indicator type
   * @return the field type, null if no intrinsic type (or registered secondary type) is available
   */
  protected FudgeFieldType<?> determineTypeFromValue(Object value) {
    if (value == null) {
      return IndicatorFieldType.INSTANCE;
    }
    if (value instanceof byte[]) {
      return ByteArrayFieldType.getBestMatch((byte[]) value);
    }
    FudgeFieldType<?> type = getFudgeContext().getTypeDictionary().getByJavaType(value.getClass());
    if (type == null && value instanceof UnknownFudgeFieldValue) {
      UnknownFudgeFieldValue unknownValue = (UnknownFudgeFieldValue) value;
      type = unknownValue.getType();
    }
    return type;
  }

  /**
   * Resolves any field ordinals to field names from the given taxonomy.
   * 
   * @param taxonomy  the taxonomy to use, null ignored
   */
  public void setNamesFromTaxonomy(FudgeTaxonomy taxonomy) {
    if (taxonomy == null) {
      return;
    }
    for (int i = 0; i < getFields().size(); i++) {
      FudgeField field = getFields().get(i);
      if ((field.getOrdinal() != null) && (field.getName() == null)) {
        String nameFromTaxonomy = taxonomy.getFieldName(field.getOrdinal());
        if (nameFromTaxonomy != null) {
          field = FudgeMsgField.of(field.getType(), field.getValue(), nameFromTaxonomy, field.getOrdinal());
          getFields().set(i, field);
        }
      }
      if (field.getValue() instanceof FudgeMsg) {
        FudgeMsg subMsg = (FudgeMsg) field.getValue();
        subMsg.setNamesFromTaxonomy(taxonomy);
      } else if (field.getValue() instanceof FudgeFieldContainer) {
        FudgeMsg subMsg = new FudgeMsg((FudgeFieldContainer) field.getValue(), getFudgeContext());
        subMsg.setNamesFromTaxonomy(taxonomy);
        field = FudgeMsgField.of(field.getType(), subMsg, field.getName(), field.getOrdinal());
        getFields().set(i, field);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<FudgeField> iterator() {
    // return the real iterator since this is a mutable message
    return getFields().iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(Short ordinal) {
    final Iterator<FudgeField> i = iterator();
    while (i.hasNext()) {
      final FudgeField field = i.next();
      if (fieldOrdinalEquals(ordinal, field))
        i.remove();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String name) {
    final Iterator<FudgeField> i = iterator();
    while (i.hasNext()) {
      final FudgeField field = i.next();
      if (fieldNameEquals(name, field))
        i.remove();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(String name, Short ordinal) {
    final Iterator<FudgeField> i = iterator();
    while (i.hasNext()) {
      final FudgeField field = i.next();
      if (fieldOrdinalEquals(ordinal, field) && fieldNameEquals(name, field))
        i.remove();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    getFields().clear();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    return obj instanceof FudgeMsg && super.equals(obj);
  }

}
