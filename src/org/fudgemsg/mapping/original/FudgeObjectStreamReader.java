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

package org.fudgemsg.mapping.original;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.io.IOException;

import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeStreamReader;

/**
 * 
 *
 * @author kirk
 */
@Deprecated
public class FudgeObjectStreamReader {
  public <T> T read(Class<T> objectClass, FudgeStreamReader reader) throws IOException {
    T result = null;
    try {
      result = objectClass.newInstance();
    } catch (Exception e) {
      throw new FudgeRuntimeException("Unable to instantiate instance of " + objectClass, e);
    }
    ObjectDescriptor descriptor = FudgeObjectDescriptors.INSTANCE.getDescriptor(objectClass);
    Field classField = null;
    Object fieldValue = null;
    while(reader.hasNext()) {
      FudgeStreamReader.FudgeStreamElement element = reader.next();
      switch(element) {
      case SUBMESSAGE_FIELD_END:
        return result;
      case MESSAGE_ENVELOPE:
        // We don't do anything with the envelope
        break;
      case SIMPLE_FIELD:
        fieldValue = reader.getFieldValue();
        classField = descriptor.getField(reader.getFieldName());
        break;
      case SUBMESSAGE_FIELD_START:
        classField = descriptor.getField(reader.getFieldName());
        if(Map.class.isAssignableFrom(classField.getType())) {
          fieldValue = processSubmessageAsMap(classField.getType(), reader);
        } else {
          fieldValue = read(classField.getType(), reader);
        }
        break;
      }
      if((classField != null) && (fieldValue != null)) {
        try {
          if(List.class.isAssignableFrom(classField.getType())) {
            processListValue(classField, result, fieldValue);
          } else if(Set.class.isAssignableFrom(classField.getType())) {
            processSetValue(classField, result, fieldValue);
          } else {
            classField.set(result, fieldValue);
          }
        } catch (Exception e) {
          throw new FudgeRuntimeException("Unable to set field " + classField + " to " + fieldValue + " on instance of " + objectClass, e);
        }
      }
    }
    return result;
  }
  
  /**
   * @param classField
   * @param fieldValue
   */
  @SuppressWarnings("unchecked")
  protected void processListValue(Field classField, Object result, Object fieldValue) throws Exception {
    List l = (List) classField.get(result);
    if(l == null) {
      l = new ArrayList();
      classField.set(result, l);
    }
    l.add(fieldValue);
  }

  /**
   * @param classField
   * @param fieldValue
   */
  @SuppressWarnings("unchecked")
  protected void processSetValue(Field classField, Object result, Object fieldValue) throws Exception {
    Set s = (Set) classField.get(result);
    if(s == null) {
      s = new HashSet();
      classField.set(result, s);
    }
    s.add(fieldValue);
  }

  /**
   * @param type
   * @param reader
   */
  protected Map<String, Object> processSubmessageAsMap(Class<?> type, FudgeStreamReader reader) throws IOException {
    Map<String, Object> result = new TreeMap<String, Object>();
    Object fieldValue = null;
    while(reader.hasNext()) {
      FudgeStreamReader.FudgeStreamElement element = reader.next();
      switch(element) {
      case SUBMESSAGE_FIELD_END:
        return result;
      case SIMPLE_FIELD:
        fieldValue = reader.getFieldValue();
        break;
      case SUBMESSAGE_FIELD_START:
        throw new IllegalArgumentException("Only single-level maps handled.");
      }
      if(fieldValue != null) {
        result.put(reader.getFieldName(), fieldValue);
      }
    }
    return result;
  }

}
