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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * <p>Converts between Java objects and {@link FudgeFieldContainer} messages using the Fudge serialisation
 * framework. This class is provided for convenience, direct use of a {@link FudgeSerializationContext} or {@link FudgeDeserializationContext}
 * will be more efficient.</p>
 * 
 * <p>This has been deprecated since 0.3, to be removed at the 1.0 release; a couple of utility methods have
 * gone into the {@link FudgeContext} to support this.</p>
 *
 * @author Kirk Wylie
 */
@Deprecated
public class FudgeObjectMessageFactory {
  
  /**
   * Serialises a Java object to a {@link FudgeFieldContainer} message. Use {@link FudgeContext#toFudgeMsg} instead.
   * 
   * @param <T> Java type
   * @param obj object to serialise
   * @param context the {@link FudgeContext} to use
   * @return the serialised message
   */
  @Deprecated
  public static <T> MutableFudgeFieldContainer serializeToMessage(T obj, FudgeContext context) {
    final FudgeSerializationContext fsc = new FudgeSerializationContext (context);
    final MutableFudgeFieldContainer message = fsc.objectToFudgeMsg(obj);
    if (!(obj instanceof List<?>) && !(obj instanceof Set<?>) && !(obj instanceof Map<?, ?>)) {
      FudgeSerializationContext.addClassHeader(message, obj.getClass());
    }
    return message;
  }
  
  /**
   * Deserializes a {@link FudgeFieldContainer} message to a Java object, trying to determine the 
   * type of the object automatically. Use {@link FudgeContext#fromFudgeMsg(FudgeFieldContainer)} instead.
   * 
   * @param message the Fudge message to deserialize
   * @param context the {@link FudgeContext} to use
   * @return the deserialized object
   */
  @Deprecated
  public static Object deserializeToObject (FudgeFieldContainer message, FudgeContext context) {
    final FudgeDeserializationContext fdc = new FudgeDeserializationContext (context);
    return fdc.fudgeMsgToObject (message);
  }
  
  /**
   * Deserializes a {@link FudgeFieldContainer} message to a Java object of type {@code clazz}. Use {@link FudgeContext#fromFudgeMsg(Class,FudgeFieldContainer)} instead.
   * 
   * @param <T> Java type
   * @param clazz the target type to deserialise
   * @param message the message to process
   * @param context the underlying {@link FudgeContext} to use
   * @return the deserialised object
   */
  @Deprecated
  public static <T> T deserializeToObject (Class<T> clazz, FudgeFieldContainer message, FudgeContext context) {
    final FudgeDeserializationContext fdc = new FudgeDeserializationContext (context);
    return fdc.fudgeMsgToObject (clazz, message);
  }
  
}
