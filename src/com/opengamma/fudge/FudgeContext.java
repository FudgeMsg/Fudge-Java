/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.opengamma.fudge.taxon.FudgeTaxonomy;
import com.opengamma.fudge.taxon.TaxonomyResolver;

/**
 * The primary entry-point for code to interact with the rest of the Fudge system.
 * For performance reasons, there are many options that are passed around as parameters
 * inside static methods for encoding and decoding, and many lightweight objects that
 * ideally don't know of their configuration context.
 * However, in a large application, it is often desirable to collect all configuration
 * parameters in one location and inject options into it.
 * <p/>
 * {@code FudgeContext} allows application developers to have a single location
 * to inject dependent parameters and instances, and make them available through
 * simple method invocations. In addition, because it wraps all checked exceptions
 * into instances of {@link FudgeRuntimeException}, it is the ideal way to use
 * the Fudge encoding system from within Spring applications.
 * <p/>
 * While most applications will have a single instance of {@code FudgeContext},
 * some applications will have one instance per unit of encoding/decoding parameters.
 * For example, if an application is consuming data from two messaging feeds, each
 * of which reuses the same taxonomy ID to represent a different
 * {@link FudgeTaxonomy}, it would configure two different instances of
 * {@code FudgeContext}, one per feed.  
 *
 * @author kirk
 */
public class FudgeContext {
  private TaxonomyResolver _taxonomyResolver;

  /**
   * @return the taxonomyResolver
   */
  public TaxonomyResolver getTaxonomyResolver() {
    return _taxonomyResolver;
  }

  /**
   * @param taxonomyResolver the taxonomyResolver to set
   */
  public void setTaxonomyResolver(TaxonomyResolver taxonomyResolver) {
    _taxonomyResolver = taxonomyResolver;
  }
  
  public void serialize(FudgeMsg msg, OutputStream os) {
    serialize(msg, null, os);
  }
  
  public void serialize(FudgeMsg msg, Short taxonomyId, OutputStream os) {
    FudgeTaxonomy taxonomy = null;
    if((getTaxonomyResolver() != null) && (taxonomyId != null)) {
      taxonomy = getTaxonomyResolver().resolveTaxonomy(taxonomyId);
    }
    DataOutputStream dos = new DataOutputStream(os);
    try {
      FudgeStreamEncoder.writeMsg(dos, new FudgeMsgEnvelope(msg), taxonomy, (taxonomyId == null) ? (short)0 : taxonomyId);
    } catch (IOException e) {
      throw new FudgeRuntimeException("Unable to write Fudge message to OutputStream", e);
    }
  }
  
  public byte[] toByteArray(FudgeMsg msg) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    serialize(msg, baos);
    return baos.toByteArray();
  }
  
  public FudgeMsgEnvelope deserialize(InputStream is) {
    DataInputStream dis = new DataInputStream(is);
    FudgeMsgEnvelope envelope;
    try {
      envelope = FudgeStreamDecoder.readMsg(dis, getTaxonomyResolver());
    } catch (IOException e) {
      throw new FudgeRuntimeException("Unable to deserialize FudgeMsg from input stream", e);
    }
    return envelope;
  }
  
  public FudgeMsgEnvelope deserialize(byte[] bytes) {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    return deserialize(bais);
  }
  
}
