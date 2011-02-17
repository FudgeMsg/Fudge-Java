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
package org.fudgemsg.types.secondary;

import java.util.UUID;

import org.fudgemsg.types.ByteArrayFieldType;
import org.fudgemsg.types.SecondaryFieldType;

/**
 * Secondary type for UUID conversion to/from byte[]. The conversion is
 * most significant bits first.
 *
 * @author Andrew Griffin
 */
public class JavaUtilUUIDFieldType extends SecondaryFieldType<UUID,byte[]> {
  
  /**
   * Singleton instance of the type.
   */
  public static final JavaUtilUUIDFieldType INSTANCE = new JavaUtilUUIDFieldType ();
  
  private JavaUtilUUIDFieldType () {
    super (ByteArrayFieldType.LENGTH_16_INSTANCE, UUID.class);
    
  }

  /**
   *
   */
  @Override
  public byte[] secondaryToPrimary(UUID object) {
    final byte[] data = new byte[16];
    long l = object.getMostSignificantBits ();
    for (int i = 7; i >= 0; i--) {
      data[i] = (byte)(l & 0xFFl);
      l >>= 8;
    }
    l = object.getLeastSignificantBits ();
    for (int i = 15; i >= 8; i--) {
      data[i] = (byte)(l & 0xFFl);
      l >>= 8;
    }
    return data;
  }
  
  /**
   *
   */
  @Override
  public UUID primaryToSecondary (byte[] data) {
    long lo = 0;
    long hi = 0;
    for (int i = 0; i < 8; i++) {
      hi <<= 8;
      hi |= (long)data[i] & 0xFFl;
    }
    for (int i = 8; i < 16; i++) {
      lo <<= 8;
      lo |= (long)data[i] & 0xFFl;
    }
    return new UUID (hi, lo); 
  }
  
}