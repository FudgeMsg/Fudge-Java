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

/**
 * A factory for Fudge messages.
 * <p>
 * This offers the minimal API necessary to create new Fudge messages.
 * Use of this interface avoids exposing additional methods and knowledge to other APIs.
 */
public interface FudgeMessageFactory {

  /**
   * Creates an initially empty message.
   * 
   * @return the empty message container, not null
   */
  public MutableFudgeFieldContainer newMessage();

  /**
   * Creates a new message initially populated with the supplied message.
   * 
   * @param fromMessage  the source message to copy fields from, not null
   * @return the new message container, not null
   */
  public MutableFudgeFieldContainer newMessage(FudgeFieldContainer fromMessage);

}
