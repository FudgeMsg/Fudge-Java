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
package org.fudgemsg.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.fudgemsg.FudgeFieldType;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.UTF8;
import org.fudgemsg.taxon.FudgeTaxonomy;

/**
 * The type definition for a Modified UTF-8 encoded string.
 *
 * @author Kirk Wylie
 */
public class StringFieldType extends FudgeFieldType<String> {

  /**
   * Standard Fudge field type: string. See {@link FudgeTypeDictionary#STRING_TYPE_ID}.
   */
  public static final StringFieldType INSTANCE = new StringFieldType();
  
  private StringFieldType() {
    super(FudgeTypeDictionary.STRING_TYPE_ID, String.class, true, 0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getVariableSize(String value, FudgeTaxonomy taxonomy) {
    return UTF8.getLengthBytes(value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String readValue(DataInput input, int dataSize) throws IOException {
    return UTF8.readString(input, dataSize);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeValue(DataOutput output, String value) throws IOException {
    UTF8.writeString(output, value);
  }

}
