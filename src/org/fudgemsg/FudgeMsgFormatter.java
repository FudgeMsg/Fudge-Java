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

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool to pretty-print a Fudge message.
 */
public class FudgeMsgFormatter {

  /**
   * The default indent (number of spaces)
   */
  public static final int DEFAULT_INDENT = 2;

  /**
   * The print writer to output to.
   */
  private final PrintWriter _writer;
  /**
   * The indent to use.
   */
  private final int _indent;
  /**
   * The text of the indent.
   */
  private final String _indentText;

  /**
   * Writes a Fudge message to {@code System.out}.
   * 
   * @param msg  the message to write, not null
   */
  public static void outputToSystemOut(FudgeFieldContainer msg) {
    new FudgeMsgFormatter(new PrintWriter(System.out)).format(msg);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a new pretty printer with a default indent.
   * 
   * @param writer  the target to write to, not null
   */
  public FudgeMsgFormatter(Writer writer) {
    this(writer, DEFAULT_INDENT);
  }

  /**
   * Creates a new pretty printer with a default indent.
   * 
   * @param writer  the target to write to, not null
   */
  public FudgeMsgFormatter(PrintWriter writer) {
    this(writer, DEFAULT_INDENT);
  }

  /**
   * Creates a new pretty printer with a specific indent.
   * 
   * @param writer  the target to write to, not null
   * @param indent  the number of spaces to use for indentation, not negative
   */
  public FudgeMsgFormatter(Writer writer, int indent) {
    this(new PrintWriter(writer), indent);
  }

  /**
   * Creates a new pretty printer with a specific indent.
   * 
   * @param writer  the target to write to, not null
   * @param indent  the number of spaces to use for indentation, not negative
   */
  public FudgeMsgFormatter(PrintWriter writer, int indent) {
    if (writer == null) {
      throw new NullPointerException("PrintWriter must not be null");
    }
    if (indent < 0) {
      throw new IllegalArgumentException("Indent must be zero or positive");
    }
    _writer = writer;
    _indent = indent;
    _indentText = composeIndentText(_indent);
  }

  /**
   * Creates an indentation string for the given level.
   *
   * @param indent  the indentation level, not negative
   * @return the string containing spaces, not null
   */
  private String composeIndentText(int indent) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      sb.append(" ");
    }
    return sb.toString();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the target to output to.
   * 
   * @return the writer, not null
   */
  public PrintWriter getWriter() {
    return _writer;
  }

  /**
   * Gets the size of the indent.
   * 
   * @return number of spaces, not negative
   */
  public int getIndent() {
    return _indent;
  }

  //-------------------------------------------------------------------------
  /**
   * Pretty-prints a Fudge message.
   * <p>
   * This outputs the message to the writer passed in the constructor.
   * 
   * @param msg  the message to write, null ignored
   */
  public void format(FudgeFieldContainer msg) {
    format(msg, 0);
  }

  /**
   * Pretty-prints a Fudge message with the given left hand indent.
   * <p>
   * This outputs the message to the writer passed in the constructor.
   * 
   * @param msg  the message to write, null ignored
   * @param depth indentation level
   */
  protected void format(FudgeFieldContainer msg, int depth) {
    if (msg == null) {
      return;
    }
    List<FudgeField> fields = msg.getAllFields();
    List<String> fieldSpecs = new ArrayList<String>(fields.size());
    int maxFieldSpecWidth = -1;
    int maxTypeNameWidth = -1;
    for (int i = 0; i < fields.size(); i++) {
      FudgeField field = fields.get(i);
      String fieldSpec = getFieldSpec(field, i, depth);
      maxFieldSpecWidth = Math.max(maxFieldSpecWidth, fieldSpec.length());
      maxTypeNameWidth = Math.max(maxTypeNameWidth, getTypeName(field.getType()).length());
      fieldSpecs.add(fieldSpec);
    }
    for (int i = 0; i < fields.size(); i++) {
      FudgeField field = fields.get(i);
      String fieldSpec = fieldSpecs.get(i);
      format(field, i, depth, fieldSpec, maxFieldSpecWidth, maxTypeNameWidth);
    }
  }

  /**
   * Calculates the length of a description (name + ordinal) of the field.
   * 
   * @param field  the field to describe, not null
   * @param index  the offset index of the field within the message
   * @param depth  the indentation level
   * @return the length in characters
   */
  protected int getFieldSpecWidth(FudgeField field, int index, int depth) {
    return getFieldSpec(field, index, depth).length();
  }

  /**
   * Lays out and writes a field description and value on a line to the output device.
   * 
   * @param field  the field to describe, not null
   * @param index  the offset index of the field within the message
   * @param depth  the indentation level
   * @param fieldSpec  the field description (name + ordinal)
   * @param maxFieldSpecWidth  the maximum length (in characters) of all {@code fieldSpec}s within the message
   * @param maxTypeNameWidth  the maximum length (in characters) of all type names within the message
   */
  protected void format(FudgeField field, int index, int depth, String fieldSpec, int maxFieldSpecWidth, int maxTypeNameWidth) {
    if (field == null) {
      throw new NullPointerException("FudgeField must not be null");
    }
    getWriter().print(fieldSpec);
    int nWritten = fieldSpec.length();
    int requiredSize = maxFieldSpecWidth + 1;
    for (int i = nWritten; i <= requiredSize; i++) {
      getWriter().print(' ');
      nWritten++;
    }
    String typeName = getTypeName(field.getType());
    getWriter().print(typeName);
    nWritten += typeName.length();
    requiredSize = requiredSize + maxTypeNameWidth + 1;
    for (int i = nWritten; i <= requiredSize; i++) {
      getWriter().print(' ');
      nWritten++;
    }
    if (field.getValue() instanceof FudgeMsg) {
      getWriter().println();
      FudgeMsg msgValue = (FudgeMsg) field.getValue();
      format(msgValue, depth + 1);
    } else {
      getWriter().print(field.getValue());
      getWriter().println();
    }
    getWriter().flush();
  }

  /**
   * Describes the fields name and ordinal.
   * 
   * @param field  the field to describe
   * @param index  the offset index into the containing message
   * @param depth  the indentation level
   * @return the description prefixed with the necessary indent
   */
  protected String getFieldSpec(FudgeField field, int index, int depth) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      sb.append(_indentText);
    }
    sb.append(index);
    sb.append("-");
    if (field.getOrdinal() != null) {
      sb.append("(").append(field.getOrdinal()).append(")");
      if (field.getName() != null) {
        sb.append(" ");
      }
    }
    if (field.getName() != null) {
      sb.append(field.getName());
    }
    return sb.toString();
  }

  /**
   * Creates a string type name for a Fudge type.
   * 
   * @param type the Fudge type
   * @return the type name, not null
   */
  protected String getTypeName(FudgeFieldType<?> type) {
    if (type == null) {
      throw new NullPointerException("FudgeFieldType must not be null");
    }
    return type.getJavaType().getSimpleName();
  }

}
