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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;

/**
 * Constructs instances of {@link FudgeMsg} from any Java object by parsing
 * its fields.
 *
 * @author kirk
 */
public class FudgeObjectMessageFactory {

  /**
   * @param obj
   * @param descriptor 
   * @return
   */
  public static FudgeMsg serializeToMessage(Object obj, FudgeContext context) {
    ObjectDescriptor descriptor = FudgeObjectDescriptors.INSTANCE.getDescriptor(obj.getClass());
    FudgeMsg msg = context.newMessage();
    for(Field field : descriptor.getAllFields()) {
      Object fieldValue;
      try {
        fieldValue = field.get(obj);
      } catch (Exception e) {
        throw new FudgeRuntimeException("Cannot extract value for field " + field + " from instance " + obj, e);
      }
      String fudgeFieldName = descriptor.getFudgeFieldName(field);
      if(fudgeFieldName != null) {
        addFieldToMsg(msg, fudgeFieldName, fieldValue, context);
      }
    }
    return msg;
  }
  
  @SuppressWarnings("unchecked")
  protected static void addFieldToMsg(FudgeMsg msg, String fudgeFieldName, Object fieldValue, FudgeContext context) {
    if(fieldValue == null) {
      return;
    }
    
    if(context.getTypeDictionary().getByJavaType(fieldValue.getClass()) != null) {
      // Natively supported by this dictionary. Just add it.
      msg.add(fudgeFieldName, fieldValue);
    } else if(fieldValue instanceof List) {
      List list = (List) fieldValue;
      for(Object obj : list) {
        addFieldToMsg(msg, fudgeFieldName, obj, context);
      }
    } else if(fieldValue instanceof Set) {
      Set set = (Set) fieldValue;
      for(Object obj : set) {
        addFieldToMsg(msg, fudgeFieldName, obj, context);
      }
    } else if(fieldValue instanceof Map) {
      Map<String, Object> map = (Map<String, Object>) fieldValue;
      FudgeMsg mapMsg = createMessageFromMap(context, map);
      msg.add(fudgeFieldName, mapMsg);
    } else {
      // Descend into sub-message.
      FudgeMsg subMsg = serializeToMessage(fieldValue, context);
      msg.add(fudgeFieldName, subMsg);
    }
  }

  /**
   * @param context
   * @param map
   * @return
   */
  @SuppressWarnings("unchecked")
  public static FudgeMsg createMessageFromMap(FudgeContext context,
      Map<String, Object> map) {
    FudgeMsg mapMsg = context.newMessage();
    for(Map.Entry entry : map.entrySet()) {
      if(!(entry.getKey() instanceof String)) {
        throw new IllegalArgumentException("Can only encode maps as FudgeMsg if all keys are Strings.");
      }
      addFieldToMsg(mapMsg, (String) entry.getKey(), entry.getValue(), context);
    }
    return mapMsg;
  }
  
}
