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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

// TODO kirk 2009-11-12 -- We can do better here if we have the context,
// because we can actually tell if the object classes have type mappings.

/**
 * 
 *
 * @author kirk
 */
public class ObjectDescriptor {
  private final Class<?> _describedClass;
  private final Map<String, Field> _fudgeFieldNamestoFields =
    new HashMap<String, Field>();
  
  public ObjectDescriptor(Class<?> describedClass) {
    assert describedClass != null;
    _describedClass = describedClass;
    buildDescriptor();
  }
  
  public Field getField(String fudgeFieldName) {
    return _fudgeFieldNamestoFields.get(fudgeFieldName);
  }

  /**
   * @return the describedClass
   */
  public Class<?> getDescribedClass() {
    return _describedClass;
  }

  /**
   * 
   */
  private void buildDescriptor() {
    Set<Field> allFields = loadAllDeclaredFields();
    Field[] fieldArray = allFields.toArray(new Field[0]);
    AccessibleObject.setAccessible(fieldArray, true);
    
    for(Field field : allFields) {
      String fudgeFieldName = field.getName();
      if(fudgeFieldName.startsWith("_")) {
        fudgeFieldName = fudgeFieldName.substring(1);
      }
      if(!StringUtils.isEmpty(fudgeFieldName)) {
        _fudgeFieldNamestoFields.put(fudgeFieldName, field);
      }
    }
  }

  /**
   * @return
   */
  private Set<Field> loadAllDeclaredFields() {
    Set<Field> result = new HashSet<Field>();
    Class<?> currClass = getDescribedClass();
    while(currClass != null) {
      Field[] currFields = currClass.getDeclaredFields();
      for(Field currField : currFields) { 
        result.add(currField);
      }
      currClass = currClass.getSuperclass();
    }
    return result;
  }

}
