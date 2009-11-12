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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

/**
 * 
 *
 * @author jim
 */
public class FudgeUtils {
  /**
   * @param inputMsg
   * @param outputMsg
   */
  public static void assertAllFieldsMatch(FudgeFieldContainer expectedMsg, FudgeFieldContainer actualMsg) {
    Iterator<FudgeField> expectedIter = expectedMsg.getAllFields().iterator();
    Iterator<FudgeField> actualIter = actualMsg.getAllFields().iterator();
    while(expectedIter.hasNext()) {
      assertTrue(actualIter.hasNext());
      FudgeField expectedField = expectedIter.next();
      FudgeField actualField = actualIter.next();
      
      assertEquals(expectedField.getName(), actualField.getName());
      assertEquals(expectedField.getType(), actualField.getType());
      assertEquals(expectedField.getOrdinal(), actualField.getOrdinal());
      if(expectedField.getValue().getClass().isArray()) {
        assertEquals(expectedField.getValue().getClass(), actualField.getValue().getClass());
        if(expectedField.getValue() instanceof byte[]) {
          FudgeUtils.assertArraysMatch((byte[]) expectedField.getValue(), (byte[])actualField.getValue());
        } else if(expectedField.getValue() instanceof short[]) {
          FudgeUtils.assertArraysMatch((short[]) expectedField.getValue(), (short[])actualField.getValue());
        } else if(expectedField.getValue() instanceof int[]) {
          FudgeUtils.assertArraysMatch((int[]) expectedField.getValue(), (int[])actualField.getValue());
        } else if(expectedField.getValue() instanceof long[]) {
          FudgeUtils.assertArraysMatch((long[]) expectedField.getValue(), (long[])actualField.getValue());
        } else if(expectedField.getValue() instanceof float[]) {
          FudgeUtils.assertArraysMatch((float[]) expectedField.getValue(), (float[])actualField.getValue());
        } else if(expectedField.getValue() instanceof double[]) {
          FudgeUtils.assertArraysMatch((double[]) expectedField.getValue(), (double[])actualField.getValue());
        }
      } else if(expectedField.getValue() instanceof FudgeMsg) {
        assertTrue(actualField.getValue() instanceof FudgeMsg);
        assertAllFieldsMatch((FudgeMsg) expectedField.getValue(),
            (FudgeMsg) actualField.getValue());
      } else if(expectedField.getValue() instanceof UnknownFudgeFieldValue) {
        assertTrue(actualField.getValue() instanceof UnknownFudgeFieldValue);
        UnknownFudgeFieldValue expectedValue = (UnknownFudgeFieldValue) expectedField.getValue();
        UnknownFudgeFieldValue actualValue = (UnknownFudgeFieldValue) actualField.getValue();
        assertEquals(expectedField.getType().getTypeId(), actualField.getType().getTypeId());
        assertEquals(expectedValue.getType().getTypeId(), actualField.getType().getTypeId());
        FudgeUtils.assertArraysMatch(expectedValue.getContents(), actualValue.getContents());
      } else {
        assertEquals(expectedField.getValue(), actualField.getValue());
      }
    }
    assertFalse(actualIter.hasNext());
  }

  public static void assertArraysMatch(double[] expected, double[] actual) {
    assertEquals(expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      // No tolerance intentionally.
      assertEquals(expected[i],actual[i], 0.0);
    }
  }

  public static void assertArraysMatch(float[] expected, float[] actual) {
    assertEquals(expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      // No tolerance intentionally.
      assertEquals(expected[i],actual[i], 0.0);
    }
  }

  public static void assertArraysMatch(long[] expected, long[] actual) {
    assertEquals(expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      assertEquals(expected[i],actual[i]);
    }
  }

  public static void assertArraysMatch(int[] expected, int[] actual) {
    assertEquals(expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      assertEquals(expected[i],actual[i]);
    }
  }

  public static void assertArraysMatch(short[] expected, short[] actual) {
    assertEquals(expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      assertEquals(expected[i],actual[i]);
    }
  }

  public static void assertArraysMatch(byte[] expected, byte[] actual) {
    assertEquals(expected.length, actual.length);
    for(int i = 0; i < expected.length; i++) {
      assertEquals(expected[i],actual[i]);
    }
  }

}
