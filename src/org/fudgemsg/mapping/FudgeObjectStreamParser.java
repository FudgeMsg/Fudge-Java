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

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeStreamReader;

/**
 * 
 *
 * @author kirk
 */
public class FudgeObjectStreamParser {
  private static final ConcurrentMap<Class<?>, ObjectDescriptor> s_descriptors;
  
  static {
    s_descriptors = new ConcurrentHashMap<Class<?>, ObjectDescriptor>();
  }
  
  public <T> T parse(Class<T> objectClass, FudgeStreamReader reader) {
    // TODO kirk 2009-11-12 -- Handle Taxonomies
    T result = null;
    try {
      result = objectClass.newInstance();
    } catch (Exception e) {
      throw new FudgeRuntimeException("Unable to instantiate instance of " + objectClass, e);
    }
    ObjectDescriptor descriptor = getDescriptor(objectClass);
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
        fieldValue = parse(classField.getType(), reader);
        break;
      }
      if((classField != null) && (fieldValue != null)) {
        try {
          classField.set(result, fieldValue);
        } catch (Exception e) {
          throw new FudgeRuntimeException("Unable to set field " + classField + " to " + fieldValue + " on instance of " + objectClass, e);
        }
      }
    }
    return result;
  }
  
  protected ObjectDescriptor getDescriptor(Class<?> clazz) {
    ObjectDescriptor objectDescriptor = s_descriptors.get(clazz);
    if(objectDescriptor == null) {
      ObjectDescriptor freshDescriptor = new ObjectDescriptor(clazz);
      objectDescriptor = s_descriptors.putIfAbsent(clazz, freshDescriptor);
      if(objectDescriptor == null) {
        objectDescriptor = freshDescriptor;
      }
    }
    assert objectDescriptor != null;
    return objectDescriptor;
  }

}
