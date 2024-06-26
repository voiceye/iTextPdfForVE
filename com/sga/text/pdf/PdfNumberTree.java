/*
 * $Id: PdfNumberTree.java 4645 2011-01-06 15:16:40Z redlab_b $
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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Creates a number tree.
 * @author Paulo Soares
 */
public class PdfNumberTree {

    private static final int leafSize = 64;

    /**
     * Creates a number tree.
     * @param items the item of the number tree. The key is an <CODE>Integer</CODE>
     * and the value is a <CODE>PdfObject</CODE>.
     * @param writer the writer
     * @throws IOException on error
     * @return the dictionary with the number tree.
     */
    public static <O extends PdfObject> PdfDictionary writeTree(HashMap<Integer, O> items, PdfWriter writer) throws IOException {
        if (items.isEmpty())
            return null;
        Integer numbers[] = new Integer[items.size()];
        numbers = items.keySet().toArray(numbers);
        Arrays.sort(numbers);
        if (numbers.length <= leafSize) {
            PdfDictionary dic = new PdfDictionary();
            PdfArray ar = new PdfArray();
            for (int k = 0; k < numbers.length; ++k) {
                ar.add(new PdfNumber(numbers[k].intValue()));
                ar.add(items.get(numbers[k]));
            }
            dic.put(PdfName.NUMS, ar);
            return dic;
        }
        int skip = leafSize;
        PdfIndirectReference kids[] = new PdfIndirectReference[(numbers.length + leafSize - 1) / leafSize];
        for (int k = 0; k < kids.length; ++k) {
            int offset = k * leafSize;
            int end = Math.min(offset + leafSize, numbers.length);
            PdfDictionary dic = new PdfDictionary();
            PdfArray arr = new PdfArray();
            arr.add(new PdfNumber(numbers[offset].intValue()));
            arr.add(new PdfNumber(numbers[end - 1].intValue()));
            dic.put(PdfName.LIMITS, arr);
            arr = new PdfArray();
            for (; offset < end; ++offset) {
                arr.add(new PdfNumber(numbers[offset].intValue()));
                arr.add(items.get(numbers[offset]));
            }
            dic.put(PdfName.NUMS, arr);
            kids[k] = writer.addToBody(dic).getIndirectReference();
        }
        int top = kids.length;
        while (true) {
            if (top <= leafSize) {
                PdfArray arr = new PdfArray();
                for (int k = 0; k < top; ++k)
                    arr.add(kids[k]);
                PdfDictionary dic = new PdfDictionary();
                dic.put(PdfName.KIDS, arr);
                return dic;
            }
            skip *= leafSize;
            int tt = (numbers.length + skip - 1 )/ skip;
            for (int k = 0; k < tt; ++k) {
                int offset = k * leafSize;
                int end = Math.min(offset + leafSize, top);
                PdfDictionary dic = new PdfDictionary();
                PdfArray arr = new PdfArray();
                arr.add(new PdfNumber(numbers[k * skip].intValue()));
                arr.add(new PdfNumber(numbers[Math.min((k + 1) * skip, numbers.length) - 1].intValue()));
                dic.put(PdfName.LIMITS, arr);
                arr = new PdfArray();
                for (; offset < end; ++offset) {
                    arr.add(kids[offset]);
                }
                dic.put(PdfName.KIDS, arr);
                kids[k] = writer.addToBody(dic).getIndirectReference();
            }
            top = tt;
        }
    }

    private static void iterateItems(PdfDictionary dic, HashMap<Integer, PdfObject> items) {
        PdfArray nn = (PdfArray)PdfReader.getPdfObjectRelease(dic.get(PdfName.NUMS));
        if (nn != null) {
            for (int k = 0; k < nn.size(); ++k) {
                PdfNumber s = (PdfNumber)PdfReader.getPdfObjectRelease(nn.getPdfObject(k++));
                items.put(Integer.valueOf(s.intValue()), nn.getPdfObject(k));
            }
        }
        else if ((nn = (PdfArray)PdfReader.getPdfObjectRelease(dic.get(PdfName.KIDS))) != null) {
            for (int k = 0; k < nn.size(); ++k) {
                PdfDictionary kid = (PdfDictionary)PdfReader.getPdfObjectRelease(nn.getPdfObject(k));
                iterateItems(kid, items);
            }
        }
    }

    public static HashMap<Integer, PdfObject> readTree(PdfDictionary dic) {
        HashMap<Integer, PdfObject> items = new HashMap<Integer, PdfObject>();
        if (dic != null)
            iterateItems(dic, items);
        return items;
    }
}