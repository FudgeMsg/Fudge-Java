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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFormatter;
import org.fudgemsg.taxon.FudgeTaxonomy;
import org.fudgemsg.taxon.MapFudgeTaxonomy;
import org.fudgemsg.taxon.ImmutableMapTaxonomyResolver;
import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class FudgeMsgFormatterTest {
  
  private static final FudgeContext s_fudgeContext = new FudgeContext();
  
  private static FudgeFieldContainer allNames () {
    MutableFudgeFieldContainer msg = StandardFudgeMessages.createMessageAllNames(s_fudgeContext);
    msg.add("Sub Message", 9999, StandardFudgeMessages.createMessageAllNames(s_fudgeContext));
    return msg;
  }
  
  private static FudgeFieldContainer allOrdinals () {
    MutableFudgeFieldContainer msg = StandardFudgeMessages.createMessageAllOrdinals(s_fudgeContext);
    msg.add("Sub Message", 9999, StandardFudgeMessages.createMessageAllOrdinals(s_fudgeContext));
    return msg;
  }
  
  private static FudgeTaxonomy getTaxonomy () {
    return new MapFudgeTaxonomy (
        new int[] { 1, 2, 3, 4, 5, 6 },
        new String[] { "boolean", "byte", "int", "string", "float", "double" }
        );
  }
  
  static {
    final Map<Short,FudgeTaxonomy> tr = new HashMap<Short,FudgeTaxonomy> ();
    tr.put ((short)1, getTaxonomy ());
    s_fudgeContext.setTaxonomyResolver (new ImmutableMapTaxonomyResolver (tr));
  }
  
  /**
   * Will output a {@link FudgeMsg} to {@code System.out} so that you can visually
   * examine it.
   */
  @Test
  public void outputToStdoutAllNames() {
    System.out.println("FudgeMsgFormatterTest.outputToStdoutAllNames()");
    (new FudgeMsgFormatter(new PrintWriter(System.out))).format(allNames ());
  }
  
  /**
   * Will output a {@link FudgeMsg} to {@code System.out} so that you can visually
   * examine it.
   */
  @Test
  public void outputToStdoutAllOrdinals() {
    System.out.println("FudgeMsgFormatterTest.outputToStdoutAllOrdinals()");
    (new FudgeMsgFormatter(new PrintWriter(System.out))).format(allOrdinals ());
  }

  @Test
  public void xmlStreamWriterAllNames () throws IOException, XMLStreamException {
    System.out.println("FudgeMsgFormatterTest.xmlStreamWriterAllNames()");
    final FudgeMsgWriter fmw = s_fudgeContext.allocateMessageWriter (new FudgeXMLStreamWriter (s_fudgeContext, new PrintWriter (System.out)));
    fmw.writeMessage (allNames (), 0);
    System.out.println ();
    fmw.writeMessage (allNames (), 1);
    fmw.close ();
  }
  
  @Test
  public void xmlStreamWriterAllOrdinals () throws IOException, XMLStreamException {
    System.out.println("FudgeMsgFormatterTest.xmlStreamWriterAllOrdinals()");
    final FudgeMsgWriter fmw = s_fudgeContext.allocateMessageWriter (new FudgeXMLStreamWriter (s_fudgeContext, new PrintWriter (System.out)));
    fmw.writeMessage (allOrdinals (), 0);
    System.out.println ();
    fmw.writeMessage (allOrdinals (), 1);
    fmw.close ();
  }
  
}
