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

import org.fudgemsg.types.IndicatorType;

/**
 * 
 *
 * @author kirk
 */
public final class StandardFudgeMessages {

  public static FudgeMsg createMessageAllNames() {
    FudgeMsg msg = new FudgeMsg();
    
    msg.add("boolean", Boolean.TRUE);
    msg.add("Boolean", new Boolean(false));
    msg.add("byte", (byte)5);
    msg.add("Byte", new Byte((byte)5));
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    msg.add("short", shortValue);
    msg.add("Short", new Short(shortValue));
    int intValue = ((int)Short.MAX_VALUE) + 5;
    msg.add("int", intValue);
    msg.add("Integer", new Integer(intValue));
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    msg.add("long", longValue);
    msg.add("Long", new Long(longValue));
    
    msg.add("float", 0.5f);
    msg.add("Float", new Float(0.5f));
    msg.add("double", 0.27362);
    msg.add("Double", new Double(0.27362));
    
    msg.add("String", "Kirk Wylie");
    
    msg.add("float array", new float[24]);
    msg.add("double array", new double[273]);
    msg.add("short array", new short[32]);
    msg.add("int array", new int[83]);
    msg.add("long array", new long[837]);
    
    msg.add("indicator", IndicatorType.INSTANCE);
    
    return msg;
  }
  
  public static FudgeMsg createMessageAllOrdinals() {
    FudgeMsg msg = new FudgeMsg();
    
    msg.add(1, Boolean.TRUE);
    msg.add(2, new Boolean(false));
    msg.add(3, (byte)5);
    msg.add(4, new Byte((byte)5));
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    msg.add(5, shortValue);
    msg.add(6, new Short(shortValue));
    int intValue = ((int)Short.MAX_VALUE) + 5;
    msg.add(7, intValue);
    msg.add(8, new Integer(intValue));
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    msg.add(9, longValue);
    msg.add(10, new Long(longValue));
    
    msg.add(11, 0.5f);
    msg.add(12, new Float(0.5f));
    msg.add(13, 0.27362);
    msg.add(14, new Double(0.27362));
    
    msg.add(15, "Kirk Wylie");
    
    msg.add(16, new float[24]);
    msg.add(17, new double[273]);
    
    return msg;
  }
  
  public static FudgeMsg createMessageAllByteArrayLengths() {
    FudgeMsg msg = new FudgeMsg();
    msg.add("byte[4]", new byte[4]);
    msg.add("byte[8]", new byte[8]);
    msg.add("byte[16]", new byte[16]);
    msg.add("byte[20]", new byte[20]);
    msg.add("byte[32]", new byte[32]);
    msg.add("byte[64]", new byte[64]);
    msg.add("byte[128]", new byte[128]);
    msg.add("byte[256]", new byte[256]);
    msg.add("byte[512]", new byte[512]);
    
    msg.add("byte[28]", new byte[28]);
    return msg;
  }
  
}
