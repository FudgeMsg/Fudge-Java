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
package org.fudgemsg;

import java.io.UTFDataFormatException;
import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

/**
 * Support for normal UTF-8 instead of the modified UTF-8 encoding used in the earlier versions of the specification.
 * Implemented from information at http://en.wikipedia.org/wiki/UTF-8, and other methods from ModifiedUTF8Util.
 * 
 * Note that this has deprecated on it in a deliberately misleading fashion - it's the ModifiedUTF8Util one that should be; not this.
 */
@Deprecated
public class UTF8 {
  
  private UTF8 () {
  }
  
  public static int getLengthBytes (final String str) {
    int bytes = str.length ();
    for (int i = 0; i < bytes; i++) {
      final int c = str.charAt(i);
      if (c >= 0x10000) {
        bytes += 3;
      } else if (c >= 0x800) {
        bytes += 2;
      } else if (c >= 0x80) {
        bytes++;
      }
    }
    return bytes;
  }
  
  public static int getLengthBytes (final char[] str) {
    int bytes = str.length;
    for (int i = 0; i < bytes; i++) {
      final int c = (int)str[i];
      if (c >= 0x10000) {
        bytes += 3;
      } else if (c >= 0x800) {
        bytes += 2;
      } else if (c >= 0x80) {
        bytes++;
      }
    }
    return bytes;
  }
  
  public static int encode (final String str, final byte[] arr) {
    final int len = str.length ();
    int count = 0;
    for (int i = 0; i < len; i++) {
      final int c = str.charAt (i);
      if (c >= 0x10000) {
        arr[count++] = (byte)(0xF0 | ((c >> 18) & 0x07));
        arr[count++] = (byte)(0x80 | ((c >> 12) & 0x3F));
        arr[count++] = (byte)(0x80 | ((c >> 6) & 0x3F));
        arr[count++] = (byte)(0x80 | (c & 0x3F));
      } else if (c >= 0x800) {
        arr[count++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
        arr[count++] = (byte)(0x80 | ((c >> 6) & 0x3F));
        arr[count++] = (byte)(0x80 | (c & 0x3F));
      } else if (c >= 0x80) {
        arr[count++] = (byte)(0xC0 | ((c >> 6) & 0x1F));
        arr[count++] = (byte)(0x80 | (c & 0x3F));
      } else {
        arr[count++] = (byte)c;
      }
    }
    return count;
  }
  
  public static byte[] encode (final String str) {
    final byte[] buffer = new byte[getLengthBytes (str)];
    encode (str, buffer);
    return buffer;
  }
  
  public static String decode (final byte[] arr) throws UTFDataFormatException {
    return decode (arr, 0, arr.length);
  }
  
  public static String decode (final byte[] arr, final int start, int length) throws UTFDataFormatException {
    final char[] buffer = new char[length];
    int count = 0;
    length += start;
    for (int i = start; i < length; i++) {
      final int c = (int)arr[i] & 0xFF;
      final int l = c >> 4;
      if (l < 8) {
        // 1 byte encoding
        buffer[count++] = (char)c;
      } else if (l < 12) {
        // illegal
        throw new UTFDataFormatException ("invalid byte sequence at position " + i);
      } else if (l < 14) {
        // 2 byte encoding
        if (++i >= length) throw new UTFDataFormatException ("unexpected end of data at position " + i);
        final int c2 = (int)arr[i] & 0xFF;
        if ((c2 & 0xC0) != 0x80) throw new UTFDataFormatException ("invalid character in 2-byte sequence at position " + i);
        buffer[count++] = (char)(((c & 0x1F) << 6) | (c2 & 0x3F));
      } else if (l < 15) {
        // 3 byte encoding
        if ((i += 2) >= length) throw new UTFDataFormatException ("unexpected end of data at position " + i);
        final int c2 = (int)arr[i - 1] & 0xFF;
        final int c3 = (int)arr[i] & 0xFF;
        if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80)) throw new UTFDataFormatException ("unexpected end of data at position " + i);
        buffer[count++] = (char)(((c & 0x0F) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F));
      } else {
        // 4 byte encoding
        if ((i += 3) >= length) throw new UTFDataFormatException ("unexpected end of data at position " + i);
        final int c2 = (int)arr[i - 2] & 0xFF;
        final int c3 = (int)arr[i - 1] & 0xFF;
        final int c4 = (int)arr[i] & 0xFF;
        if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80) || ((c4 & 0xC0) != 0x80)) throw new UTFDataFormatException ("unexpected end of data at position " + i);
        buffer[count++] = (char)(((c & 0x07) << 18) | ((c2 & 0x3F) << 12) | ((c3 & 0x3F) << 6) | (c4 & 0x3F));
      }
    }
    return new String (buffer, 0, count);
  }
  
  public static String readString (final DataInput is, final int utfLen) throws IOException {
    // REVIEW kirk 2009-08-18 -- This can be optimized. We're copying the data too many
    // times. Particularly since we expect that most of the time we're reading from
    // a byte array already, duplicating it doesn't make much sense.
    byte[] bytearr = new byte[utfLen];
    is.readFully(bytearr, 0, utfLen);
    return decode(bytearr);
  }
  
  public static int writeString (final DataOutput os, final String str) throws IOException {
    // REVIEW 2010-01-26 Andrew -- Can this be optimised like the readString method? 
    byte[] bytearr = encode(str);
    os.write(bytearr);
    return bytearr.length;
  }
  
}