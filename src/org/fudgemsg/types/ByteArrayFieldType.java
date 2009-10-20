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
package org.fudgemsg.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.taxon.FudgeTaxonomy;


/**
 * The type definition for a byte array.
 *
 * @author kirk
 */
public class ByteArrayFieldType extends FudgeFieldType<byte[]> {
  public static final ByteArrayFieldType VARIABLE_SIZED_INSTANCE = new ByteArrayFieldType();
  public static final ByteArrayFieldType LENGTH_4_INSTANCE = new ByteArrayFieldType(FudgeTypeDictionary.BYTE_ARR_4_TYPE_ID, 4);
  public static final ByteArrayFieldType LENGTH_8_INSTANCE = new ByteArrayFieldType(FudgeTypeDictionary.BYTE_ARR_8_TYPE_ID, 8);
  public static final ByteArrayFieldType LENGTH_16_INSTANCE = new ByteArrayFieldType(FudgeTypeDictionary.BYTE_ARR_16_TYPE_ID, 16);
  public static final ByteArrayFieldType LENGTH_20_INSTANCE = new ByteArrayFieldType(FudgeTypeDictionary.BYTE_ARR_20_TYPE_ID, 20);
  public static final ByteArrayFieldType LENGTH_32_INSTANCE = new ByteArrayFieldType(FudgeTypeDictionary.BYTE_ARR_32_TYPE_ID, 32);
  public static final ByteArrayFieldType LENGTH_64_INSTANCE = new ByteArrayFieldType(FudgeTypeDictionary.BYTE_ARR_64_TYPE_ID, 64);
  public static final ByteArrayFieldType LENGTH_128_INSTANCE = new ByteArrayFieldType(FudgeTypeDictionary.BYTE_ARR_128_TYPE_ID, 128);
  public static final ByteArrayFieldType LENGTH_256_INSTANCE = new ByteArrayFieldType(FudgeTypeDictionary.BYTE_ARR_256_TYPE_ID, 256);
  public static final ByteArrayFieldType LENGTH_512_INSTANCE = new ByteArrayFieldType(FudgeTypeDictionary.BYTE_ARR_512_TYPE_ID, 512);
  
  public ByteArrayFieldType() {
    super(FudgeTypeDictionary.BYTE_ARRAY_TYPE_ID, byte[].class, true, 0);
  }
  
  public ByteArrayFieldType(byte typeId, int length) {
    super(typeId, byte[].class, false, length);
  }
  
  public static ByteArrayFieldType getBestMatch(byte[] array) {
    if(array == null) {
      return VARIABLE_SIZED_INSTANCE;
    }
    switch(array.length) {
    case 4: return LENGTH_4_INSTANCE;
    case 8: return LENGTH_8_INSTANCE;
    case 16: return LENGTH_16_INSTANCE;
    case 20: return LENGTH_20_INSTANCE;
    case 32: return LENGTH_32_INSTANCE;
    case 64: return LENGTH_64_INSTANCE;
    case 128: return LENGTH_128_INSTANCE;
    case 256: return LENGTH_256_INSTANCE;
    case 512: return LENGTH_512_INSTANCE;
    default: return VARIABLE_SIZED_INSTANCE;
    }
  }

  @Override
  public int getVariableSize(byte[] value, FudgeTaxonomy taxonomy) {
    return value.length;
  }

  @Override
  public byte[] readValue(DataInput input, int dataSize, FudgeTypeDictionary typeDictionary) throws IOException {
    if(!isVariableSize()) {
      dataSize = getFixedSize();
    }
    byte[] result = new byte[dataSize];
    input.readFully(result);
    return result;
  }

  @Override
  public void writeValue(DataOutput output, byte[] value, FudgeTaxonomy taxonomy) throws IOException {
    if(!isVariableSize()) {
      if(value.length != getFixedSize()) {
        throw new IllegalArgumentException("Used fixed size type of size " + getFixedSize() + " but passed array of size " + value.length);
      }
    }
    output.write(value);
  }

}
