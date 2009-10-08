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

  public abstract void add(String name, Object value);

  public abstract void add(Integer ordinal, Object value);

  public abstract void add(String name, Integer ordinal, Object value);

  public abstract void add(String name, Integer ordinal, FudgeFieldType<?> type, Object value);

}
