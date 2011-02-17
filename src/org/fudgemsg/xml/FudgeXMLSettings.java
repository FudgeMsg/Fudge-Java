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

package org.fudgemsg.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeTypeDictionary;

/**
 * Base class to hold common data for the Fudge XML reader and
 * writer as defined by the specification that can be customized for a
 * specific XML source or destination. The default settings are based on
 * <a href="http://wiki.fudgemsg.org/display/FDG/XML+Fudge+Messages">XML Fudge Message specification</a>.
 * 
 * @author Andrew Griffin
 */
public class FudgeXMLSettings {
  
  // TODO change the way this works to the approach used in the JSON package. I think I prefer that.
  
  /**
   * Attributes available for the envelope element.
   */
  protected static enum XMLEnvelopeAttribute {
    /**
     * The processing directives value.
     */
    PROCESSINGDIRECTIVES,
    /**
     * The schema version.
     */
    SCHEMAVERSION,
    /**
     * The taxonomy references.
     */
    TAXONOMY;
  }
  
  /**
   * Attributes available for a field element.
   */
  protected static enum XMLFieldAttribute {
    /**
     * The field name.
     */
    NAME,
    /**
     * The field ordinal index.
     */
    ORDINAL,
    /**
     * The field type.
     */
    TYPE,
    /**
     * The field encoding.
     */
    ENCODING;
  }
  
  /**
   * Default element name for the outer envelope tag. 
   */
  public static final String DEFAULT_ENVELOPE_ELEMENT = "fudgeEnvelope";
  
  /**
   * Default attribute name for the processing directives on the envelope.
   */
  public static final String DEFAULT_ENVELOPE_ATTRIBUTE_PROCESSINGDIRECTIVES = "processingDirectives";
  
  /**
   * Default attribute name for the schema version on the envelope.
   */
  public static final String DEFAULT_ENVELOPE_ATTRIBUTE_SCHEMAVERSION = "schemaVersion";
  
  /**
   * Default attribute name for the taxonomy on the envelope.
   */
  public static final String DEFAULT_ENVELOPE_ATTRIBUTE_TAXONOMY = "taxonomy";
  
  /**
   * Default element name for an anonymous or unnamed field.
   */
  public static final String DEFAULT_FIELD_ELEMENT = "fudgeField";
  
  /**
   * Default attribute name for the name of a field.
   */
  public static final String DEFAULT_FIELD_ATTRIBUTE_NAME = "name";
  
  /**
   * Default attribute name for the ordinal index of a field.
   */
  public static final String DEFAULT_FIELD_ATTRIBUTE_ORDINAL = "ordinal";
  
  /**
   * Alternative attribute name (not written but recognized by default) for the ordinal index of a field. 
   */
  public static final String ALIAS_FIELD_ATTRIBUTE_ORDINAL_INDEX = "index";
  
  /**
   * Alternative attribute name (not written but recognized by default) for the ordinal index of a field.
   */
  public static final String ALIAS_FIELD_ATTRIBUTE_ORDINAL_KEY = "key";
  
  /**
   * Default attribute name for the type of a field.
   */
  public static final String DEFAULT_FIELD_ATTRIBUTE_TYPE = "type";
  
  /**
   * Default attribute name for the encoding of a field.
   */
  public static final String DEFAULT_FIELD_ATTRIBUTE_ENCODING = "encoding";
  
  /**
   * Default value for a {@code true} boolean field.
   */
  public static final String DEFAULT_BOOLEAN_TRUE = "true";
  
  /**
   * Alternative value (not written but recognized by default) for a {@code true} boolean field.
   */
  public static final String ALIAS_BOOLEAN_TRUE_ON = "on";
  
  /**
   * Alternative value (not written but recognized by default) for a {@code true} boolean field.
   */
  public static final String ALIAS_BOOLEAN_TRUE_T = "T";
  
  /**
   * Alternative value (not written but recognized by default) for a {@code true} boolean field.
   */
  public static final String ALIAS_BOOLEAN_TRUE_1 = "1";
  
  /**
   * Default value for a {@code false} boolean field.
   */
  public static final String DEFAULT_BOOLEAN_FALSE = "false";
  
  /**
   * Alternative value (not written but recognized by default) for a {@code false} boolean field.
   */
  public static final String ALIAS_BOOLEAN_FALSE_OFF = "off";
  
  /**
   * Alternative value (not written but recognized by default) for a {@code false} boolean field.
   */
  public static final String ALIAS_BOOLEAN_FALSE_F = "F";
  
