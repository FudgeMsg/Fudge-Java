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

package org.fudgemsg.mapping;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.types.SecondaryFieldTypeBase;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * {@link FudgeBuilder} instance for encoding and decoding MongoDB objects.
 * 
 * @author Andrew Griffin
 */
/* package */ class MongoDBFudgeBuilder implements FudgeBuilder<DBObject> {
  
  /**
   * 
   */
  public static final FudgeBuilder<DBObject> INSTANCE = new MongoDBFudgeBuilder ();
  
  private MongoDBFudgeBuilder () {
  }
  
  private Object decodeObjectValue(FudgeSerializationContext context, Object value) {
    if(value instanceof DBObject) {
      DBObject dbObject = (DBObject) value;
      return buildMessage (context, dbObject);
    }
    return value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MutableFudgeFieldContainer buildMessage(
      FudgeSerializationContext context, DBObject dbObject) {
    if(dbObject == null) {
      return null;
    }
    MutableFudgeFieldContainer msg = context.newMessage ();
    for(String key : dbObject.keySet()) {
      Object value = dbObject.get(key);
      if(value instanceof List<?>) {
        for(Object element : (List<?>) value) {
          msg.add(key, decodeObjectValue(context, element));
        }
      } else {
        msg.add(key, decodeObjectValue(context, value));
      }
    }
    return msg;
  }
  
  @SuppressWarnings("unchecked")
  private Object encodePrimitiveFieldValue(final FudgeDeserializationContext context, Object fieldValue) {
    FudgeFieldType<?> valueType = context.getFudgeContext().getTypeDictionary().getByJavaType(fieldValue.getClass());
    if (valueType == null) {
      throw new IllegalArgumentException("Cannot handle serialization of object " + fieldValue + " of type " + fieldValue.getClass() + " as no Fudge type available in context");
    }
    
    switch (valueType.getTypeId()) {
    case FudgeTypeDictionary.INDICATOR_TYPE_ID:
      // REVIEW kirk 2010-08-20 -- Is this the right behavior here?
      return null;
    case FudgeTypeDictionary.BOOLEAN_TYPE_ID :
    case FudgeTypeDictionary.BYTE_ARR_128_TYPE_ID:
    case FudgeTypeDictionary.BYTE_ARR_16_TYPE_ID:
    case FudgeTypeDictionary.BYTE_ARR_20_TYPE_ID:
    case FudgeTypeDictionary.BYTE_ARR_256_TYPE_ID:
    case FudgeTypeDictionary.BYTE_ARR_32_TYPE_ID:
    case FudgeTypeDictionary.BYTE_ARR_4_TYPE_ID:
    case FudgeTypeDictionary.BYTE_ARR_512_TYPE_ID:
    case FudgeTypeDictionary.BYTE_ARR_64_TYPE_ID:
    case FudgeTypeDictionary.BYTE_ARR_8_TYPE_ID:
    case FudgeTypeDictionary.BYTE_ARRAY_TYPE_ID:
    case FudgeTypeDictionary.BYTE_TYPE_ID:
    case FudgeTypeDictionary.DOUBLE_ARRAY_TYPE_ID:
    case FudgeTypeDictionary.DOUBLE_TYPE_ID:
    case FudgeTypeDictionary.FLOAT_ARRAY_TYPE_ID:
    case FudgeTypeDictionary.FLOAT_TYPE_ID:
    case FudgeTypeDictionary.INT_ARRAY_TYPE_ID:
    case FudgeTypeDictionary.INT_TYPE_ID:
    case FudgeTypeDictionary.LONG_ARRAY_TYPE_ID:
    case FudgeTypeDictionary.LONG_TYPE_ID:
    case FudgeTypeDictionary.SHORT_ARRAY_TYPE_ID:
    case FudgeTypeDictionary.SHORT_TYPE_ID:
    case FudgeTypeDictionary.STRING_TYPE_ID:
      if (valueType instanceof SecondaryFieldTypeBase) {
        SecondaryFieldTypeBase secondaryType = (SecondaryFieldTypeBase) valueType;
        return secondaryType.secondaryToPrimary(fieldValue);
      }
      // Built-in support.
      return fieldValue;
    case FudgeTypeDictionary.DATE_TYPE_ID:
    case FudgeTypeDictionary.DATETIME_TYPE_ID:
    case FudgeTypeDictionary.TIME_TYPE_ID:
      // FIXME kirk 2010-08-20 -- This is an insanely gross hack around the rest of the
      // fix for FRJ-83 breaking all dates, exposed by FRJ-84.
      return fieldValue;
    }
    // If we get this far, it's a user-defined type. Nothing we can do here.
    throw new IllegalStateException("User-defined types must be handled before they get to MongoDBFudgeBuilder currently. Value type " + valueType);
  }

  private Object encodeFieldValue(final FudgeDeserializationContext context, final Object currentValue, Object fieldValue) {
    boolean structureExpected = false;
    if(fieldValue instanceof FudgeFieldContainer) {
      fieldValue = buildObject(context, (FudgeFieldContainer) fieldValue);
      structureExpected = true;
    }
    if(currentValue instanceof List<?>) {
      List<Object> l = new ArrayList<Object>((List<?>)(currentValue));
      l.add(fieldValue);
      return l;
    } else if (currentValue != null) {
      List<Object> l = new ArrayList<Object>();
      l.add(currentValue);
      if (!structureExpected) {
        fieldValue = encodePrimitiveFieldValue(context, fieldValue);
      }
      l.add(fieldValue);
      return l;
    }
    
    if (structureExpected) {
      return fieldValue;
    }
    
    return encodePrimitiveFieldValue(context, fieldValue);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public DBObject buildObject(FudgeDeserializationContext context,
      FudgeFieldContainer fields) {
    if(fields == null) {
      return null;
    }
    BasicDBObject dbObject = new BasicDBObject();
    
    for(FudgeField field : fields.getAllFields()) {
      if(field.getName() == null) {
        if (field.getOrdinal() == 0) {
          continue;
        }
        // REVIEW kirk 2009-10-22 -- Should this be configurable so that it just
        // silently drops unnamed fields?
        throw new IllegalArgumentException("Field encountered without a name (" + field + ")");
      }
      Object value = field.getValue();
      value = encodeFieldValue(context, dbObject.get(field.getName()), value);
      dbObject.put(field.getName(), value);
    }
    
    return dbObject;
  }
  
}