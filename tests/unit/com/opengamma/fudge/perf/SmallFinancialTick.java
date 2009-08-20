/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.perf;

import java.io.Serializable;

/**
 * Intended to model a very small tick, with just a few key fields.
 *
 * @author kirk
 */
public class SmallFinancialTick implements Serializable {
  private double _bid;
  private double _ask;
  private double _bidVolume;
  private double _askVolume;
  private long _timestamp = Long.MAX_VALUE - Short.MAX_VALUE;
  /**
   * @return the bid
   */
  public double getBid() {
    return _bid;
  }
  /**
   * @param bid the bid to set
   */
  public void setBid(double bid) {
    _bid = bid;
  }
  /**
   * @return the ask
   */
  public double getAsk() {
    return _ask;
  }
  /**
   * @param ask the ask to set
   */
  public void setAsk(double ask) {
    _ask = ask;
  }
  /**
   * @return the bidVolume
   */
  public double getBidVolume() {
    return _bidVolume;
  }
  /**
   * @param bidVolume the bidVolume to set
   */
  public void setBidVolume(double bidVolume) {
    _bidVolume = bidVolume;
  }
  /**
   * @return the askVolume
   */
  public double getAskVolume() {
    return _askVolume;
  }
  /**
   * @param askVolume the askVolume to set
   */
  public void setAskVolume(double askVolume) {
    _askVolume = askVolume;
  }
  /**
   * @return the timestamp
   */
  public long getTimestamp() {
    return _timestamp;
  }
  /**
   * @param timestamp the timestamp to set
   */
  public void setTimestamp(long timestamp) {
    _timestamp = timestamp;
  }

}
