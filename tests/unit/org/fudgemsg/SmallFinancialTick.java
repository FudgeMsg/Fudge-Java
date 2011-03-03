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

import java.io.Serializable;

/**
 * Intended to model a very small tick, with just a few key fields.
 *
 * @author Kirk Wylie
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
  
  /**
   * @param fc [documentation not available]
   * @return [documentation not available]
   */
  public FudgeFieldContainer toFudgeMsg (final FudgeMessageFactory fc) {
    MutableFudgeFieldContainer msg = fc.newMessage ();
    msg.add ("bid", (Double)_bid);
    msg.add ("ask", (Double)_ask);
    msg.add ("bidVolume", (Double)_bidVolume);
    msg.add ("askVolume", (Double)_askVolume);
    msg.add ("timestamp", (Long)_timestamp);
    return msg;
  }

}
