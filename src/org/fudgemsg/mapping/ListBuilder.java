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

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;

/**
 * Builder for List objects.
 * 
 * @author Andrew
 */
/* package */ class ListBuilder implements FudgeBuilder<List<?>> {
  
  /* package */ static final ListBuilder INSTANCE = new ListBuilder (); 
  
  private ListBuilder () {
  }

  @Override
  public MutableFudgeFieldContainer buildMessage (FudgeSerializationContext context, List<?> list) {
    final MutableFudgeFieldContainer msg = context.newMessage ();
    for (Object entry : list) {
      context.objectToFudgeMsg (msg, null, null, entry);
    }
    return msg;
  }
  
  @Override
  public List<?> buildObject (FudgeDeserializationContext context, FudgeFieldContainer message) {
    final List<Object> list = new ArrayList<Object> ();
    for (FudgeField field : message) {
      if ((field.getOrdinal () != null) && (field.getOrdinal () != 1)) throw new FudgeRuntimeException ("Sub-message doesn't contain a list (bad field " + field + ")");
      list.add (context.fieldValueToObject (field));
    }
    return list;
  }

}