  /**
   * Alternative value (not written but recognized by default) for a {@code false} boolean field.
   */
  public static final String ALIAS_BOOLEAN_FALSE_0 = "0";
  
  /**
   * Default value for base-64 encoded data.
   */
  public static final String DEFAULT_ENCODING_BASE64 = "base64";
  
  private String _envelopeElementName;
  private final Set<String> _envelopeElementAliases = new HashSet<String> ();
  private String _fieldElementName;
  private final Set<String> _fieldElementAliases = new HashSet<String> ();
  
  private final Map<String,XMLEnvelopeAttribute> _namesToEnvelopeAttribute = new HashMap<String,XMLEnvelopeAttribute> ();
  private final Map<XMLEnvelopeAttribute,String> _envelopeAttributesToName = new HashMap<XMLEnvelopeAttribute,String> ();
  
  private final Map<String,XMLFieldAttribute> _namesToFieldAttribute = new HashMap<String,XMLFieldAttribute> ();
  private final Map<XMLFieldAttribute,String> _fieldAttributesToName = new HashMap<XMLFieldAttribute,String> ();
  
  private final Map<Integer,String> _fudgeTypesToIdentifier = new HashMap<Integer,String> ();
  private final Map<String,Integer> _identifiersToFudgeType = new HashMap<String,Integer> ();
  
  private boolean _preserveFieldNames = true;
  private boolean _appendFieldOrdinal = true;
  private boolean _base64UnknownTypes = false;
  
  private final Map<String,Boolean> _stringsToBoolean = new HashMap<String,Boolean> ();
  private String _booleanTrue;
  private String _booleanFalse;
  
  private String _base64EncodingName;
  private final Set<String> _base64EncodingAliases = new HashSet<String> ();
  
  /**
   * Creates a new settings object with all of the defaults.
   */
  public FudgeXMLSettings () {
    setEnvelopeElementName (DEFAULT_ENVELOPE_ELEMENT);
    setEnvelopeAttributeProcessingDirectives (DEFAULT_ENVELOPE_ATTRIBUTE_PROCESSINGDIRECTIVES);
    setEnvelopeAttributeSchemaVersion (DEFAULT_ENVELOPE_ATTRIBUTE_SCHEMAVERSION);
    setEnvelopeAttributeTaxonomy (DEFAULT_ENVELOPE_ATTRIBUTE_TAXONOMY);
    setFieldElementName (DEFAULT_FIELD_ELEMENT);
    setFieldAttributeName (DEFAULT_FIELD_ATTRIBUTE_NAME);
    setFieldAttributeOrdinal (DEFAULT_FIELD_ATTRIBUTE_ORDINAL);
    addFieldAttributeOrdinalAlias (ALIAS_FIELD_ATTRIBUTE_ORDINAL_INDEX);
    addFieldAttributeOrdinalAlias (ALIAS_FIELD_ATTRIBUTE_ORDINAL_KEY);
    setFieldAttributeType (DEFAULT_FIELD_ATTRIBUTE_TYPE);
    setFieldAttributeEncoding (DEFAULT_FIELD_ATTRIBUTE_ENCODING);
    setBooleanTrue (DEFAULT_BOOLEAN_TRUE);
    addBooleanTrueAlias (ALIAS_BOOLEAN_TRUE_ON);
    addBooleanTrueAlias (ALIAS_BOOLEAN_TRUE_T);
    addBooleanTrueAlias (ALIAS_BOOLEAN_TRUE_1);
    setBooleanFalse (DEFAULT_BOOLEAN_FALSE);
    addBooleanFalseAlias (ALIAS_BOOLEAN_FALSE_OFF);
    addBooleanFalseAlias (ALIAS_BOOLEAN_FALSE_F);
    addBooleanFalseAlias (ALIAS_BOOLEAN_FALSE_0);
    registerFudgeType (FudgeTypeDictionary.INDICATOR_TYPE_ID, null, "indicator");
    registerFudgeType (FudgeTypeDictionary.BOOLEAN_TYPE_ID, null, "boolean", "bool");
    registerFudgeType (FudgeTypeDictionary.BYTE_TYPE_ID, null, "byte", "int8");
    registerFudgeType (FudgeTypeDictionary.SHORT_TYPE_ID, null, "short", "int16");
    registerFudgeType (FudgeTypeDictionary.INT_TYPE_ID, null, "int", "int32");
    registerFudgeType (FudgeTypeDictionary.LONG_TYPE_ID, null, "long", "int64");
    registerFudgeType (FudgeTypeDictionary.BYTE_ARRAY_TYPE_ID, "byte[]");
    registerFudgeType (FudgeTypeDictionary.SHORT_ARRAY_TYPE_ID, "short[]");
    registerFudgeType (FudgeTypeDictionary.INT_ARRAY_TYPE_ID, "int[]");
    registerFudgeType (FudgeTypeDictionary.LONG_ARRAY_TYPE_ID, "long[]");
    registerFudgeType (FudgeTypeDictionary.FLOAT_TYPE_ID, null, "float");
    registerFudgeType (FudgeTypeDictionary.DOUBLE_TYPE_ID, null, "double");
    registerFudgeType (FudgeTypeDictionary.FLOAT_ARRAY_TYPE_ID, "float[]");
    registerFudgeType (FudgeTypeDictionary.DOUBLE_ARRAY_TYPE_ID, "double[]");
    registerFudgeType (FudgeTypeDictionary.STRING_TYPE_ID, null, "string");
    registerFudgeType (FudgeTypeDictionary.FUDGE_MSG_TYPE_ID, null, "message");
    registerFudgeType (FudgeTypeDictionary.BYTE_ARR_4_TYPE_ID, "byte[4]");
    registerFudgeType (FudgeTypeDictionary.BYTE_ARR_8_TYPE_ID, "byte[8]");
    registerFudgeType (FudgeTypeDictionary.BYTE_ARR_16_TYPE_ID, "byte[16]");
    registerFudgeType (FudgeTypeDictionary.BYTE_ARR_20_TYPE_ID, "byte[20]");
    registerFudgeType (FudgeTypeDictionary.BYTE_ARR_32_TYPE_ID, "byte[32]");
    registerFudgeType (FudgeTypeDictionary.BYTE_ARR_64_TYPE_ID, "byte[64]");
    registerFudgeType (FudgeTypeDictionary.BYTE_ARR_128_TYPE_ID, "byte[128]");
    registerFudgeType (FudgeTypeDictionary.BYTE_ARR_256_TYPE_ID, "byte[256]");
    registerFudgeType (FudgeTypeDictionary.BYTE_ARR_512_TYPE_ID, "byte[512]");
    registerFudgeType (FudgeTypeDictionary.DATE_TYPE_ID, "date");
    registerFudgeType (FudgeTypeDictionary.TIME_TYPE_ID, "time");
    registerFudgeType (FudgeTypeDictionary.DATETIME_TYPE_ID, "datetime");
    setBase64EncodingName (DEFAULT_ENCODING_BASE64);
  }
  
