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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

// TODO kirk 2009-11-12 -- We can do better here if we have the context,
// because we can actually tell if the object classes have type mappings.

/**
 * Holds metadata about objects that are the subject of {@link FudgeObjectStreamReader}
 * or {@link FudgeObjectStreamWriter} invocations.
 *
 * @author kirk
 */
public class ObjectDescriptor {
  private final Class<?> _describedClass;
  private final Set<Field> _allFields = new TreeSet<Field>(new Comparator<Field>() {
    @Override
    public int compare(Field o1, Field o2) {
      return o1.getName().compareTo(o2.getName());
    }
  });
  private final Map<String, Field> _fudgeFieldNamestoFields =
    new HashMap<String, Field>();
  private final Map<Field, String> _fieldToFudgeFieldNames=
    new HashMap<Field, String>();
  
  public ObjectDescriptor(Class<?> describedClass) {
    assert describedClass != null;
    _describedClass = describedClass;
    buildDescriptor();
  }
  
  public Field getField(String fudgeFieldName) {
    return _fudgeFieldNamestoFields.get(fudgeFieldName);
  }
  
  public String getFudgeFieldName(Field field) {
    return _fieldToFudgeFieldNames.get(field);
  }

  /**
   * @return the allFields
   */
  public Set<Field> getAllFields() {
    return Collections.unmodifiableSet(_allFields);
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
    loadAllDeclaredFields();
    Field[] fieldArray = _allFields.toArray(new Field[0]);
    AccessibleObject.setAccessible(fieldArray, true);
    
    for(Field field : _allFields) {
      String fudgeFieldName = field.getName();
      if(fudgeFieldName.startsWith("_")) {
        fudgeFieldName = fudgeFieldName.substring(1);
      }
      int fieldModifiers = field.getModifiers();
      if(Modifier.isStatic(fieldModifiers)) {
        continue;
      }
      if(Modifier.isTransient(fieldModifiers)) {
        continue;
      }
      if(!StringUtils.isEmpty(fudgeFieldName)) {
        _fudgeFieldNamestoFields.put(fudgeFieldName, field);
        _fieldToFudgeFieldNames.put(field, fudgeFieldName);
      }
    }
  }

  /**
   * @return
   */
  private void loadAllDeclaredFields() {
    Class<?> currClass = getDescribedClass();
    while(currClass != null) {
      Field[] currFields = currClass.getDeclaredFields();
      for(Field currField : currFields) { 
        _allFields.add(currField);
      }
      currClass = currClass.getSuperclass();
    }
  }

}
