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
package org.fudgemsg;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A container of Fudge fields providing simple access to principal types.
 * <p>
 * The Fudge specification is built around messages containing a list of {@link FudgeField fields}.
 * This interface is the high-level representation of the list of fields.
 * <p>
 * Each field may be referenced by a name or by an ordinal.
 * All four combinations are possible - from both present to both absent.
 * Methods provide the ability to lookup a field by both name or ordinal.
 * <p>
 * Applications working with messages should use this interface rather than
 * {@link FudgeMsg} directly where possible for flexibility.
 * <p>
 * This interface makes no guarantees about the mutability or thread-safety of implementations.
 */
public interface FudgeFieldContainer extends Iterable<FudgeField> {

  /**
   * Gets the size of the container.
   * <p>
   * This returns the total number of fields.
   * 
   * @return number of fields
   */
  short getNumFields();

  /**
   * Checks if the container is empty.
   * <p>
   * This checks to see if there are any fields present.
   * 
   * @return true if the container is empty
   */
  boolean isEmpty();

  //-------------------------------------------------------------------------
  /**
   * Gets an iterator over the list of fields in this container.
   * <p>
   * A container is ordered and the returned iterator reflects that order.
   * 
   * @return the iterator of fields, may be unmodifiable, not null
   */
  @Override
  Iterator<FudgeField> iterator();

  /**
   * Gets the list of all the fields in this container.
   * <p>
   * A container is ordered and the returned list reflects that order.
   * 
   * @return the unmodifiable list of fields, not null
   */
  List<FudgeField> getAllFields();

  /**
   * Gets the set of all unique field names in this container.
   * 
   * @return the unmodifiable set of names, not null
   */
  Set<String> getAllFieldNames();

  //-------------------------------------------------------------------------
  /**
   * Gets the field in the container with the given index offset.
   * 
   * @param index  the zero-based offset into the message of the field, valid
   * @return the field
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  FudgeField getByIndex(int index);

  /**
   * Checks whether this container has any field which matches the given name.
   * 
   * @param name  the field name to check, null returns false
   * @return true if this container has at least one field with the specified name
   */
  boolean hasField(String name);

  /**
   * Gets the list of all fields with the given name.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns all matching fields in the order of the container.
   * 
   * @param name  the field name, null matches fields without a name
   * @return the unmodifiable list of matching fields, not null
   */
  List<FudgeField> getAllByName(String name);

  /**
   * Gets the first field with the given name.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns the first that matches.
   * 
   * @param name  the field name, null matches fields without a name
   * @return the first matching field, null if not found
   */
  FudgeField getByName(String name);

  /**
   * Checks whether this container has any field which matches the given ordinal.
   * 
   * @param ordinal  the field ordinal to check
   * @return true if this container has at least one field with the specified ordinal
   */
  boolean hasField(int ordinal);

  /**
   * Gets the list of all fields with the given ordinal.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns all matching fields in the order of the container.
   * 
   * @param ordinal  the field ordinal
   * @return the unmodifiable list of matching fields, not null
   */
  List<FudgeField> getAllByOrdinal(int ordinal);

  /**
   * Gets the first field with the given ordinal.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns the first that matches.
   * 
   * @param ordinal  the field ordinal
   * @return the first matching field, null if not found
   */
  FudgeField getByOrdinal(int ordinal);

  //-------------------------------------------------------------------------
  /**
   * Gets the value of a field converted to a specific type.
   * <p>
   * This attempts to convert the given field to a specific value type.
   * Depending on the underlying implementation this may use a {@link FudgeTypeDictionary}.
   * <p>
   * This conversion logic is at the message level as the field itself does not contain sufficient information.
   * For example, the underlying {@link FudgeFieldType} may be shared between a number of encoding strategies.
   * If an implementation does not have the ability to convert, it must return {@code null} for a {@code null}
   * field value and the field value unchanged if it is assignable to the type requested.
   * 
   * @param <T>  the class to convert to
   * @param clazz  the type to convert to, not null
   * @param field  the field whose data to convert, null returns null
   * @return the converted field value, null if the field value was null
   * @throws IllegalArgumentException if the requested target class is not appropriate for the field
   */
  <T> T getFieldValue(Class<T> clazz, FudgeField field);

  /**
   * Gets the value of the first field with the given name.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns the value of the first that matches.
   * 
   * @param <T>  the class to convert to
   * @param clazz  the type to convert to, not null
   * @param name  the field name, null matches fields without a name
   * @return the first matching field converted as requested, null if not found
   */
  <T> T getValue(Class<T> clazz, String name);

  /**
   * Gets the value of the first field with the given ordinal.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns the value of the first that matches.
   * 
   * @param <T>  the class to convert to
   * @param clazz  the type to convert to, not null
   * @param ordinal  the field ordinal
   * @return the converted field value, null if the field value was null
   * @throws IllegalArgumentException if the requested target class is not appropriate for the field
   */
  <T> T getValue(Class<T> clazz, int ordinal);

  //-------------------------------------------------------------------------
  /**
   * Gets the value of the first field with the given name as an {@code Object}.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns the value of the first that matches.
   * 
   * @param name  the field name, null matches fields without a name
   * @return the first matching field, null if not found
   */
  Object getValue(String name);

