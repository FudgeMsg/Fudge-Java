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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.fudgemsg.FudgeStreamReader.FudgeStreamElement;
import org.junit.Test;

/**
 * 
 *
 * @author Andrew Griffin
 */
public class FudgeStreamTest {
  
  private FudgeFieldContainer simpleMessage (int n) {
    final MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage ();
    msg.add ("n", (Integer)n);
    msg.add ("foo", (Integer)42);
    msg.add ("bar", "forty-two");
    return msg;
  }
  
  private InputStream prepareThreeMessageStream () {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    final FudgeMsgWriter writer = FudgeContext.GLOBAL_DEFAULT.createMessageWriter (baos);
    //writer.setDefaultTaxonomyId (0);
    writer.writeMessage (simpleMessage (1));
    writer.writeMessage (simpleMessage (2));
    writer.writeMessage (simpleMessage (3));
    return new ByteArrayInputStream (baos.toByteArray ());
  }
  
  /**
   * [documentation not available]
   */
  @Test
  public void readMultipleMessages () {
    final FudgeMsgReader reader = FudgeContext.GLOBAL_DEFAULT.createMessageReader (prepareThreeMessageStream ());
    for (int i = 1; i <= 3; i++) {
      assertTrue (reader.hasNext ());
      final FudgeFieldContainer msg = reader.nextMessage ();
      assertEquals (i, (int)msg.getInt ("n"));
    }
    assertFalse (reader.hasNext ());
  }
  
  private void readMultipleMessagesUnderlying (final InputStream in) {
    final FudgeStreamReader reader = FudgeContext.GLOBAL_DEFAULT.createReader (in);
    FudgeStreamElement element;
    for (int i = 1; i <= 3; i++) {
      assertTrue (reader.hasNext ());
      element = reader.next ();
      assertEquals (FudgeStreamElement.MESSAGE_ENVELOPE, element);
      for (int j = 1; j <= 3; j++) {
        assertTrue (reader.hasNext ());
        reader.next ();
      }
      assertFalse (reader.hasNext ());
    }
    assertTrue (reader.hasNext ());
    element = reader.next ();
    assertNull (element);
  }
  
  /**
   * [FRJ-66] EOF at a message boundary
   */
  @Test
  public void readMultipleMessagesUnderlyingCorrectly () {
    readMultipleMessagesUnderlying (prepareThreeMessageStream ());
  }
  
  /**
   * [FRJ-66] throw an EOF midway through an envelope
   */
  @Test(expected=FudgeRuntimeIOException.class)
  public void readMultipleMessagesUnderlyingErroring () {
    final InputStream in = prepareThreeMessageStream ();
    readMultipleMessagesUnderlying (new InputStream () {
      private int count = 0;
      public int read () throws IOException {
        if (count++ < 40) {
          return in.read ();
        } else {
          throw new EOFException ();
        }
      }
    });
  }
  
  /**
   * FRJ-67
   */
  @Test
  public void flushingBufferTest () {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    final BufferedOutputStream bout = new BufferedOutputStream (baos, 4096); // big enough to not flush automatically 
    final FudgeMsgWriter writer = FudgeContext.GLOBAL_DEFAULT.createMessageWriter (bout);
    writer.writeMessage (simpleMessage (1));
    // stream should have flushed automatically
    int messageSize = baos.size ();
    assertTrue (messageSize > 0);
    writer.writeMessage (simpleMessage (2));
    // and again
    assertTrue (baos.size () > messageSize);
    messageSize = baos.size ();
    // reset the stream and turn off the default automatic flushing
    baos.reset ();
    assertTrue (baos.size () == 0);
    final FudgeStreamWriter underlyingWriter = writer.getStreamWriter ();
    assertTrue (underlyingWriter instanceof FudgeDataOutputStreamWriter);
    ((FudgeDataOutputStreamWriter)underlyingWriter).setFlushOnEnvelopeComplete (false);
    writer.writeMessage (simpleMessage (1));
    // stream should not have flushed automatically
    assertTrue (baos.size () == 0);
    writer.writeMessage (simpleMessage (2));
    assertTrue (baos.size () == 0);
    writer.flush ();
    // now it should have done
    assertEquals (messageSize, baos.size ());
  }
  
}