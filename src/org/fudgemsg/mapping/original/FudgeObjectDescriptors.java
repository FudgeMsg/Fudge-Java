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

package org.fudgemsg.mapping.original;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


// REVIEW kirk 2009-11-12 -- This is a candidate for moving up to the FudgeContext,
// so that it can be smarter about type mapping.
/**
 * Merely holds the {@link ObjectDescriptor}s for the current classloader in a
 * convenient object to be shared by both {@link FudgeObjectStreamReader} and
 * {@link FudgeObjectStreamWriter}.
 *
 * @author kirk
 */
@Deprecated
public final class FudgeObjectDescriptors {
  public static final FudgeObjectDescriptors INSTANCE = new FudgeObjectDescriptors();
  private final ConcurrentMap<Class<?>, ObjectDescriptor> _descriptors;
  
  private FudgeObjectDescriptors() {
    _descriptors = new ConcurrentHashMap<Class<?>, ObjectDescriptor>();
  }

  public ObjectDescriptor getDescriptor(Class<?> clazz) {
    ObjectDescriptor objectDescriptor = _descriptors.get(clazz);
    if(objectDescriptor == null) {
      ObjectDescriptor freshDescriptor = new ObjectDescriptor(clazz);
      objectDescriptor = _descriptors.putIfAbsent(clazz, freshDescriptor);
      if(objectDescriptor == null) {
        objectDescriptor = freshDescriptor;
      }
    }
    assert objectDescriptor != null;
    return objectDescriptor;
  }

}
