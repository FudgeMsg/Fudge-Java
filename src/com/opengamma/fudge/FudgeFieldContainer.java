/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
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

  List<FudgeField> getAllByOrdinal(short ordinal);

  FudgeField getByOrdinal(short ordinal);

  List<FudgeField> getAllByName(String name);

  FudgeField getByName(String name);

  Object getValue(String name);

  Object getValue(short ordinal);

  Object getValue(String name, Short ordinal);

  Double getDouble(String fieldName);

  Double getDouble(short ordinal);

  Float getFloat(String fieldName);

  Float getFloat(short ordinal);

  Long getLong(String fieldName);

  Long getLong(short ordinal);

  Integer getInt(String fieldName);

  Integer getInt(short ordinal);

  Short getShort(String fieldName);

  Short getShort(short ordinal);

  Byte getByte(String fieldName);

  Byte getByte(short ordinal);

  String getString(String fieldName);

  String getString(short ordinal);

  Boolean getBoolean(String fieldName);

  Boolean getBoolean(short ordinal);
}
