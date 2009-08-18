/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge;

import java.io.DataInput;
import java.io.IOException;
import java.io.UTFDataFormatException;

/**
 * 
 *
 * @author kirk
 */
public class FudgeStreamDecoder {
  
  public static FudgeMsg readMsg(DataInput is) throws IOException {
    checkInputStream(is);
    int nRead = 0;
    byte magicByte = is.readByte();
    nRead++;
    if(magicByte != FudgeStreamEncoder.MESSAGE_START_MAGIC_BYTE) {
      // What is the actual desired behavior here?
      throw new RuntimeException("Input stream lacks the Fudge start byte");
    }
    /*byte taxonomy = */is.readByte();
    nRead++;
    short nFields = is.readShort();
    nRead += 2;
    int size = is.readInt();
    nRead += 4;
    
    FudgeMsg msg = new FudgeMsg();
    for(int i = 0; i < nFields; i++) {
      nRead += readField(is, msg);
    }
    
    if((size > 0) && (nRead != size)) {
      throw new RuntimeException("Expected to read " + size + " but only had " + nRead + " in message.");
    }
    return msg;
  }

  /**
   * Reads data about a field, and adds it to the message as a new field.
   * 
   * @param is
   * @param msg
   * @return The number of bytes read.
   */
  public static int readField(DataInput is, FudgeMsg msg) throws IOException {
    checkInputStream(is);
    int nRead = 0;
    
    byte fieldPrefix = is.readByte();
    nRead++;
    boolean fixedWidth = (fieldPrefix & FudgeStreamEncoder.FIELD_PREFIX_FIXED_WIDTH_MASK) != 0;
    boolean hasOrdinal = (fieldPrefix & FudgeStreamEncoder.FIELD_PREFIX_ORDINAL_PROVIDED_MASK) != 0;
    boolean hasName = (fieldPrefix & FudgeStreamEncoder.FIELD_PREFIX_NAME_PROVIDED_MASK) != 0;
    int varSizeBytes = 0;
    if(!fixedWidth) {
      varSizeBytes = (fieldPrefix << 1) >> 4;
    }
    
    byte typeId = is.readByte();
    nRead++;
    
    Short ordinal = null;
    if(hasOrdinal) {
      ordinal = is.readShort();
      nRead += 2;
    }
    
    String name = null;
    if(hasName) {
      int nameSize = is.readUnsignedByte();
      nRead++;
      name = readString(is, nameSize);
      nRead += nameSize;
    }
    
    FudgeFieldType type = FudgeTypeDictionary.INSTANCE.getByTypeId(typeId);
    if(type == null) {
      // REVIEW kirk 2009-08-18 -- Is this the right behavior?
      throw new RuntimeException("Unable to locate a FudgeFieldType for type id " + typeId + " for field " + ordinal + ":" + name);
    }
    int varSize = 0;
    if(!fixedWidth) {
      switch(varSizeBytes) {
      case 0: varSize = 0; break;
      case 1: varSize = is.readUnsignedByte(); nRead+=1; break;
      case 2: varSize = is.readShort(); nRead += 2; break;
      case 4: varSize = is.readInt();  nRead += 4; break;
      default:
        throw new RuntimeException("Illegal number of bytes indicated for variable width encoding: " + varSizeBytes);
      }
      
    }
    Object fieldValue = readFieldValue(is, type, varSize);
    if(fixedWidth) {
      nRead += type.getFixedSize();
    } else {
      nRead += varSize;
    }
    
    msg.add(type, fieldValue, name, ordinal);
    
    return nRead;
  }
  
  /**
   * @param is
   * @param type
   * @param varSize 
   * @return
   */
  public static Object readFieldValue(DataInput is, FudgeFieldType type, int varSize) throws IOException {
    assert type != null;
    assert is != null;
    
    // Special fast-pass for known field types
    switch(type.getTypeId()) {
    case FudgeTypeDictionary.BOOLEAN_TYPE_ID:
      return is.readBoolean();
    case FudgeTypeDictionary.BYTE_TYPE_ID:
      return is.readByte();
    case FudgeTypeDictionary.SHORT_TYPE_ID:
      return is.readShort();
    case FudgeTypeDictionary.INT_TYPE_ID:
      return is.readInt();
    case FudgeTypeDictionary.LONG_TYPE_ID:
      return is.readLong();
    }
    
    throw new UnsupportedOperationException("Unhandled type " + type);
  }

  public static String readString(DataInput is, int utflen) throws IOException {
    // REVIEW kirk 2009-08-18 -- This can be optimized. We're copying the data too many
    // times. Particularly since we expect that most of the time we're reading from
    // a byte array already, duplicating it doesn't make much sense.
    byte[] bytearr = new byte[utflen];
    char[] chararr = new char[utflen];

    int c, char2, char3;
    int count = 0;
    int chararr_count=0;

    is.readFully(bytearr, 0, utflen);

    while (count < utflen) {
        c = (int) bytearr[count] & 0xff;      
        if (c > 127) break;
        count++;
        chararr[chararr_count++]=(char)c;
    }

    while (count < utflen) {
        c = (int) bytearr[count] & 0xff;
        switch (c >> 4) {
            case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                /* 0xxxxxxx*/
                count++;
                chararr[chararr_count++]=(char)c;
                break;
            case 12: case 13:
                /* 110x xxxx   10xx xxxx*/
                count += 2;
                if (count > utflen)
                    throw new UTFDataFormatException(
                        "malformed input: partial character at end");
                char2 = (int) bytearr[count-1];
                if ((char2 & 0xC0) != 0x80)
                    throw new UTFDataFormatException(
                        "malformed input around byte " + count); 
                chararr[chararr_count++]=(char)(((c & 0x1F) << 6) | 
                                                (char2 & 0x3F));  
                break;
            case 14:
                /* 1110 xxxx  10xx xxxx  10xx xxxx */
                count += 3;
                if (count > utflen)
                    throw new UTFDataFormatException(
                        "malformed input: partial character at end");
                char2 = (int) bytearr[count-2];
                char3 = (int) bytearr[count-1];
                if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                    throw new UTFDataFormatException(
                        "malformed input around byte " + (count-1));
                chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
                                                ((char2 & 0x3F) << 6)  |
                                                ((char3 & 0x3F) << 0));
                break;
            default:
                /* 10xx xxxx,  1111 xxxx */
                throw new UTFDataFormatException(
                    "malformed input around byte " + count);
        }
    }
    // The number of chars produced may be less than utflen
    return new String(chararr, 0, chararr_count);
  }

  protected static void checkInputStream(DataInput is) {
    if(is == null) {
      throw new NullPointerException("Must specify a DataInput for processing.");
    }
  }

}
