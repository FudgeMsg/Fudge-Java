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

package org.fudgemsg.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Converts instances of {@link FudgeFieldContainer} to MongoDB {@link DBObject}
 * instances.
 * This can be used to store a Fudge message in MongoDB.
 *
 * @author kirk
 */
public final class MongoDBEncoder {
  private MongoDBEncoder() {
  }
  
  public static DBObject encode(FudgeFieldContainer fields) {
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
      value = encodeFieldValue(dbObject.get(field.getName()), value);
      dbObject.put(field.getName(), value);
    }
    
    return dbObject;
  }
  
  @SuppressWarnings("unchecked")
  protected static Object encodeFieldValue(final Object currentValue, Object fieldValue) {
    if(fieldValue instanceof FudgeFieldContainer) {
      fieldValue = encode((FudgeFieldContainer) fieldValue);
    }
    if(currentValue instanceof List) {
      List l = new ArrayList((List)currentValue);
      l.add(fieldValue);
      fieldValue = l;
    } else if (currentValue != null) {
      List l = new ArrayList();
      l.add(currentValue);
      l.add(fieldValue);
      fieldValue = l;
    }
    return fieldValue;
  }

}