  /**
   * Gets the value of the first field with the given ordinal as an {@code Object}.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns the value of the first that matches.
   * 
   * @param ordinal  the field ordinal
   * @return the first matching field, null if not found
   */
  Object getValue(int ordinal);

  /**
   * Gets the value of the first field with the given name as a {@code Double}.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns the value of the first that matches with a type that can
   * be converted to a double.
   * 
   * @param name  the field name, null matches fields without a name
   * @return the first matching field, null if not found
   */
  Double getDouble(String name);

  /**
   * Gets the value of the first field with the given ordinal as a {@code Double}.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns the value of the first that matches with a type that can
   * be converted to a double.
   * 
   * @param ordinal  the field ordinal
   * @return the first matching field, null if not found
   */
  Double getDouble(int ordinal);

  /**
   * Gets the value of the first field with the given name as a {@code Float}.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns the value of the first that matches with a type that can
   * be converted to a float.
   * 
   * @param name  the field name, null matches fields without a name
   * @return the first matching field, null if not found
   */
  Float getFloat(String name);

  /**
   * Gets the value of the first field with the given ordinal as a {@code Float}.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns the value of the first that matches with a type that can
   * be converted to a float.
   * 
   * @param ordinal  the field ordinal
   * @return the first matching field, null if not found
   */
  Float getFloat(int ordinal);

  /**
   * Gets the value of the first field with the given name as a {@code Long}.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns the value of the first that matches with a type that can
   * be converted to a long.
   * 
   * @param name  the field name, null matches fields without a name
   * @return the first matching field, null if not found
   */
  Long getLong(String name);

  /**
   * Gets the value of the first field with the given ordinal as a {@code Long}.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns the value of the first that matches with a type that can
   * be converted to a long.
   * 
   * @param ordinal  the field ordinal
   * @return the first matching field, null if not found
   */
  Long getLong(int ordinal);

  /**
   * Gets the value of the first field with the given name as an {@code Integer}.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns the value of the first that matches with a type that can
   * be converted to an integer.
   * 
   * @param name  the field name, null matches fields without a name
   * @return the first matching field, null if not found
   */
  Integer getInt(String name);

  /**
   * Gets the value of the first field with the given ordinal as an {@code Integer}.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns the value of the first that matches with a type that can
   * be converted to an integer.
   * 
   * @param ordinal  the field ordinal
   * @return the first matching field, null if not found
   */
  Integer getInt(int ordinal);

  /**
   * Gets the value of the first field with the given name as a {@code Short}.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns the value of the first that matches with a type that can
   * be converted to a short.
   * 
   * @param name  the field name, null matches fields without a name
   * @return the first matching field, null if not found
   */
  Short getShort(String name);

  /**
   * Gets the value of the first field with the given ordinal as a {@code Short}.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns the value of the first that matches with a type that can
   * be converted to a short.
   * 
   * @param ordinal  the field ordinal
   * @return the first matching field, null if not found
   */
  Short getShort(int ordinal);

  /**
   * Gets the value of the first field with the given name as a {@code Byte}.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns the value of the first that matches with a type that can
   * be converted to a byte.
   * 
   * @param name  the field name, null matches fields without a name
   * @return the first matching field, null if not found
   */
  Byte getByte(String name);

  /**
   * Gets the value of the first field with the given ordinal as a {@code Byte}.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns the value of the first that matches with a type that can
   * be converted to a byte.
   * 
   * @param ordinal  the field ordinal
   * @return the first matching field, null if not found
   */
  Byte getByte(int ordinal);

  /**
   * Gets the value of the first field with the given name as a {@code String}.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns the value of the first that matches with a type that can
   * be converted to a string.
   * 
   * @param name  the field name, null matches fields without a name
   * @return the first matching field, null if not found
   */
  String getString(String name);

  /**
   * Gets the value of the first field with the given ordinal as a {@code String}.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns the value of the first that matches with a type that can
   * be converted to a string.
   * 
   * @param ordinal  the field ordinal
   * @return the first matching field, null if not found
   */
  String getString(int ordinal);

  /**
   * Gets the value of the first field with the given name as a {@code Boolean}.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns the value of the first that matches with a type that can
   * be converted to a boolean.
   * 
   * @param name  the field name, null matches fields without a name
   * @return the first matching field, null if not found
   */
  Boolean getBoolean(String name);

  /**
   * Gets the value of the first field with the given ordinal as a {@code Boolean}.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns the value of the first that matches with a type that can
   * be converted to a boolean.
   * 
   * @param ordinal  the field ordinal
   * @return the first matching field, null if not found
   */
  Boolean getBoolean(int ordinal);

  /**
   * Gets the value of the first field with the given name as a {@code FudgeFieldContainer}.
   * <p>
   * A container is ordered and may contain multiple fields with the same name.
   * This method returns the value of the first that matches with a type that can
   * be converted to a message.
   * 
   * @param name  the field name, null matches fields without a name
   * @return the first matching field, null if not found
   */
  FudgeFieldContainer getMessage(String name);

  /**
   * Gets the value of the first field with the given ordinal as a {@code FudgeFieldContainer}.
   * <p>
   * A container is ordered and may contain multiple fields with the same ordinal.
   * This method returns the value of the first that matches with a type that can
   * be converted to a message.
   * 
   * @param ordinal  the field ordinal
   * @return the first matching field, null if not found
   */
  FudgeFieldContainer getMessage(int ordinal);

}