  /**
   * Creates a new settings object as a copy of another. After copying, changes can be made to the
   * new instance without affecting the other.
   * 
   * @param other an existing settings object to copy
   */
  public FudgeXMLSettings (final FudgeXMLSettings other) {
    if (other == null) throw new NullPointerException ("other cannot be null");
    setEnvelopeElementName (other.getEnvelopeElementName ());
    getEnvelopeElementAliases ().addAll (other.getEnvelopeElementAliases ());
    setFieldElementName (other.getFieldElementName ());
    getFieldElementAliases ().addAll (other.getFieldElementAliases ());
    getNamesToEnvelopeAttribute ().putAll (other.getNamesToEnvelopeAttribute ());
    getEnvelopeAttributesToName ().putAll (other.getEnvelopeAttributesToName ());
    getNamesToFieldAttribute ().putAll (other.getNamesToFieldAttribute ());
    getFieldAttributesToName ().putAll (other.getFieldAttributesToName ());
    getFudgeTypesToIdentifier ().putAll (other.getFudgeTypesToIdentifier ());
    getIdentifiersToFudgeType ().putAll (other.getIdentifiersToFudgeType ());
    setPreserveFieldNames (other.getPreserveFieldNames ());
    setAppendFieldOrdinal (other.getAppendFieldOrdinal ());
    setBase64UnknownTypes (other.getBase64UnknownTypes ());
    getBase64EncodingAliases ().addAll (other.getBase64EncodingAliases ());
  }
  
  /**
   * @return the envelopeElementAliases
   */
  protected Set<String> getEnvelopeElementAliases() {
    return _envelopeElementAliases;
  }

  /**
   * @return the fieldElementAliases
   */
  protected Set<String> getFieldElementAliases() {
    return _fieldElementAliases;
  }

  /**
   * @return the namesToEnvelopeAttribute
   */
  protected Map<String, XMLEnvelopeAttribute> getNamesToEnvelopeAttribute() {
    return _namesToEnvelopeAttribute;
  }

