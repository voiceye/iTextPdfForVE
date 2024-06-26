/*
 * $Id: StandardDecryption.java 4113 2009-12-01 11:08:59Z blowagie $
 *
 * This file is part of the iText project.
 * Copyright (c) 1998-2009 1T3XT BVBA
 * Authors: Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY 1T3XT,
 * 1T3XT DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://sga.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * you must retain the producer line in every PDF that is created or manipulated
 * using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sga
 */
package com.sga.text.pdf;

import com.sga.text.pdf.crypto.AESCipher;
import com.sga.text.pdf.crypto.ARCFOUREncryption;

public class StandardDecryption {
    protected ARCFOUREncryption arcfour;
    protected AESCipher cipher;
    private byte[] key;
    private static final int AES_128 = 4;
    private boolean aes;
    private boolean initiated;
    private byte[] iv = new byte[16];
    private int ivptr;

    /** Creates a new instance of StandardDecryption */
    public StandardDecryption(byte key[], int off, int len, int revision) {
        aes = revision == AES_128;
        if (aes) {
            this.key = new byte[len];
            System.arraycopy(key, off, this.key, 0, len);
        }
        else {
            arcfour = new ARCFOUREncryption();
            arcfour.prepareARCFOURKey(key, off, len);
        }
    }
    
    public byte[] update(byte[] b, int off, int len) {
        if (aes) {
            if (initiated)
                return cipher.update(b, off, len);
            else {
                int left = Math.min(iv.length - ivptr, len);
                System.arraycopy(b, off, iv, ivptr, left);
                off += left;
                len -= left;
                ivptr += left;
                if (ivptr == iv.length) {
                    cipher = new AESCipher(false, key, iv);
                    initiated = true;
                    if (len > 0)
                        return cipher.update(b, off, len);
                }
                return null;
            }
        }
        else {
            byte[] b2 = new byte[len];
            arcfour.encryptARCFOUR(b, off, len, b2, 0);
            return b2;
        }
    }
    
    public byte[] finish() {
        if (aes) {
            return cipher.doFinal();
        }
        else
            return null;
    }
}