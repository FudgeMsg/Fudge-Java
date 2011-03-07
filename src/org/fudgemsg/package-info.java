/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * The core API for working with Fudge encoded data.
 * <p>
 * In general, most applications will start with an instance of {@link org.fudgemsg.FudgeContext},
 * and use that to read, write, and persist instances of {@link org.fudgemsg.FudgeFieldContainer}
 * using the reader and writer classes.
 * <p>
 * Applications requiring serialization of more complex Java objects and automated support for
 * encoding objects to/from Fudge messages should use the {@link org.fudgemsg.mapping} package.
 */
package org.fudgemsg;