  /**
   * @return the envelopeAttributesToName
   */
  protected Map<XMLEnvelopeAttribute, String> getEnvelopeAttributesToName() {
    return _envelopeAttributesToName;
  }

  /**
   * @return the namesToFieldAttribute
   */
  protected Map<String, XMLFieldAttribute> getNamesToFieldAttribute() {
    return _namesToFieldAttribute;
  }

  /**
   * @return the fieldAttributesToName
   */
  protected Map<XMLFieldAttribute, String> getFieldAttributesToName() {
    return _fieldAttributesToName;
  }
  
  /**
   * Sets the name of the envelope element. Other names (aliases) may be recognized when parsing, but this value will always be used when writing. Set to
   * {@code null} to disable reading/writing of an envelope element.
   * 
   * @param envelopeElementName the name of the element
   */
  public void setEnvelopeElementName(String envelopeElementName) {
    _envelopeElementName = envelopeElementName;
    addEnvelopeElementAlias (envelopeElementName);
  }

  /**
   * Returns the main name for the envelope element. Other names (aliases) may be be recognized when parsing, this value will always be used when writing. Returns {@code null}
   * if an envelope element should not be written or expected in the source XML. 
   * 
   * @return the name of the element
   */
  public String getEnvelopeElementName() {
    return _envelopeElementName;
  }

  /**
   * Adds an additional alias to recognize as an envelope element when parsing XML input.
   * 
   * @param envelopeElementName the name of the element
   */
  public void addEnvelopeElementAlias (final String envelopeElementName) {
    if (envelopeElementName != null) {
      getEnvelopeElementAliases ().add (envelopeElementName);
    }
  }
  
  /**
   * Removes any aliases added by {@link #addEnvelopeElementAlias}. Only the main element name as returned by {@link #getEnvelopeElementName} will be recognized
   * when parsing XML input.
   */
  public void clearEnvelopeElementAliases () {
    getEnvelopeElementAliases ().clear ();
    addEnvelopeElementAlias (getEnvelopeElementName ());
  }  
  
  /**
   * Sets the name of the field element for unnamed fields. Other names (aliases) may be recognized when parsing, but this value will always be used when writing.
   * Set to {@code null} to omit unnamed fields from the XML output, or to treat all field names literally when parsing XML input.
   * 
   * @param fieldElementName the name of the element
   */
  public void setFieldElementName(String fieldElementName) {
    _fieldElementName = fieldElementName;
    addFieldElementAlias (fieldElementName);
  }

  /**
   * Returns the element name to use for unnamed fields. Returns {@code null} if unnamed fields shouldn't be written, or not processed when parsing XML input. 
   * 
   * @return the name of the element
   */
  public String getFieldElementName() {
    return _fieldElementName;
  }

  /**
   * Adds an additional alias to recognize as an unnamed field element when parsing XML input.
   * 
   * @param fieldElementName the name of the element
   */
  public void addFieldElementAlias (final String fieldElementName) {
    if (fieldElementName != null) {
      getFieldElementAliases ().add (fieldElementName);
    }
  }
  
  /**
   * Removes any aliases added by {@link #addFieldElementAlias}. Only the main element name as returned by {@link #getFieldElementName} will be recognized when parsing XML input.
   */
  public void clearFieldElementAliases () {
    getFieldElementAliases ().clear ();
    addFieldElementAlias (getFieldElementName ());
  }
  
  /**
   * Sets the attribute name to use for processing directives in the envelope element. Set to {@code null} to omit processing directives when writing XML, or ignore in XML input.
   * 
   * @param processingDirectivesAttributeName name of the attribute 
   */
  public void setEnvelopeAttributeProcessingDirectives (final String processingDirectivesAttributeName) {
    setEnvelopeAttribute (XMLEnvelopeAttribute.PROCESSINGDIRECTIVES, processingDirectivesAttributeName);
  }
  
  /**
   * Sets the attribute name to use for the schema version in the envelope element. Set to {@code null} to omit schema version when writing XML, or ignore in XML input.
   * 
   * @param schemaVersionAttributeName name of the attribute
   */
  public void setEnvelopeAttributeSchemaVersion (final String schemaVersionAttributeName) {
    setEnvelopeAttribute (XMLEnvelopeAttribute.SCHEMAVERSION, schemaVersionAttributeName);
  }
  
