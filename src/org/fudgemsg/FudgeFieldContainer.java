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

import java.util.List;
import java.util.Set;

import org.fudgemsg.mapping.FudgeObjectDictionary;

/**
 * An interface defining any arbitrary container for fields that can
 * be described by the Fudge specification. Applications working with
 * messages received should use this interface in place of a concrete
 * instantiation for flexibility in how the message can be decoded. 
 *
 * @author kirk
 */
public interface FudgeFieldContainer extends Iterable<FudgeField> {

  /**
   * Returns the total number of fields within the message.
   * 
   * @return number of fields
   */
  short getNumFields();

  /**
   * Return an <em>unmodifiable</em> list of all the fields in this message, in the index
   * order for those fields.
   * 
   * @return the list
   */
  List<FudgeField> getAllFields();
  
  /**
   * Returns an <em>unmodifiable</em> set of all field names in the message.
   * 
   *  @return the set
   */
  Set<String> getAllFieldNames();

  /**
   * Returns the first field in the message with the given index offset.
   * 
   * @param index the zero-based offset into the message of the field
   * @return the field
   */
  FudgeField getByIndex(int index);

  /**
   * Returns an <em>unmodifiable</em> list of all fields with the given ordinal, preserving the ordering.
   * 
   * @param ordinal ordinal index
   * @return the field
   */
  List<FudgeField> getAllByOrdinal(int ordinal);

  /**
   * Returns the first field in the message with the given ordinal, or {@code null} if the ordinal does not exist.
   * 
   * @param ordinal ordinal index
   * @return the field
   */
  FudgeField getByOrdinal(int ordinal);

  /**
   * Returns an <em>unmodifiable</em> list of all fields with the given name, preserving the ordering.
   * 
   * @param name field name
   * @return the list of fields
   */
  List<FudgeField> getAllByName(String name);

  /**
   * Returns the first field in the message with the given name, or {@code null} if the name does not exist.
   * 
   * @param name field name
   * @return the field
   */
  FudgeField getByName(String name);
  
  /**
   * <p>Attempts to convert a field to a specific value type. Depending on the underlying implementation this may mean
   * a conversion through a related {@link FudgeTypeDictionary}, use of a {@link FudgeObjectDictionary}, that the
   * message arrived through.</p>
   * 
   * <p>The conversion logic has to be at the message level rather than the field as a field is not able to resolve
   * any context - e.g. it's underlying {@link FudgeFieldType} may be shared between a number of encoding strategies. If
   * an implementation does not have any conversion abilities available, it must as a minimum return {@code null}
   * for a {@code null} field value, and return the field value unchanged if it is assignable to the type
   * requested.</p>
   * 
   * @param <T> class to convert to
   * @param clazz Java class to convert to
   * @param field field whose data to convert
   * @throws IllegalArgumentException if the requested target class is not appropriate for the field
   * @return the converted field value
   */
  <T> T getFieldValue (Class<T> clazz, FudgeField field) throws IllegalArgumentException;

  /**
   * Returns the value of the first field in the message with the given name, or {@code null} if the name does not exist.
   * 
   * @param name field name
   * @return field value
   */
  Object getValue(String name);
  
  /**
   * Returns the value of the first field in the message with the given ordinal, or {@code null} if the ordinal does not exist.
   * 
   * @param ordinal ordinal index
   * @return field value 
   */
  Object getValue(int ordinal);

  /**
   * Returns the value of the first field in the message with the matching name and ordinal, or {@code null} if no field matches both name and ordinal.
   * 
   * @param name field name
   * @param ordinal ordinal index
   * @return field value
   */
  Object getValue(String name, Integer ordinal);

  /**
   * Returns the value of the first field in the message with the given name that holds a {@code double} type, or {@code null} if no such field exists.
   * 
   * @param fieldName field name
   * @return field value
   */
  Double getDouble(String fieldName);

