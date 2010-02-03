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

import org.fudgemsg.FudgeStreamReader.FudgeStreamElement;
import org.fudgemsg.types.FudgeMsgFieldType;

/**
 * A reader for returning whole Fudge messages ({@link FudgeFieldContainer} instances) from an underlying {@link FudgeStreamReader} instance.
 * This implementation constructs the whole Fudge message in memory before returning to the caller. This is provided for convenience - greater
 * runtime efficiency may be possible by working directly with the {@link FudgeStreamReader} to process stream elements as they are decoded.
 * 
 * @author Andrew
 */
public class FudgeMsgReader {
  
  /**
   * The underlying source of Fudge elements.
   */
  private final FudgeStreamReader _streamReader;
  
  /**
   * A read-ahead buffer used to implement the {@link #hasNext()} method. Used in conjunction with {@link #_streamErrored} to identify EOF on
   * the underlying source.
   */
  private FudgeMsgEnvelope _readAhead = null;
  
  /**
   * Set to true after the stream has errored during a read-ahead to identify the EOF condition on the underlying source.
   */
  private boolean _streamErrored = false;

  /**
   * Creates a new {@link FudgeMsgReader} around an existing {@link FudgeStreamReader}.
   * 
   * @param streamReader the source of Fudge stream elements to read
   */
  public FudgeMsgReader (final FudgeStreamReader streamReader) {
    //System.out.println ("FudgeMessageStreamReader::FudgeMessageStreamReader(" + streamReader + ")");
    if (streamReader == null) {
      throw new NullPointerException ("streamReader cannot be null");
    }
    _streamReader = streamReader;
  }
  
  /**
   * Closes this {@link FudgeMsgReader} and the underlying {@link FudgeStreamReader}.
   */
  public void close () {
    //System.out.println ("FudgeMessageStreamReader::close()");
    if (_streamReader == null) return;
    _streamReader.close ();
    _readAhead = null;
    _streamErrored = false;
  }
  
  /**
   * Returns the {@link FudgeContext} associated with the underlying source.
   * 
   * @return the {@code FudgeContext}
   */
  public FudgeContext getFudgeContext () {
    final FudgeStreamReader reader = getStreamReader ();
    if (reader == null) return null;
    return reader.getFudgeContext ();
  }
  
  protected FudgeStreamReader getStreamReader () {
    return _streamReader;
  }
  
  /**
   * Returns true if there are more messages to read from the underlying source.
   * 
   * @return {@code true} if {@link #nextMessage()} or {@link #nextMessageEnvelope()} will return data
   */
  public boolean hasNext () {
    //System.out.println ("FudgeMessageStreamReader::hasNext()");
    if (_streamErrored) return false;
    if (_readAhead != null) return true;
    try {
      _readAhead = readMessageEnvelope ();
      return (_readAhead != null);
    } catch (IOException ioe) {
      _streamErrored = true;
      return false;
    }
  }
  
  /**
   * Reads the next message, discarding the envelope.
   * 
   * @return the message read without the envelope
   * @throws IOException if the underlying source errors
   */
  public FudgeFieldContainer nextMessage () throws IOException {
    //System.out.println ("FudgeMessageStreamReader::nextMessage()");
    final FudgeMsgEnvelope msgEnv = nextMessageEnvelope ();
    if (msgEnv == null) return null;
    return msgEnv.getMessage ();
  }
  
  /**
   * Reads the next message, returning the envelope.
   * 
   * @return the {@link FudgeMsgEnvelope}
   * @throws IOException if the underlying source errors
   */
  public FudgeMsgEnvelope nextMessageEnvelope () throws IOException {
    //System.out.println ("FudgeMessageStreamReader::nextMessageEnvelope()");
    if (_readAhead != null) {
      FudgeMsgEnvelope envelope = _readAhead;
      _readAhead = null;
      return envelope;
    }
    return readMessageEnvelope ();
  }
  
  protected FudgeMsgEnvelope readMessageEnvelope () throws IOException {
    //System.out.println ("FudgeMessageStreamReader::readMessageEnvelope()");
    final FudgeStreamReader reader = getStreamReader ();
    FudgeStreamElement element = reader.next();
    if(element == null) {
      return null;
    }
    if(element != FudgeStreamElement.MESSAGE_ENVELOPE) {
      throw new IllegalArgumentException("First element in encoding stream wasn't a message element.");
    }
    int version = reader.getSchemaVersion();
    MutableFudgeFieldContainer msg = getFudgeContext().newMessage();
    FudgeMsgEnvelope envelope = new FudgeMsgEnvelope (msg, version);
    processFields(reader, msg);
    return envelope;
  }
  
  protected void processFields(FudgeStreamReader reader, MutableFudgeFieldContainer msg) throws IOException {
    //System.out.println ("FudgeMessageStreamReader::processFields(" + reader + ", " + msg + ")");
    while(reader.hasNext()) {
      FudgeStreamElement element = reader.next();
      switch(element) {
      case SIMPLE_FIELD:
        msg.add(reader.getFieldName(), reader.getFieldOrdinal(), reader.getFieldType(), reader.getFieldValue());
        break;
      case SUBMESSAGE_FIELD_START:
        MutableFudgeFieldContainer subMsg = getFudgeContext().newMessage ();
        msg.add(reader.getFieldName(), reader.getFieldOrdinal(), FudgeMsgFieldType.INSTANCE, subMsg);
        processFields(reader, subMsg);
        break;
      case SUBMESSAGE_FIELD_END:
        return;
      }
    }
  }
  
}