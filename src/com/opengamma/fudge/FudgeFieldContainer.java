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
package com.opengamma.fudge;

import java.util.List;

/**
 * An interface defining any arbitrary container for fields that can
 * be described by the Fudge specification.
 *
 * @author kirk
 */
public interface FudgeFieldContainer extends Iterable<FudgeField> {

  short getNumFields();

  /**
   * Return an <em>unmodifiable</em> list of all the fields in this message, in the index
   * order for those fields.
   * 
   * @return
   */
  List<FudgeField> getAllFields();

  FudgeField getByIndex(int index);

  List<FudgeField> getAllByOrdinal(int ordinal);

  FudgeField getByOrdinal(int ordinal);

  List<FudgeField> getAllByName(String name);

  FudgeField getByName(String name);

  Object getValue(String name);

  Object getValue(int ordinal);

  Object getValue(String name, Integer ordinal);

  Double getDouble(String fieldName);

  Double getDouble(int ordinal);

  Float getFloat(String fieldName);

  Float getFloat(int ordinal);

  Long getLong(String fieldName);

  Long getLong(int ordinal);

  Integer getInt(String fieldName);

  Integer getInt(int ordinal);

  Short getShort(String fieldName);

  Short getShort(int ordinal);

  Byte getByte(String fieldName);

  Byte getByte(int ordinal);

  String getString(String fieldName);

  String getString(int ordinal);

  Boolean getBoolean(String fieldName);

  Boolean getBoolean(int ordinal);
}