  /**
   * Returns the value of the first field in the message with the given ordinal that holds a {@code double} type, or {@code null} if no such field exists.
   * 
   * @param ordinal ordinal index
   * @return field value
   */
  Double getDouble(int ordinal);

  /**
   * Returns the value of the first field in the message with the given name that holds a {@code float} type, or {@code null} if no such field exists.
   * 
   * @param fieldName field name
   * @return field value
   */
  Float getFloat(String fieldName);

  /**
   * Returns the value of the first field in the message with the given ordinal that holds a {@code float} type, or {@code null} if no such field exists.
   * 
   * @param ordinal ordinal index
   * @return field value
   */
  Float getFloat(int ordinal);

  /**
   * Returns the value of the first field in the message with the given name that holds a {@code long} type, or {@code null} if no such field exists.
   * 
   *  @param fieldName field name
   *  @return field value
   */
  Long getLong(String fieldName);

  /**
   * Returns the value of the first field in the message with the given ordinal that holds a {@code long} type, or {@code null} if no such field exists.
   * 
   *  @param ordinal ordinal index
   *  @return field value
   */
  Long getLong(int ordinal);

  /**
   * Returns the value of the first field in the message with the given name that holds a {@code int} type, or {@code null} if no such field exists.
   * 
   *  @param fieldName field name
   *  @return field value
   */
  Integer getInt(String fieldName);

  /**
   * Returns the value of the first field in the message with the given ordinal that holds a {@code int} type, or {@code null} if no such field exists.
   * 
   *  @param ordinal ordinal index
   *  @return field value
   */
  Integer getInt(int ordinal);

  /**
   * Returns the value of the first field in the message with the given name that holds a {@code short} type, or {@code null} if no such field exists.
   * 
   *  @param fieldName field name
   *  @return field value
   */
  Short getShort(String fieldName);

  /**
   * Returns the value of the first field in the message with the given ordinal that holds a {@code short} type, or {@code null} if no such field exists.
   * 
   *  @param ordinal ordinal index
   *  @return field value
   */
  Short getShort(int ordinal);

  /**
   * Returns the value of the first field in the message with the given name that holds a {@code byte} type, or {@code null} if no such field exists.
   * 
   *  @param fieldName field name
   *  @return field value
   */
  Byte getByte(String fieldName);

  /**
   * Returns the value of the first field in the message with the given ordinal that holds a {@code byte} type, or {@code null} if no such field exists.
   * 
   *  @param ordinal ordinal index
   *  @return field value
   */
  Byte getByte(int ordinal);

  /**
   * Returns the value of the first field in the message with the given name that holds a {@code String} type, or {@code null} if no such field exists.
   * 
   *  @param fieldName field name
   *  @return field value
   */
  String getString(String fieldName);

  /**
   * Returns the value of the first field in the message with the given ordinal that holds a {@code String} type, or {@code null} if no such field exists.
   * 
   *  @param ordinal ordinal index
   *  @return field value
   */
  String getString(int ordinal);

  /**
   * Returns the value of the first field in the message with the given name that holds a {@code boolean} type, or {@code null} if no such field exists.
   * 
   *  @param fieldName field name
   *  @return field value
   */
  Boolean getBoolean(String fieldName);

  /**
   * Returns the value of the first field in the message with the given ordinal that holds a {@code boolean} type, or {@code null} if no such field exists.
   * 
   *  @param ordinal ordinal index
   *  @return field value
   */
  Boolean getBoolean(int ordinal);
  
  /**
   * Returns the value of the first field in the message with the given name that holds a {@code message} type, or {@code null} if no such field exists.
   * 
   *  @param name field name
   *  @return field value
   */
  FudgeFieldContainer getMessage(String name);
  
  /**
   * Returns the value of the first field in the message with the given ordinal that holds a {@code message} type, or {@code null} if no such field exists.
   * 
   * @param ordinal ordinal index
   * @return field value
   */
  FudgeFieldContainer getMessage(int ordinal);
}
