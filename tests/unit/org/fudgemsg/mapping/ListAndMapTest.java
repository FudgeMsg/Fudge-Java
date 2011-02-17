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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.fudgemsg.FudgeContext;
import org.junit.Test;

/**
 * 
 */
public class ListAndMapTest {
  
  private Object cycleObject (final Object o) {
    return FudgeContext.GLOBAL_DEFAULT.fromFudgeMsg (FudgeContext.GLOBAL_DEFAULT.toFudgeMsg (o).getMessage ());
  }
  
  /**
   * 
   */
  @SuppressWarnings("unchecked")
  @Test
  public void test2DArray() {
    final double[][] a = new double[8][8];
    final Object o = cycleObject(a);
    assertTrue(o instanceof List);
    for (Object e : (List) o) {
      assertTrue(e instanceof double[]);
    }
  }

  /**
   * 
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testList () {
    final List<String> l = new ArrayList<String> (4);
    l.add ("hello");
    l.add ("world");
    l.add (null);
    l.add ("42");
    final Object o = cycleObject (l);
    assertNotNull (o);
    assertTrue (o instanceof List);
    final List<String> l2 = (List<String>)o;
    assertEquals (l.size (), l2.size ());
    for (int i = 0; i < l.size (); i++) {
      assertEquals (l.get (i), l2.get (i));
    }
  }
  
  /**
   * 
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testSet () {
    final Set<String> s = new TreeSet<String> ();
    s.add ("hello");
    s.add ("world");
    s.add ("foo");
    final Object o = cycleObject (s);
    assertNotNull (o);
    assertTrue (o instanceof Set);
    final Set<String> s2 = (Set<String>)o;
    assertEquals (s.size (), s2.size ());
    for (String e : s2) {
      assertTrue (s.contains (e));
    }
    for (String e : s) {
      assertTrue (s2.contains (e));
    }
  }
  
  /**
   * 
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testMap () {
    final Map<String,String> m = new HashMap<String,String> ();
    m.put ("hello", "world");
    m.put (null, "42");
    m.put ("42", null);
    final Object o = cycleObject (m);
    assertNotNull (o);
    assertTrue (o instanceof Map);
    final Map<String,String> m2 = (Map<String,String>)o;
    assertEquals (m.size (), m2.size ());
    for (Map.Entry<String,String> e : m.entrySet ()) {
      assertTrue (m2.containsKey (e.getKey ()));
      assertEquals (e.getValue (), m2.get (e.getKey ()));
    }
  }
  
}