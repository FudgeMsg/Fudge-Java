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
package org.fudgemsg.taxon;

/**
 * A Fudge taxonomy providing the connection between a name and an ordinal.
 * <p>
 * Each field in a Fudge message may contain a name and an ordinal.
 * All four combinations of name and ordinal are possible - from both present to both absent.
 * <p>
 * The purpose of the ordinal is primarily to be a shorthand 2-byte key to a full name.
 * The connection between the ordinal and the name is given by the taxonomy.
 * <p>
 * The key point is that the taxonomy is dumb.
 * If a taxonomy is used in a complex message, then field names would be replaced by ordinals.
 * Consider a large message that contains the field name 'foo'.
 * The name 'foo' occurs multiple times in the overall message within two types of sub-message - 'bar and 'baz'.
 * The meaning of 'foo' is thus different depending on whether it is in 'bar' or 'baz'.
 * When a taxonomy is used, the name 'foo' is replaced by the same ordinal in both types of message.
 * Similarly, the ordinal is replaced by the name 'foo' when read back in.
 * This means that the ordinal has no unique meaning.
 * 'foo' in 'bar' and 'foo' in 'baz' are the same.
 * <p>
 * A taxonomy may be obtained from a {@link TaxonomyResolver}.
 * <p>
 * This interface should be implemented with care to ensure Fudge operates correctly.
 * Implementations must be thread-safe and should probably be immutable.
 */
public interface FudgeTaxonomy {

  /**
   * Looks up the field name for the given ordinal.
   * <p>
   * Not all ordinals will necessarily be mapped to a name.
   * This must be implemented to be bidirectional with {@link #getFieldOrdinal(String)}.
   * 
   * @param ordinal  the ordinal to lookup
   * @return the field name, null if the ordinal is not mapped to a name
   */
  String getFieldName(short ordinal);

  /**
   * Looks up the field ordinal for the given name.
   * <p>
   * Not all names will necessarily be mapped to an ordinal.
   * This must be implemented to be bidirectional with {@link #getFieldName(short)}.
   * 
   * @param name  the name to lookup, null returns null
   * @return the field ordinal, null if the name is not mapped to a ordinal
   */
  Short getFieldOrdinal(String name);

}
