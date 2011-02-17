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
 * <p>An extension for {@link FudgeFieldContainer} that marks it as immutable. Although
 * the base interface has no mutator methods, an implementation may implement the
 * {@link MutableFudgeFieldContainer} extension or otherwise allow modification.</p>
 * 
 * <p>A container that implements this interface <b>must not</b> allow any modifications
 * to the fields within the container after construction.</p>
 * 
 * @author t0rx
 */
public interface ImmutableFudgeFieldContainer extends FudgeFieldContainer {
  
  // no methods; this is just a placeholder
  
}