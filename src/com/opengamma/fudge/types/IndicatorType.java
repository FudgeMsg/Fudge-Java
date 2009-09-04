/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.types;

/**
 * The only value of a field with the Indicator type.
 *
 * @author kirk
 */
public final class IndicatorType {
  private IndicatorType() {
  }
  
  /**
   * The only instance of this type.
   */
  public static final IndicatorType INSTANCE = new IndicatorType();

}