  /**
   * Sets the attribute name to use for the taxonomy in the envelope element. Set to {@code null} to omit taxonomy information when writing XML, or ignore in XML input.
   * 
   * @param taxonomyAttributeName name of the attribute
   */
  public void setEnvelopeAttributeTaxonomy (final String taxonomyAttributeName) {
    setEnvelopeAttribute (XMLEnvelopeAttribute.TAXONOMY, taxonomyAttributeName);
  }
  
  /**
   * @param attribute
   * @param attributeName
   */
  protected void setEnvelopeAttribute (final XMLEnvelopeAttribute attribute, final String attributeName) {
    getEnvelopeAttributesToName ().put (attribute, attributeName);
    addEnvelopeAttributeAlias (attribute, attributeName);
  }
  
  /**
   * Returns the name to use for the processing directives attribute in an envelope element. Returns {@code null} if processing directives should be omitted from output, or ignored in XML input.
   * 
   * @return the name of the attribute
   */
  public String getEnvelopeAttributeProcessingDirectives () {
    return getEnvelopeAttribute (XMLEnvelopeAttribute.PROCESSINGDIRECTIVES);
  }
  
  /**
   * Returns the name to use for the schema version attribute in an envelope element. Returns {@code null} if schema version should be omitted from output, or ignored in XML input.
   * 
   * @return the name of the attribute
   */
  public String getEnvelopeAttributeSchemaVersion () {
    return getEnvelopeAttribute (XMLEnvelopeAttribute.SCHEMAVERSION);
  }
  
  /**
   * Returns the name to use for the taxonomy attribute in an envelope element. Returns {@code null} if taxonomy should be omitted from output, or ignored in XML input.
   * 
   * @return the name of the attribute
   */
  public String getEnvelopeAttributeTaxonomy () {
    return getEnvelopeAttribute (XMLEnvelopeAttribute.TAXONOMY);
  }
  
  /**
   * @param attribute
   * @return
   */
  protected String getEnvelopeAttribute (final XMLEnvelopeAttribute attribute) {
    return getEnvelopeAttributesToName ().get (attribute);
  }
  
  /**
   * Adds an additional alias to recognize as a processing directives attribute in an envelope element.
   * 
   * @param processingDirectivesAttributeName the name of the attribute
   */
  public void addEnvelopeAttributeProcessingDirectivesAlias (final String processingDirectivesAttributeName) {
    addEnvelopeAttributeAlias (XMLEnvelopeAttribute.PROCESSINGDIRECTIVES, processingDirectivesAttributeName);
  }
  
  /**
   * Adds an additional alias to recognize as a schema version attribute in an envelope element.
   * 
   * @param schemaVersionAttributeName the name of the attribute
   */
  public void addEnvelopeAttributeSchemaVersionAlias (final String schemaVersionAttributeName) {
    addEnvelopeAttributeAlias (XMLEnvelopeAttribute.SCHEMAVERSION, schemaVersionAttributeName);
  }
  
  /**
   * Adds an additional alias to recognize as a taxonomy attribute in an envelope element.
   * 
   * @param taxonomyAttributeName the name of the attribute
   */
  public void addEnvelopeAttributeTaxonomyAlias (final String taxonomyAttributeName) {
    addEnvelopeAttributeAlias (XMLEnvelopeAttribute.TAXONOMY, taxonomyAttributeName);
  }
  
  /**
   * @param attribute
   * @param attributeName
   */
  protected void addEnvelopeAttributeAlias (final XMLEnvelopeAttribute attribute, final String attributeName) {
    if (attributeName != null) {
      getNamesToEnvelopeAttribute ().put (attributeName, attribute);
    }
  }
  
  /**
   * Clears the set of aliases recognized for the processing directives attribute in an envelope element. Only the main attribute name is recognized afterwards.
   */
  public void clearEnvelopeAttributeProcessingDirectivesAliases () {
    clearEnvelopeAttributeAliases (XMLEnvelopeAttribute.PROCESSINGDIRECTIVES);
  }
  
  /**
   * Clears the set of aliases recognized for the schema version attribute in an envelope element. Only the main attribute name is recognized afterwards.
   */
  public void clearEnvelopeAttributeSchemaVersionAliases () {
    clearEnvelopeAttributeAliases (XMLEnvelopeAttribute.SCHEMAVERSION);
  }
  
  /**
   * Clears the set of aliases recognized for the taxonomy attribute in an envelope element. Only the main attribute name is recognized afterwards.
   */
  public void clearEnvelopeAttributeTaxonomyAliases () {
    clearEnvelopeAttributeAliases (XMLEnvelopeAttribute.TAXONOMY);
  }
  
