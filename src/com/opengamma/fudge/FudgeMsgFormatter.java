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
package com.opengamma.fudge;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows for pretty-printing of {@link FudgeMsg} instances.
 *
 * @author kirk
 */
public class FudgeMsgFormatter {
  public static final int DEFAULT_INDENT = 2;
  private final PrintWriter _writer;
  private final int _indent;
  private final String _indentText;
  
  public FudgeMsgFormatter(Writer writer) {
    this(writer, DEFAULT_INDENT);
  }
  
  public FudgeMsgFormatter(PrintWriter printWriter) {
    this(printWriter, DEFAULT_INDENT);
  }
  
  public FudgeMsgFormatter(Writer writer, int indent) {
    this(new PrintWriter(writer), indent);
  }
  
  public FudgeMsgFormatter(PrintWriter writer, int indent) {
    if(writer == null) {
      throw new NullPointerException("Must specify a valid writer for output.");
    }
    if(indent < 0) {
      throw new IllegalArgumentException("Indent must not be negative.");
    }
    _writer = writer;
    _indent = indent;
    _indentText = composeIndentText(_indent);
  }
  
  public static void outputToSystemOut(FudgeFieldContainer msg) {
    (new FudgeMsgFormatter(new PrintWriter(System.out))).format(msg);
  }

  /**
   * @return the writer
   */
  public PrintWriter getWriter() {
    return _writer;
  }

  /**
   * @return the indent
   */
  public int getIndent() {
    return _indent;
  }

  public void format(FudgeFieldContainer msg) {
    format(msg, 0);
  }
  
  protected void format(FudgeFieldContainer msg, int depth) {
    if(msg == null) {
      return;
    }
    List<FudgeField> fields = msg.getAllFields();
    List<String> fieldSpecs = new ArrayList<String>(fields.size());
    int maxFieldSpecWidth = -1;
    int maxTypeNameWidth = -1;
    for(int i = 0; i < fields.size(); i++) {
      FudgeField field = fields.get(i);
      String fieldSpec = getFieldSpec(field, i, depth);
      maxFieldSpecWidth = Math.max(maxFieldSpecWidth, fieldSpec.length());
      maxTypeNameWidth = Math.max(maxTypeNameWidth, getTypeName(field.getType()).length());
      fieldSpecs.add(fieldSpec);
    }
    for(int i = 0; i < fields.size(); i++) {
      FudgeField field = fields.get(i);
      String fieldSpec = fieldSpecs.get(i);
      format(field, i, depth, fieldSpec, maxFieldSpecWidth, maxTypeNameWidth);
    }
  }
  
  protected int getFieldSpecWidth(FudgeField field, int index, int depth) {
    return getFieldSpec(field, index, depth).length();
  }

  protected void format(FudgeField field, int index, int depth, String fieldSpec, int maxFieldSpecWidth, int maxTypeNameWidth) {
    if(field == null) {
      throw new NullPointerException("Cannot format a null field");
    }
    getWriter().print(fieldSpec);
    int nWritten = fieldSpec.length();
    int requiredSize = maxFieldSpecWidth + 1;
    for(int i = nWritten; i <= requiredSize; i++) {
      getWriter().print(' ');
      nWritten++;
    }
    String typeName = getTypeName(field.getType());
    getWriter().print(typeName);
    nWritten += typeName.length();
    requiredSize = requiredSize + maxTypeNameWidth + 1;
    for(int i = nWritten; i <= requiredSize; i++) {
      getWriter().print(' ');
      nWritten++;
    }
    if(field.getValue() instanceof FudgeMsg) {
      getWriter().println();
      FudgeMsg msgValue = (FudgeMsg) field.getValue();
      format(msgValue, depth + 1);
    } else {
      getWriter().print(field.getValue());
      getWriter().println();
    }
    getWriter().flush();
  }
  
  protected String getFieldSpec(FudgeField field, int index, int depth) {
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < depth; i++) {
      sb.append(_indentText);
    }
    sb.append(index);
    sb.append("-");
    if(field.getOrdinal() != null) {
      sb.append("(").append(field.getOrdinal()).append(")");
      if(field.getName() != null) {
        sb.append(" ");
      }
    }
    if(field.getName() != null) {
      sb.append(field.getName());
    }
    return sb.toString();
  }

  /**
   * @param indent
   * @return
   */
  protected String composeIndentText(int indent) {
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < indent; i++) {
      sb.append(" ");
    }
    return sb.toString();
  }
  
  protected String getTypeName(FudgeFieldType<?> type) {
    if(type == null) {
      throw new NullPointerException("Must specify a type.");
    }
    return type.getJavaType().getSimpleName();
  }

}
