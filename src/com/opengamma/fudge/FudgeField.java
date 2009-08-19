/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

/**
 * A read-only representation of a field which is contained in a fudge
 * message, or a stream of fudge encoded data.
 *
 * @author kirk
 */
public interface FudgeField {
  FudgeFieldType<?> getType();
  Object getValue();
  Short getOrdinal();
  String getName();

}
