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

package org.fudgemsg.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.test.FudgeUtils;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.types.StringFieldType;
import org.junit.Ignore;
import org.junit.Test;

import com.mongodb.DBObject;

/**
 *
 */
public class MongoDBFudgeBuilderTest {
  
  /*
   * 
   */
  public static class MockObject {
    private final String _value;
    public MockObject(String value) {
      _value = value;
    }
    public String getValue() {
      return _value;
    }
  }
  
  private static class MockObjectSecondaryType extends SecondaryFieldType<MockObject, String> {

    /**
     * Singleton instance of the type.
     */
    public static final MockObjectSecondaryType INSTANCE = new MockObjectSecondaryType();

    private MockObjectSecondaryType() {
      super(StringFieldType.INSTANCE, MockObject.class);
    }

    @Override
    public String secondaryToPrimary(MockObject object) {
      return object.getValue();
    }

    @Override
    public MockObject primaryToSecondary(final String string) {
      return new MockObject(string);
    }
  }

  /**
   * See http://jira.fudgemsg.org/browse/FRJ-83
   */
  @Test
  public void userProvidedTypeSystem() {
    FudgeContext context = new FudgeContext();
    context.getTypeDictionary().addType(MockObjectSecondaryType.INSTANCE);
    
    MockObject mo = new MockObject("FRJ-83");
    
    MutableFudgeFieldContainer inputMsg = context.newMessage();
    inputMsg.add("field1", mo);
    DBObject dbObject = context.fromFudgeMsg(DBObject.class, inputMsg);
    // We have to actually do something to the object to make sure it's supported
    // in the MongoDB type system.
    dbObject.toString();
    FudgeFieldContainer outputMsg = context.toFudgeMsg(dbObject).getMessage();

    assertTrue(outputMsg.hasField("field1"));
    FudgeField moField = outputMsg.getByName("field1");
    assertNotNull(moField);
    assertTrue(moField.getValue() instanceof String);
    MockObject moResult = outputMsg.getFieldValue(MockObject.class, moField);
    assertNotNull(moResult);
    assertEquals(mo.getValue(), moResult.getValue());
  }

  /**
   * Test case exposing FRJ-84.
   * Written while investigating FRJ-83.
   */
  @Test
  @Ignore("Until FRJ-84 is done")
  public void fudgeDateTime() {
    Date d = new Date();
    MutableFudgeFieldContainer inputMsg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    inputMsg.add("field1", d);
    DBObject dbObject = FudgeContext.GLOBAL_DEFAULT.fromFudgeMsg(DBObject.class, inputMsg);
    // We have to actually do something to the object to make sure it's supported
    // in the MongoDB type system.
    dbObject.toString();
    FudgeFieldContainer outputMsg = FudgeContext.GLOBAL_DEFAULT.toFudgeMsg(dbObject).getMessage();

    assertTrue(outputMsg.hasField("field1"));
    FudgeField dateField = outputMsg.getByName("field1");
    assertNotNull(dateField);
    
    FudgeUtils.assertAllFieldsMatch(inputMsg, outputMsg);
  }

}
