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
package org.fudgemsg;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.taxon.MapFudgeTaxonomy;
import org.junit.Test;

/**
 * This saves (and subsequently reloads) data files containing the message representation of taxonomy objects.
 *
 * @author Andrew
 */
public class TaxonomyMessageTest {
  
  private static final String FIELD_1 = "id";
  private static final String FIELD_2 = "name";
  private static final String FIELD_3 = "email";
  private static final String FIELD_42 = "foo";
  
  private static MapFudgeTaxonomy testTaxonomyInstance () {
    final Map<Integer,String> namesByOrdinal = new HashMap<Integer,String> ();
    namesByOrdinal.put (1, FIELD_1);
    namesByOrdinal.put (2, FIELD_2);
    namesByOrdinal.put (3, FIELD_3);
    namesByOrdinal.put (42, FIELD_42);
    return new MapFudgeTaxonomy (namesByOrdinal);
  }
  
  private void testTaxonomy (final FudgeTaxonomy taxon) {
    assertEquals (taxon.getFieldName ((short)1), FIELD_1);
    assertEquals (taxon.getFieldName ((short)2), FIELD_2);
    assertEquals (taxon.getFieldName ((short)3), FIELD_3);
    assertEquals (taxon.getFieldName ((short)42), FIELD_42);
    assertEquals (taxon.getFieldOrdinal (FIELD_1), (Short)(short)1);
    assertEquals (taxon.getFieldOrdinal (FIELD_2), (Short)(short)2);
    assertEquals (taxon.getFieldOrdinal (FIELD_3), (Short)(short)3);
    assertEquals (taxon.getFieldOrdinal (FIELD_42), (Short)(short)42);
  }

  /**
   * 
   */
  @Test
  public void encodeDecodeCycle () {
    final FudgeTaxonomy taxon = MapFudgeTaxonomy.fromFudgeMsg (testTaxonomyInstance ().toFudgeMsg (new FudgeContext ()));
    testTaxonomy (taxon);
  }
  
  /**
   * 
   */
  @Test
  public void messageLoadingTest () {
    final FudgeTaxonomy taxon = MapFudgeTaxonomy.fromFudgeMsg (StandardMessageLoadingTest.loadMessage (new FudgeContext (), "taxonomy.dat").getMessage ());
    testTaxonomy (taxon);
  }
  
  /**
   * 
   */
  @Test
  public void messageRewritingTest () {
    final FudgeFieldContainer reference = testTaxonomyInstance ().toFudgeMsg (new FudgeContext ());
    StandardMessageRewritingTest.testFile (reference, "taxonomy.dat");
  }
  
}
