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
 * A container of Fudge fields that should be effectively immutable.
 * <p>
 * The Fudge specification is built around messages containing a list of fields.
 * This interface provides the high-level representation of the list of fields.
 * <p>
 * Each field may be referenced by a name or by an ordinal.
 * All four combinations are possible - from both present to both absent.
 * Methods provide the ability to lookup a field by both name or ordinal.
 * <p>
 * Applications working with messages should use this interface rather than
 * {@link FudgeMsg} directly where possible for flexibility.
 * <p>
 * This interface intends implementations to be immutable however it should be
 * thought of as read-only following construction.
 */
public interface ImmutableFudgeFieldContainer extends FudgeFieldContainer {

  // no additional methods

}
