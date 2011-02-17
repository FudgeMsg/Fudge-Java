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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.taxon.ImmutableMapTaxonomyResolver;
import org.fudgemsg.taxon.MapFudgeTaxonomy;
import org.fudgemsg.xml.FudgeXMLStreamWriter;
import org.junit.Test;

/**
 * 
 *
 * @author Andrew
 */
public class FudgeXMLTest {
  
  private final FudgeContext _fudgeContext;

  private static FudgeTaxonomy getTaxonomy () {
    return new MapFudgeTaxonomy (
        new int[] { 1, 2, 3, 4, 5, 6 },
        new String[] { "boolean", "byte", "int", "string", "float", "double" }
        );
  }
  
  /**
   * 
   */
  public FudgeXMLTest () {
    _fudgeContext = new FudgeContext ();
    final Map<Short,FudgeTaxonomy> tr = new HashMap<Short,FudgeTaxonomy> ();
    tr.put ((short)1, getTaxonomy ());
    _fudgeContext.setTaxonomyResolver (new ImmutableMapTaxonomyResolver (tr));
  }
  
  private void xmlTest (final FudgeFieldContainer message, final int taxonomy) {
    final FudgeMsgWriter fmw = new FudgeMsgWriter (new FudgeXMLStreamWriter (_fudgeContext, new PrintWriter (System.out)));
    fmw.writeMessage (message, taxonomy);
    fmw.flush ();
    System.out.println ();
  }
  
  /**
   */
  @Test
  public void xmlStreamWriterAllNamesNoTaxonomy () {
    xmlTest (StandardFudgeMessages.createMessageAllNames (_fudgeContext), 0);
  }
  
  /**
   */
  @Test
  public void xmlStreamWriterAllNamesTaxonomy () {
    xmlTest (StandardFudgeMessages.createMessageAllNames (_fudgeContext), 1);
  }
  
  /**
   */
  @Test
  public void xmlStreamWriterAllOrdinalsNoTaxonomy () {
    xmlTest (StandardFudgeMessages.createMessageAllOrdinals (_fudgeContext), 0);
  }
  
  /**
   */
  @Test
  public void xmlStreamWriterAllOrdinalsTaxonomy () {
    xmlTest (StandardFudgeMessages.createMessageAllOrdinals (_fudgeContext), 1);
  }
  
  /**
   */
  @Test
  public void xmlStreamWriterWithSubMsgsNoTaxonomy () {
    xmlTest (StandardFudgeMessages.createMessageWithSubMsgs (_fudgeContext), 0);
  }
  
  /**
   */
  @Test
  public void xmlStreamWriterWithSubMsgsTaxonomy () {
    xmlTest (StandardFudgeMessages.createMessageWithSubMsgs (_fudgeContext), 1);
  }
  
}