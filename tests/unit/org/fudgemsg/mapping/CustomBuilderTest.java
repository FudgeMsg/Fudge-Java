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
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;
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
    public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context,
        CustomClass object) {
      final MutableFudgeFieldContainer msg = context.newMessage ();
      int a = (object.getAB () - object.getBC () + object.getAC ()) / 2;
      int b = object.getAB () - a;
      int c = object.getAC () - a;
      msg.add ("a", a);
      msg.add ("b", b);
      msg.add ("c", c);
      return msg;
    }

    @Override
    public CustomClass buildObject(FudgeDeserializationContext context,
        FudgeFieldContainer message) {
      return new CustomClass (message.getInt ("a"), message.getInt ("b"), message.getInt ("c"));
    }
    
  }
  
  /**
   * 
   */
  @Test
  public void withoutCustomBuilder () {
    final FudgeContext fudgeContext = new FudgeContext ();
    final FudgeDeserializationContext deserialisationContext = new FudgeDeserializationContext (fudgeContext);
    final CustomClass object = new CustomClass (2, 3, 5);
    final FudgeFieldContainer msg = FudgeObjectMessageFactory.serializeToMessage (object, fudgeContext);
    assert msg.getInt ("AB") == object.getAB ();
    assert msg.getInt ("AC") == object.getAC ();
    assert msg.getInt ("BC") == object.getBC ();
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
  
  /**
   * 
   */
  @Test
  public void withCustomBuilder () {
    final FudgeContext fudgeContext = new FudgeContext ();
    final FudgeDeserializationContext deserialisationContext = new FudgeDeserializationContext (fudgeContext);
    fudgeContext.getObjectDictionary ().addBuilder (CustomClass.class, new CustomBuilder ());
    final CustomClass object = new CustomClass (2, 3, 5);
    final FudgeFieldContainer msg = FudgeObjectMessageFactory.serializeToMessage (object, fudgeContext);
    assert msg.getInt ("AB") == null;
    assert msg.getInt ("AC") == null;
    assert msg.getInt ("BC") == null;
    assert msg.getInt ("a") == 2;
    assert msg.getInt ("b") == 3;
    assert msg.getInt ("c") == 5;
    final CustomClass object2 = deserialisationContext.fudgeMsgToObject (CustomClass.class, msg);
    assert object.equals (object2);
  }
  
  private interface FooInterface {
    public String foo ();
  }
  
  private static class FooHorse implements FooInterface {
    public static class Builder implements FudgeBuilder<FooHorse> {
      @Override
      public MutableFudgeFieldContainer buildMessage(
          FudgeSerializationContext context, FooHorse object) {
        final MutableFudgeFieldContainer msg = context.newMessage ();
        msg.add (0, FooHorse.class.getName ());
        msg.add (1, "gibberish");
        return msg;
      }
      @Override
      public FooHorse buildObject(FudgeDeserializationContext context,
          FudgeFieldContainer message) {
        assert message.getString (1).equals ("gibberish");
        return new FooHorse ();
      }
    }
    public String foo () { return "horse"; }
    public boolean equals (final Object o) {
      return (o != null) && (o instanceof FooHorse);
    }
  }
  
  private static class FooCow implements FooInterface {
    public static class Builder implements FudgeBuilder<FooCow> {
      @Override
      public MutableFudgeFieldContainer buildMessage(
          FudgeSerializationContext context, FooCow object) {
        final MutableFudgeFieldContainer msg = context.newMessage ();
        msg.add (0, FooCow.class.getName ());
        msg.add ("gibberish", 1);
        return msg;
      }
      @Override
      public FooCow buildObject(FudgeDeserializationContext context,
          FudgeFieldContainer message) {
        assert message.getInt ("gibberish") == 1;
        return new FooCow ();
      }
    }
    public String foo () { return "cow"; }
    public boolean equals (final Object o) {
      return (o != null) && (o instanceof FooCow);
    }
  }
  
  /**
   * 
   *
   * @author andrew
   */
  public static class BeanClass {
    private String _bar;
    /**
     * @param bar [documentation not available]
     */
    public void setBar (final String bar) {
      _bar = bar;
    }
    /**
     * @return [documentation not available]
     */
    public String getBar () {
      return _bar;
    }
    /**
     * @param o [documentation not available]
     * @return [documentation not available]
     */
    public boolean equals (final Object o) {
      if (o == null) return false;
      if (!(o instanceof BeanClass)) return false;
      final BeanClass bc = (BeanClass)o;
      return _bar.equals (bc._bar);
    }
  }
  
  /**
   * 
   *
   * @author andrew
   */
  public static class ProtoMessage {
    
    private final FooInterface _foo;
    private final BeanClass _bar;
    private final int _n;
    
    /**
     * @param foo [documentation not available]
     * @param bar [documentation not available]
     * @param n [documentation not available]
     */
    ProtoMessage (FooInterface foo, BeanClass bar, int n) {
      _foo = foo;
      _bar = bar;
      _n = n;
    }
    
    /**
     * @param context [documentation not available]
     * @return [documentation not available]
     */
    public FudgeFieldContainer toFudgeMsg (FudgeSerializationContext context) {
      MutableFudgeFieldContainer msg = context.newMessage ();
      msg.add ("foo", context.objectToFudgeMsg (_foo));
      msg.add ("bar", context.objectToFudgeMsg (_bar));
      msg.add ("n", _n);
      return msg;
    }
    
    /**
     * @param context [documentation not available]
     * @param fields [documentation not available]
     * @return [documentation not available]
     */
    public static ProtoMessage fromFudgeMsg (FudgeDeserializationContext context, FudgeFieldContainer fields) {
      final FooInterface foo = context.fudgeMsgToObject (FooInterface.class, fields.getMessage ("foo"));
      final BeanClass bar = context.fudgeMsgToObject (BeanClass.class, fields.getMessage ("bar"));
      int n = fields.getInt ("n");
      return new ProtoMessage (foo, bar, n);
    }
    
    /**
     * {@docInherit}
     */
    @Override
    public boolean equals (final Object o) {
      if (o == null) return false;
      if (!(o instanceof ProtoMessage)) return false;
      final ProtoMessage pm = (ProtoMessage)o;
      return _foo.equals (pm._foo) && _bar.equals (pm._bar) && (_n == pm._n);
    }
    
  }
  
  private void subclassBuilder (final FudgeContext fc) {
    BeanClass bc1 = new BeanClass ();
    bc1.setBar ("one");
    final ProtoMessage pmHorse = new ProtoMessage (new FooHorse (), bc1, 1);
    final FudgeFieldContainer ffcHorse = FudgeObjectMessageFactory.serializeToMessage (pmHorse, fc);
    System.out.println (ffcHorse);
    BeanClass bc2 = new BeanClass ();
    bc2.setBar ("two");
    final ProtoMessage pmCow = new ProtoMessage (new FooCow (), bc2, 2);
    final FudgeFieldContainer ffcCow = FudgeObjectMessageFactory.serializeToMessage (pmCow, fc);
    System.out.println (ffcCow);
    final ProtoMessage pmHorse2 = FudgeObjectMessageFactory.deserializeToObject (ProtoMessage.class, ffcHorse, fc);
    final ProtoMessage pmCow2 = FudgeObjectMessageFactory.deserializeToObject (ProtoMessage.class, ffcCow, fc);
    assert pmHorse2.equals (pmHorse);
    assert pmCow2.equals (pmCow);
  }
  
  /**
   * 
   */
  @Test
  public void subclassBuilderTest () {
    final FudgeContext fc = new FudgeContext ();
    // the defaults should fail because of the interface
    try {
      subclassBuilder (fc);
      assert false;
    } catch (FudgeRuntimeException fre) {
      final String expectedMessage = "Don't know how to create interface " + FooInterface.class.getName ();
      if (!fre.getCause ().getCause ().getMessage ().substring (0, expectedMessage.length ()).equals (expectedMessage)) {
        fre.printStackTrace ();
        assert false;
      }
    }
    // a custom builder for our implementation should fix it
    fc.getObjectDictionary ().addBuilder (FooHorse.class, new FooHorse.Builder ());
    fc.getObjectDictionary ().addBuilder (FooCow.class, new FooCow.Builder ());
    subclassBuilder (fc);
  }
  
}
