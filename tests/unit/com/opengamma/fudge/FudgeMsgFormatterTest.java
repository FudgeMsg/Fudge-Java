/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.io.PrintWriter;

import org.junit.Test;

/**
 * 
 *
 * @author kirk
 */
public class FudgeMsgFormatterTest {
  
  /**
   * Will output a {@link FudgeMsg} to {@code System.out} so that you can visually
   * examine it.
   */
  @Test
  public void outputToStdoutAllNames() {
    System.out.println("FudgeMsgFormatterTest.outputToStdoutAllNames()");
    FudgeMsg msg = FudgeMsgTest.createMessageAllNames();
    msg.add("Sub Message", 9999, FudgeMsgTest.createMessageAllNames());
    (new FudgeMsgFormatter(new PrintWriter(System.out))).format(msg);
  }
  
  /**
   * Will output a {@link FudgeMsg} to {@code System.out} so that you can visually
   * examine it.
   */
  @Test
  public void outputToStdoutAllOrdinals() {
    System.out.println("FudgeMsgFormatterTest.outputToStdoutAllOrdinals()");
    FudgeMsg msg = FudgeMsgTest.createMessageAllOrdinals();
    msg.add("Sub Message", 9999, FudgeMsgTest.createMessageAllOrdinals());
    (new FudgeMsgFormatter(new PrintWriter(System.out))).format(msg);
  }

}
