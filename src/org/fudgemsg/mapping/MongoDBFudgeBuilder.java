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

package org.fudgemsg.mapping;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

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
    //context.addClassHeader (msg, dbObject.getClass ());
    return msg;
  }

  private Object encodeFieldValue(final FudgeDeserializationContext context, final Object currentValue, Object fieldValue) {
    if(fieldValue instanceof FudgeFieldContainer) {
      fieldValue = buildObject(context, (FudgeFieldContainer) fieldValue);
    }
    if(currentValue instanceof List<?>) {
      List<Object> l = new ArrayList<Object>((List<?>)currentValue);
      l.add(fieldValue);
      fieldValue = l;
    } else if (currentValue != null) {
      List<Object> l = new ArrayList<Object>();
      l.add(currentValue);
      l.add(fieldValue);
      fieldValue = l;
    }
    return fieldValue;
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
        // REVIEW kirk 2009-10-22 -- Should this be configurable so that it just
        // silently drops unnamed fields?
        throw new IllegalArgumentException("Field encountered without a name.");
      }
      Object value = field.getValue();
      value = encodeFieldValue(context, dbObject.get(field.getName()), value);
      dbObject.put(field.getName(), value);
    }
    
    return dbObject;
  }
  
}