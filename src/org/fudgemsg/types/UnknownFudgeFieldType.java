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
import org.fudgemsg.UnknownFudgeFieldValue;
import org.fudgemsg.taxon.FudgeTaxonomy;


/**
 * A type class for handling variable sized field values where the type
 * isn't available in the current {@link FudgeTypeDictionary}.
 *
 * @author kirk
 */
public class UnknownFudgeFieldType extends
    FudgeFieldType<UnknownFudgeFieldValue> {
  
  public UnknownFudgeFieldType(int typeId) {
    super(typeId, UnknownFudgeFieldValue.class, true, 0);
  }

  @Override
  public int getVariableSize(UnknownFudgeFieldValue value,
      FudgeTaxonomy taxonomy) {
    return value.getContents().length;
  }

  @Override
  public UnknownFudgeFieldValue readValue(DataInput input, int dataSize, FudgeTypeDictionary typeDictionary)
      throws IOException {
    byte[] contents = new byte[dataSize];
    input.readFully(contents);
    return new UnknownFudgeFieldValue(contents, this);
  }

  @Override
  public void writeValue(DataOutput output, UnknownFudgeFieldValue value,
      FudgeTaxonomy taxonomy) throws IOException {
    output.write(value.getContents());
  }

}
