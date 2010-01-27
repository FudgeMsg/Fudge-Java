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

/**
 * Constructs instances of {@link FudgeMsg} from any Java object by parsing
 * its fields.
 *
 * @author kirk
 */
public class FudgeObjectMessageFactory {

  /**
   * @param obj
   * @param descriptor 
   * @return
   */
  public static <T> FudgeFieldContainer serializeToMessage(T obj, FudgeContext context) {
    return serializeToMessage (obj, new FudgeSerialisationContext (context));
  }
  
  @SuppressWarnings("unchecked")
  public static <T> FudgeFieldContainer serializeToMessage (T obj, FudgeSerialisationContext context) {
    final FudgeMessageBuilder<T> builder = context.getFudgeContext ().getObjectDictionary ().getMessageBuilder ((Class<T>)obj.getClass ());
    return builder.buildMessage (context, obj);
  }
  
}
