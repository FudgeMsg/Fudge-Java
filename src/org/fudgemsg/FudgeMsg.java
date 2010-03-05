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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.types.ByteArrayFieldType;
import org.fudgemsg.types.PrimitiveFieldTypes;


/**
 * <p>A container for {@link FudgeMsgField}s.
 * This instance will contain all data fully extracted from a Fudge-encoded
 * stream, unlike other systems where fields are unpacked as required.
 * Therefore, constructing a {@code FudgeMsg} from a field is relatively more
 * expensive in CPU and memory usage than just holding the original byte array,
 * but lookups are substantially faster.</p>
 * 
 * <p>Instead of constructing an instance of this directly, a preferred approach is
 * to request a MutableFudgeFieldContainer from the main context, or a
 * serialisation context as that may return an implementation more appropriate
 * to the underlying or target stream.</p>
 *
 * @author Kirk Wylie
 */
public class FudgeMsg extends FudgeMsgBase implements MutableFudgeFieldContainer {

  /**
   * {@inheritDoc} 
   */
  protected FudgeMsg(FudgeContext fudgeContext) {
    super (fudgeContext);
  }
  
  /**
   * {@inheritDoc}
   */ 
  protected FudgeMsg (final FudgeFieldContainer fields, final FudgeContext fudgeContext) {
    super (fields, fudgeContext);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void add(FudgeField field) {
    if(field == null) {
      throw new NullPointerException("Cannot add an empty field");
    }
    getFields ().add(new FudgeMsgField(field));
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
    if(type == null) {
      throw new IllegalArgumentException("Cannot determine a Fudge type for value " + value + " of type " + value.getClass());
    }
    add(name, ordinal, type, value);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void add(String name, Integer ordinal, FudgeFieldType<?> type, Object value) {
    if(getFields ().size() >= Short.MAX_VALUE) {
      throw new IllegalStateException("Can only add " + Short.MAX_VALUE + " to a single message.");
    }
    if((ordinal != null) && ((ordinal > Short.MAX_VALUE) || (ordinal < Short.MIN_VALUE))) {
      throw new IllegalArgumentException("Ordinal must be within signed 16-bit range.");
    }
    if(type == null) {
      throw new NullPointerException("Cannot add a field without a type specified.");
    }
    
    // Adjust integral values to the lowest possible representation.
    switch(type.getTypeId()) {
    case FudgeTypeDictionary.SHORT_TYPE_ID:
    case FudgeTypeDictionary.INT_TYPE_ID:
    case FudgeTypeDictionary.LONG_TYPE_ID:
      long valueAsLong = ((Number)value).longValue();
      if((valueAsLong >= Byte.MIN_VALUE) && (valueAsLong <= Byte.MAX_VALUE)) {
        value = new Byte((byte)valueAsLong);
        type = PrimitiveFieldTypes.BYTE_TYPE;
      } else if((valueAsLong >= Short.MIN_VALUE) && (valueAsLong <= Short.MAX_VALUE)) {
        value = new Short((short)valueAsLong);
        type = PrimitiveFieldTypes.SHORT_TYPE;
      } else if((valueAsLong >= Integer.MIN_VALUE) && (valueAsLong <= Integer.MAX_VALUE)) {
        value = new Integer((int)valueAsLong);
        type = PrimitiveFieldTypes.INT_TYPE;
      }
      break;
    }
    
    Short ordinalAsShort = null;
    if(ordinal != null) {
      ordinalAsShort = ordinal.shortValue();
    }
    FudgeMsgField field = new FudgeMsgField(type, value, name, ordinalAsShort);
    getFields ().add(field);
  }
  
  /**
   * Resolves an arbitrary Java object to an underlying Fudge type (if possible).
   * 
   * @param value the object to resolve
   * @return the {@link FudgeFieldType} or {@code null} if no intrinsic type (or registered secondary type) is available
   */
  protected FudgeFieldType<?> determineTypeFromValue(Object value) {
    if(value == null) {
      throw new NullPointerException("Cannot determine type for null value.");
    }
    if(value instanceof byte[]) {
      return ByteArrayFieldType.getBestMatch((byte[])value);
    }
    FudgeFieldType<?> type = getFudgeContext().getTypeDictionary().getByJavaType(value.getClass());
    if((type == null) && (value instanceof UnknownFudgeFieldValue)) {
      UnknownFudgeFieldValue unknownValue = (UnknownFudgeFieldValue) value;
      type = unknownValue.getType();
    }
    return type;
  }
  
  /**
   * Resolves any field ordinals to field names from the given taxonomy.
   * 
   * @param taxonomy the taxonomy to use
   */
  public void setNamesFromTaxonomy(FudgeTaxonomy taxonomy) {
    if(taxonomy == null) {
      return;
    }
    for(int i = 0; i < getFields ().size(); i++) {
      FudgeMsgField field = getFields ().get(i);
      if((field.getOrdinal() != null) && (field.getName() == null)) {
        String nameFromTaxonomy = taxonomy.getFieldName(field.getOrdinal());
        if(nameFromTaxonomy != null) {
          field = new FudgeMsgField(field.getType(), field.getValue(), nameFromTaxonomy, field.getOrdinal());
          getFields ().set(i, field);
        }
      }
      if(field.getValue() instanceof FudgeMsg) {
        FudgeMsg subMsg = (FudgeMsg) field.getValue();
        subMsg.setNamesFromTaxonomy(taxonomy);
      } else if (field.getValue () instanceof FudgeFieldContainer) {
        FudgeMsg subMsg = new FudgeMsg ((FudgeFieldContainer)field.getValue (), getFudgeContext ());
        subMsg.setNamesFromTaxonomy (taxonomy);
        field = new FudgeMsgField (field.getType (), subMsg, field.getName (), field.getOrdinal ());
        getFields ().set (i, field);
      }
    }
  }

}