  /**
   * @param attribute
   */
  protected void clearEnvelopeAttributeAliases (final XMLEnvelopeAttribute attribute) {
    final String preserve = getEnvelopeAttribute (attribute);
    final Iterator<Map.Entry<String,XMLEnvelopeAttribute>> iterator = getNamesToEnvelopeAttribute ().entrySet ().iterator ();
    while (iterator.hasNext ()) {
      final Map.Entry<String,XMLEnvelopeAttribute> entry = iterator.next ();
      if (entry.getValue () == attribute) {
        if ((preserve == null) || !preserve.equals (entry.getKey ())) {
          iterator.remove ();
        }
      }
    }
  }
  
  /**
   * Sets the name of the field name attribute on a field element. Set to {@code null} to omit the attribute from output or ignore in XML input.
   * 
   * @param nameAttributeName name of the attribute
   */
  public void setFieldAttributeName (final String nameAttributeName) {
    setFieldAttribute (XMLFieldAttribute.NAME, nameAttributeName);
  }
  
  /**
   * Sets the name of the field ordinal index attribute on a field element. Set to {@code null} to omit the attribute from output, or ignore in XML input.
   * 
   * @param ordinalAttributeName name of the attribute
   */
  public void setFieldAttributeOrdinal (final String ordinalAttributeName) {
    setFieldAttribute (XMLFieldAttribute.ORDINAL, ordinalAttributeName);
  }
  
  /**
   * Sets the name of the type attribute on a field element. Set to {@code null} to omit the attribute from output, or ignore in XML input.
   * 
   * @param typeAttributeName name of the attribute
   */
  public void setFieldAttributeType (final String typeAttributeName) {
    setFieldAttribute (XMLFieldAttribute.TYPE, typeAttributeName);
  }
  
  /**
   * Sets the name of the encoding attribute on a field element. Set to {@code null} to omit the attribute from output, or ignore in XML input.
   * 
   * @param encodingAttributeName name of the attribute
   */
  public void setFieldAttributeEncoding (final String encodingAttributeName) {
    setFieldAttribute (XMLFieldAttribute.ENCODING, encodingAttributeName);
  }
  
  /**
   * @param attribute
   * @param attributeName
   */
  protected void setFieldAttribute (XMLFieldAttribute attribute, final String attributeName) {
    getFieldAttributesToName ().put (attribute, attributeName);
    addFieldAttributeAlias (attribute, attributeName);
  }
  
  /**
   * Returns the name of the field name attribute in a field element. Returns {@code null} if the name should be omitted from output or ignored in XML input.
   * 
   * @return name of the attribute
   */
  public String getFieldAttributeName () {
    return getFieldAttribute (XMLFieldAttribute.NAME);
  }
  
  /**
   * Returns the name of the field ordinal attribute in a field element. Returns {@code null} if the ordinal should be omitted from output, or ignored in XML input.
   * 
   * @return name of the attribute
   */
  public String getFieldAttributeOrdinal () {
    return getFieldAttribute (XMLFieldAttribute.ORDINAL);
  }
  
  /**
   * Returns the name of the type attribute in a field element. Returns {@code null} if the type should be omitted from output, or ignored in XML input.
   * 
   * @return name of the attribute
   */
  public String getFieldAttributeType () {
    return getFieldAttribute (XMLFieldAttribute.TYPE);
  }
  
  /**
   * Returns the name of the encoding attribute of a field element. Returns {@code null} if the encoding should be omitted from output, or ignored in XML input.
   * 
   * @return name of the attribute
   */
  public String getFieldAttributeEncoding () {
    return getFieldAttribute (XMLFieldAttribute.ENCODING);
  }
  
  /**
   * @param attribute
   * @return
   */
  protected String getFieldAttribute (final XMLFieldAttribute attribute) {
    return getFieldAttributesToName ().get (attribute);
  }
  
  /**
   * Adds an additional alias to recognize as a field name attribute of a field element when parsing XML input.
   * 
   * @param nameAttributeName name of the attribute
   */
  public void addFieldAttributeNameAlias (final String nameAttributeName) {
    addFieldAttributeAlias (XMLFieldAttribute.NAME, nameAttributeName);
  }
  
  /**
   * Adds an additional alias to recognize as a field ordinal attribute when parsing XML input.
   * 
   * @param ordinalAttributeName name of the attribute
   */
  public void addFieldAttributeOrdinalAlias (final String ordinalAttributeName) {
    addFieldAttributeAlias (XMLFieldAttribute.ORDINAL, ordinalAttributeName);
  }
  
