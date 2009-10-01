/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.fudge;

/**
 * 
 *
 * @author kirk
 */
public interface MutableFudgeFieldContainer extends FudgeFieldContainer {

  public abstract void add(FudgeField field);

  public abstract void add(Object value, String name);

  public abstract void add(Object value, Short ordinal);

  public abstract void add(Object value, String name, Short ordinal);

  public abstract void add(FudgeFieldType<?> type, Object value, String name,
      Short ordinal);

}
