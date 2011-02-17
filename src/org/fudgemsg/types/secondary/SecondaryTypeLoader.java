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
package org.fudgemsg.types.secondary;

import java.util.ResourceBundle;

import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.types.SecondaryFieldTypeBase;

/**
 * Loads the secondary types from a resources file. The resources file must list entries
 * as <code><em>Java class</em> = <em>secondary type implementation</em></code>.
 * 
 * @author Andrew Griffin
 */
public class SecondaryTypeLoader {
  
  private SecondaryTypeLoader () {
  }
  
  /**
   * Loads the contents of {@code SecondaryTypeLoader.properties} and updates the supplied
   * type dictionary with the secondary type definitions found.
   * 
   * @param dictionary dictionary to update
   */
  public static void addTypes (final FudgeTypeDictionary dictionary) {
    final ResourceBundle genericBuilders = ResourceBundle.getBundle (SecondaryTypeLoader.class.getName ());
    for (final String key : genericBuilders.keySet ()) {
      final String secondaryFieldType = genericBuilders.getString (key);
      try {
        dictionary.addType ((SecondaryFieldTypeBase<?,?,?>)Class.forName (secondaryFieldType).getDeclaredField ("INSTANCE").get (null));
      } catch (ClassNotFoundException e) {
        throw new FudgeRuntimeException ("secondary type" + secondaryFieldType + " not found", e);
      } catch (NoClassDefFoundError e) {
        // ignore; a referenced class wasn't available (e.g. JSR-310)
      } catch (Exception e) {
        throw new FudgeRuntimeException ("couldn't register secondary type" + secondaryFieldType, e);
      }
    }
  }
  
}