  /**
   * Adds an additional alias to recognize as a field type attribute when parsing XML input.
   * 
   * @param typeAttributeName name of the attribute
   */
  public void addFieldAttributeTypeAlias (final String typeAttributeName) {
    addFieldAttributeAlias (XMLFieldAttribute.TYPE, typeAttributeName);
  }
  
  /**
   * Adds an additional alias to recognize as an encoding attribute when parsing XML input.
   * 
   * @param encodingAttributeName name of the attribute
   */
  public void addFieldAttributeEncodingAlias (final String encodingAttributeName) {
    addFieldAttributeAlias (XMLFieldAttribute.ENCODING, encodingAttributeName);
  }
  
  /**
   * @param attribute
   * @param attributeName
   */
  protected void addFieldAttributeAlias (final XMLFieldAttribute attribute, final String attributeName) {
    if (attributeName != null) {
      getNamesToFieldAttribute ().put (attributeName, attribute);
    }
  }
  
  /**
   * Clears the aliases for field name attributes set by {@link #addFieldAttributeNameAlias}.
   */
  public void clearFieldAttributeNameAliases () {
    clearFieldAttributeAliases (XMLFieldAttribute.NAME);
  }
  
  /**
   * Clears the aliases for field ordinal attributes set by {@link #addFieldAttributeOrdinalAlias}.
   */
  public void clearFieldAttributeOrdinalAliases () {
    clearFieldAttributeAliases (XMLFieldAttribute.ORDINAL);
  }
  
  /**
   * Clears the aliases for field type attributes set by {@link #addFieldAttributeTypeAlias}.
   */
  public void clearFieldAttributeTypeAliases () {
    clearFieldAttributeAliases (XMLFieldAttribute.TYPE);
  }
  
  /**
   * Clears the aliases for field encoding attributes set by {@link #addFieldAttributeEncodingAlias}.
   */
  public void clearFieldAttributeEncodingAliases () {
    clearFieldAttributeAliases (XMLFieldAttribute.ENCODING);
  }
  
  /**
   * @param attribute
   */
  protected void clearFieldAttributeAliases (final XMLFieldAttribute attribute) {
    final String preserve = getFieldAttribute (attribute);
    final Iterator<Map.Entry<String,XMLFieldAttribute>> iterator = getNamesToFieldAttribute ().entrySet ().iterator ();
    while (iterator.hasNext ()) {
      final Map.Entry<String,XMLFieldAttribute> entry = iterator.next ();
      if (entry.getValue () == attribute) {
        if ((preserve == null) || !preserve.equals (entry.getKey ())) {
          iterator.remove ();
        }
      }
    }
  }
  
  /**
   * Set to use the field name as the element name when writing fields, or derive the field name from the element name when reading.
   * 
   * @param preserveFieldNames {@code true} to map the field name to/from the element name
   */
  public void setPreserveFieldNames (final boolean preserveFieldNames) {
    _preserveFieldNames = preserveFieldNames;
  }
  
  /**
   * Returns whether to map the field name to/from the element name.
   */
  public boolean getPreserveFieldNames () {
    return _preserveFieldNames;
  }
  
  /**
   * Sets the value to use for encoding a boolean {@code true} value, or to expect when decoding XML input.
   * 
   * @param trueValue the XML content
   */
  public void setBooleanTrue (final String trueValue) {
    if (trueValue == null) throw new NullPointerException ("trueValue cannot be null");
    _booleanTrue = trueValue;
    addBooleanTrueAlias (trueValue);
  }
  
  /**
   * Returns the value to use for encoding a boolean {@code true} value, or to expect when decoding XML input.
   */
  public String getBooleanTrue () {
    return _booleanTrue;
  }
  
  /**
   * Adds an additional alias to recognize as a boolean {@code true} when decoding XML input.
   */
  public void addBooleanTrueAlias (final String trueValue) {
    addBooleanAlias (true, trueValue);
  }
  
  /**
   * Removes any aliases registered with {@link #addBooleanTrueAlias}.
   */
  public void clearBooleanTrueAliases () {
    clearBooleanAliases (true);
  }
  
  /**
   * Sets the value to use for encoding a boolean {@code false} value, or to expect when decoding XML input.
   * 
   * @param falseValue the XML content
   */
  public void setBooleanFalse (final String falseValue) {
    if (falseValue == null) throw new NullPointerException ("falseValue cannot be null");
    _booleanFalse = falseValue;
    addBooleanFalseAlias (falseValue);
  }
  
