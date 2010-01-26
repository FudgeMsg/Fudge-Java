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

package org.fudgemsg.mapping;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeMsg;
import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class CustomBuilderTest {
  
  private static class CustomClass {
    
    private final int _a, _b, _c;
    
    public CustomClass (int a, int b, int c) {
      _a = a;
      _b = b;
      _c = c;
    }
    
    public int getAB () {
      return _a + _b;
    }
    
    public int getBC () {
      return _b + _c;
    }
    
    public int getAC () {
      return _a + _c;
    }
    
    public boolean equals (final Object o) {
      if (o == null) return false;
      if (!(o instanceof CustomClass)) return false;
      final CustomClass c = (CustomClass)o;
      return (c._a == _a) && (c._b == _b) && (c._c == _c);
    }
    
  }
  
  private static class CustomBuilder implements FudgeBuilder<CustomClass> {

    @Override
    public FudgeMsg buildMessage(FudgeSerialisationContext context,
        CustomClass object) {
      final FudgeMsg msg = context.newMessage ();
      int a = (object.getAB () - object.getBC () + object.getAC ()) / 2;
      int b = object.getAB () - a;
      int c = object.getAC () - a;
      msg.add ("a", a);
      msg.add ("b", b);
      msg.add ("c", c);
      return msg;
    }

    @Override
    public CustomClass buildObject(FudgeDeserialisationContext context,
        FudgeFieldContainer message) {
      return new CustomClass (message.getInt ("a"), message.getInt ("b"), message.getInt ("c"));
    }
    
  }
  
  @Test
  public void withoutCustomBuilder () {
    final FudgeContext fudgeContext = new FudgeContext ();
    final FudgeDeserialisationContext deserialisationContext = new FudgeDeserialisationContext (fudgeContext);
    final CustomClass object = new CustomClass (2, 3, 5);
    final FudgeMsg msg = FudgeObjectMessageFactory.serializeToMessage (object, fudgeContext);
    assert msg.getInt ("aB") == object.getAB ();
    assert msg.getInt ("aC") == object.getAC ();
    assert msg.getInt ("bC") == object.getBC ();
    assert msg.getInt ("a") == null;
    assert msg.getInt ("b") == null;
    assert msg.getInt ("c") == null;
    try {
      deserialisationContext.fudgeMsgToObject (CustomClass.class, msg);
      assert false;
    } catch (FudgeRuntimeException e) {
      // correct behaviour - shouldn't be able to instantiate as there is no no-arg constructor
    }
  }
  
  @Test
  public void withCustomBuilder () {
    final FudgeContext fudgeContext = new FudgeContext ();
    final FudgeDeserialisationContext deserialisationContext = new FudgeDeserialisationContext (fudgeContext);
    fudgeContext.getObjectDictionary ().addBuilder (CustomClass.class, new CustomBuilder ());
    final CustomClass object = new CustomClass (2, 3, 5);
    final FudgeMsg msg = FudgeObjectMessageFactory.serializeToMessage (object, fudgeContext);
    assert msg.getInt ("aB") == null;
    assert msg.getInt ("aC") == null;
    assert msg.getInt ("bC") == null;
    assert msg.getInt ("a") == 2;
    assert msg.getInt ("b") == 3;
    assert msg.getInt ("c") == 5;
    final CustomClass object2 = deserialisationContext.fudgeMsgToObject (CustomClass.class, msg);
    assert object.equals (object2);
  }
  
}
