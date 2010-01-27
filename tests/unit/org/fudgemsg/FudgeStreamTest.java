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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

/**
 * 
 *
 * @author Andrew
 */
public class FudgeStreamTest {
  
  private final FudgeContext _fudgeContext = new FudgeContext ();
  
  private FudgeFieldContainer simpleMessage (int n) {
    final MutableFudgeFieldContainer msg = _fudgeContext.newMessage ();
    msg.add ("n", (Integer)n);
    msg.add ("foo", (Integer)42);
    msg.add ("bar", "forty-two");
    return msg;
  }
  
  @Test
  public void readMultipleMessages () throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    final FudgeMsgStreamWriter writer = _fudgeContext.allocateMessageWriter (baos);
    writer.setDefaultTaxonomyId (0);
    writer.writeMessage (simpleMessage (1));
    writer.writeMessage (simpleMessage (2));
    writer.writeMessage (simpleMessage (3));
    _fudgeContext.releaseMessageWriter (writer);
    final ByteArrayInputStream bais = new ByteArrayInputStream (baos.toByteArray ());
    final FudgeMsgStreamReader reader = _fudgeContext.allocateMessageReader (bais);
    for (int i = 1; i <= 3; i++) {
      assert reader.hasNext ();
      final FudgeFieldContainer msg = reader.nextMessage ();
      assert msg.getInt ("n") == i;
    }
    assert !reader.hasNext ();
  }
  
}