  /**
   * Returns the value to use for encoding a boolean {@code false} value, or to expect when decoding XML input.
   */
  public String getBooleanFalse () {
    return _booleanFalse;
  }
  
  /**
   * Adds an additional alias to recognize as a boolean {@code false} value when decoding XML input.
   */
  public void addBooleanFalseAlias (final String falseValue) {
    addBooleanAlias (false, falseValue);
  }
  
  /**
   * Removes any aliases registered with {@link #addBooleanFalseAlias}.
   */
  public void clearBooleanFalseAliases () {
    clearBooleanAliases (false);
  }
  
  /**
   * @param value
   * @param alias
   */
  protected void addBooleanAlias (final boolean value, final String alias) {
    if (alias == null) throw new NullPointerException ("alias cannot be null");
    getStringsToBoolean ().put (alias.toLowerCase (), value);
  }
  
  /**
   * @param value
   */
  protected void clearBooleanAliases (final Boolean value) {
    final String preserve = (value ? getBooleanTrue () : getBooleanFalse ()).toLowerCase ();
    final Iterator<Map.Entry<String,Boolean>> iterator = getStringsToBoolean ().entrySet ().iterator ();
    while (iterator.hasNext ()) {
      final Map.Entry<String,Boolean> entry = iterator.next ();
      if (entry.getValue () == value) {
        if (!preserve.equals (entry.getKey ())) {
          iterator.remove ();
        }
      }
    }
  }
  
  /**
   * @return
   */
  protected Map<String,Boolean> getStringsToBoolean () {
    return _stringsToBoolean;
  }
  
  /**
   * Sets the value to use, or expect, on an encoding attribute if the field data is written in Base-64.
   */
  public void setBase64EncodingName (final String value) {
    if (value == null) throw new NullPointerException ("value cannot be null");
    _base64EncodingName = value;
    addBase64EncodingAlias (value);
  }
  
  /**
   * Returns the value to use, or expect, on an encoding attribute if the field data is written in Base-64. 
   */
  public String getBase64EncodingName () {
    return _base64EncodingName;
  }
  
  /**
   * Adds an additional alias to recognize as a Base-64 encoding when reading XML input.
   */
  public void addBase64EncodingAlias (final String value) {
    if (value == null) throw new NullPointerException ("value cannot be null");
    getBase64EncodingAliases ().add (value);
  }
  
  /**
   * @return
   */
  protected Set<String> getBase64EncodingAliases () {
    return _base64EncodingAliases;
  }
  
  /**
   * Returns true if the data for unknown types should be encoded in Base-64.
   */
  public boolean getBase64UnknownTypes () {
    return _base64UnknownTypes;
  }
  
  /**
   * Sets whether to encode unknown types in Base-64.
   */
  public void setBase64UnknownTypes (final boolean base64UnknownTypes) {
    _base64UnknownTypes = base64UnknownTypes;
  }
  
  /**
   * @return
   */
  protected Map<String,Integer> getIdentifiersToFudgeType () {
    return _identifiersToFudgeType;
  }
  
  /**
   * @return
   */
  protected Map<Integer,String> getFudgeTypesToIdentifier () {
    return _fudgeTypesToIdentifier;
  }
  
  /**
   * @param type
   * @param identifiers
   */
  protected void registerFudgeType (final int type, final String ... identifiers) {
    getFudgeTypesToIdentifier ().put (type, (identifiers[0] == null) ? "" : identifiers[0]);
    for (String identifier : identifiers) {
      if (identifier != null) {
        getIdentifiersToFudgeType ().put (identifier.toLowerCase (), type);
      }
    }
  }
  
  /**
   * Returns a type string for a Fudge type identifier.
   */
  public String fudgeTypeIdToString (final int type) {
    String s = getFudgeTypesToIdentifier ().get (type);
    return (s == null) ? Integer.toString (type) : ((s.length () == 0) ? null : s);
  }
  
  /**
   * Returns a Fudge type identifier for a given string.
   */
  public Integer stringToFudgeTypeId (final String str) {
    return getIdentifiersToFudgeType ().get (str.toLowerCase ());
  }

  /**
   * Returns {@code true} if the field ordinal should be appended to the field name when generating field elements.
   */
  public boolean getAppendFieldOrdinal () {
    return _appendFieldOrdinal;
  }
  
  /**
   * Sets whether to append the field ordinal to the default field name when generating field elements.
   */
  public void setAppendFieldOrdinal (final boolean appendFieldOrdinal) {
    _appendFieldOrdinal = appendFieldOrdinal;
  }
  
}