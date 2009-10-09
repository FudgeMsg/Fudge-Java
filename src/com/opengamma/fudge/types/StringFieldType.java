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
package com.opengamma.fudge.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.opengamma.fudge.FudgeFieldType;
import com.opengamma.fudge.FudgeTypeDictionary;
import com.opengamma.fudge.ModifiedUTF8Util;
import com.opengamma.fudge.taxon.FudgeTaxonomy;

/**
 * The type definition for a Modified UTF-8 encoded string.
 *
 * @author kirk
 */
public class StringFieldType extends FudgeFieldType<String> {
  public static final StringFieldType INSTANCE = new StringFieldType();
  
  public StringFieldType() {
    super(FudgeTypeDictionary.STRING_TYPE_ID, String.class, true, 0);
  }

  @Override
  public int getVariableSize(String value, FudgeTaxonomy taxonomy) {
    return ModifiedUTF8Util.modifiedUTF8Length(value);
  }

  @Override
  public String readValue(DataInput input, int dataSize, FudgeTypeDictionary typeDictionary) throws IOException {
    return ModifiedUTF8Util.readString(input, dataSize);
  }

  @Override
  public void writeValue(DataOutput output, String value, FudgeTaxonomy taxonomy) throws IOException {
    ModifiedUTF8Util.writeModifiedUTF8(value, output);
  }

}
