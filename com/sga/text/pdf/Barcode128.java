/*
 * $Id: Barcode128.java 4647 2011-01-08 21:15:41Z redlab_b $
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
import java.awt.Canvas;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import com.sga.text.error_messages.MessageLocalization;

import com.sga.text.Element;
import com.sga.text.ExceptionConverter;
import com.sga.text.Rectangle;
import com.sga.text.BaseColor;

/**
 * Implements the code 128 and UCC/EAN-128. Other symbologies are allowed in raw mode.<p>
 * The code types allowed are:<br>
 * <ul>
 * <li><b>CODE128</b> - plain barcode 128.
 * <li><b>CODE128_UCC</b> - support for UCC/EAN-128 with a full list of AI.
 * <li><b>CODE128_RAW</b> - raw mode. The code attribute has the actual codes from 0
 *     to 105 followed by '&#92;uffff' and the human readable text.
 * </ul>
 * The default parameters are:
 * <pre>
 * x = 0.8f;
 * font = BaseFont.createFont("Helvetica", "winansi", false);
 * size = 8;
 * baseline = size;
 * barHeight = size * 3;
 * textAlignment = Element.ALIGN_CENTER;
 * codeType = CODE128;
 * </pre>
 * @author Paulo Soares
 */
public class Barcode128 extends Barcode{

    /** The bars to generate the code.
     */    
    private static final byte BARS[][] = 
    {
        {2, 1, 2, 2, 2, 2},
        {2, 2, 2, 1, 2, 2},
        {2, 2, 2, 2, 2, 1},
        {1, 2, 1, 2, 2, 3},
        {1, 2, 1, 3, 2, 2},
        {1, 3, 1, 2, 2, 2},
        {1, 2, 2, 2, 1, 3},
        {1, 2, 2, 3, 1, 2},
        {1, 3, 2, 2, 1, 2},
        {2, 2, 1, 2, 1, 3},
        {2, 2, 1, 3, 1, 2},
        {2, 3, 1, 2, 1, 2},
        {1, 1, 2, 2, 3, 2},
        {1, 2, 2, 1, 3, 2},
        {1, 2, 2, 2, 3, 1},
        {1, 1, 3, 2, 2, 2},
        {1, 2, 3, 1, 2, 2},
        {1, 2, 3, 2, 2, 1},
        {2, 2, 3, 2, 1, 1},
        {2, 2, 1, 1, 3, 2},
        {2, 2, 1, 2, 3, 1},
        {2, 1, 3, 2, 1, 2},
        {2, 2, 3, 1, 1, 2},
        {3, 1, 2, 1, 3, 1},
        {3, 1, 1, 2, 2, 2},
        {3, 2, 1, 1, 2, 2},
        {3, 2, 1, 2, 2, 1},
        {3, 1, 2, 2, 1, 2},
        {3, 2, 2, 1, 1, 2},
        {3, 2, 2, 2, 1, 1},
        {2, 1, 2, 1, 2, 3},
        {2, 1, 2, 3, 2, 1},
        {2, 3, 2, 1, 2, 1},
        {1, 1, 1, 3, 2, 3},
        {1, 3, 1, 1, 2, 3},
        {1, 3, 1, 3, 2, 1},
        {1, 1, 2, 3, 1, 3},
        {1, 3, 2, 1, 1, 3},
        {1, 3, 2, 3, 1, 1},
        {2, 1, 1, 3, 1, 3},
        {2, 3, 1, 1, 1, 3},
        {2, 3, 1, 3, 1, 1},
        {1, 1, 2, 1, 3, 3},
        {1, 1, 2, 3, 3, 1},
        {1, 3, 2, 1, 3, 1},
        {1, 1, 3, 1, 2, 3},
        {1, 1, 3, 3, 2, 1},
        {1, 3, 3, 1, 2, 1},
        {3, 1, 3, 1, 2, 1},
        {2, 1, 1, 3, 3, 1},
        {2, 3, 1, 1, 3, 1},
        {2, 1, 3, 1, 1, 3},
        {2, 1, 3, 3, 1, 1},
        {2, 1, 3, 1, 3, 1},
        {3, 1, 1, 1, 2, 3},
        {3, 1, 1, 3, 2, 1},
        {3, 3, 1, 1, 2, 1},
        {3, 1, 2, 1, 1, 3},
        {3, 1, 2, 3, 1, 1},
        {3, 3, 2, 1, 1, 1},
        {3, 1, 4, 1, 1, 1},
        {2, 2, 1, 4, 1, 1},
        {4, 3, 1, 1, 1, 1},
        {1, 1, 1, 2, 2, 4},
        {1, 1, 1, 4, 2, 2},
        {1, 2, 1, 1, 2, 4},
        {1, 2, 1, 4, 2, 1},
        {1, 4, 1, 1, 2, 2},
        {1, 4, 1, 2, 2, 1},
        {1, 1, 2, 2, 1, 4},
        {1, 1, 2, 4, 1, 2},
        {1, 2, 2, 1, 1, 4},
        {1, 2, 2, 4, 1, 1},
        {1, 4, 2, 1, 1, 2},
        {1, 4, 2, 2, 1, 1},
        {2, 4, 1, 2, 1, 1},
        {2, 2, 1, 1, 1, 4},
        {4, 1, 3, 1, 1, 1},
        {2, 4, 1, 1, 1, 2},
        {1, 3, 4, 1, 1, 1},
        {1, 1, 1, 2, 4, 2},
        {1, 2, 1, 1, 4, 2},
        {1, 2, 1, 2, 4, 1},
        {1, 1, 4, 2, 1, 2},
        {1, 2, 4, 1, 1, 2},
        {1, 2, 4, 2, 1, 1},
        {4, 1, 1, 2, 1, 2},
        {4, 2, 1, 1, 1, 2},
        {4, 2, 1, 2, 1, 1},
        {2, 1, 2, 1, 4, 1},
        {2, 1, 4, 1, 2, 1},
        {4, 1, 2, 1, 2, 1},
        {1, 1, 1, 1, 4, 3},
        {1, 1, 1, 3, 4, 1},
        {1, 3, 1, 1, 4, 1},
        {1, 1, 4, 1, 1, 3},
        {1, 1, 4, 3, 1, 1},
        {4, 1, 1, 1, 1, 3},
        {4, 1, 1, 3, 1, 1},
        {1, 1, 3, 1, 4, 1},
        {1, 1, 4, 1, 3, 1},
        {3, 1, 1, 1, 4, 1},
        {4, 1, 1, 1, 3, 1},
        {2, 1, 1, 4, 1, 2},
        {2, 1, 1, 2, 1, 4},
        {2, 1, 1, 2, 3, 2}
    };
    
    /** The stop bars.
     */    
    private static final byte BARS_STOP[] = {2, 3, 3, 1, 1, 1, 2};
    /** The charset code change.
     */
    public static final char CODE_AB_TO_C = 99;
    /** The charset code change.
     */
    public static final char CODE_AC_TO_B = 100;
    /** The charset code change.
     */
    public static final char CODE_BC_TO_A = 101;
    /** The code for UCC/EAN-128.
     */
    public static final char FNC1_INDEX = 102;
    /** The start code.
     */
    public static final char START_A = 103;
    /** The start code.
     */
    public static final char START_B = 104;
    /** The start code.
     */
    public static final char START_C = 105;

    public static final char FNC1 = '\u00ca';
    public static final char DEL = '\u00c3';
    public static final char FNC3 = '\u00c4';
    public static final char FNC2 = '\u00c5';
    public static final char SHIFT = '\u00c6';
    public static final char CODE_C = '\u00c7';
    public static final char CODE_A = '\u00c8';
    public static final char FNC4 = '\u00c8';
    public static final char STARTA = '\u00cb';
    public static final char STARTB = '\u00cc';
    public static final char STARTC = '\u00cd';
    
    private static final IntHashtable ais = new IntHashtable();
    /** Creates new Barcode128 */
    public Barcode128() {
        try {
            x = 0.8f;
            font = BaseFont.createFont("Helvetica", "winansi", false);
            size = 8;
            baseline = size;
            barHeight = size * 3;
            textAlignment = Element.ALIGN_CENTER;
            codeType = CODE128;
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Removes the FNC1 codes in the text.
     * @param code the text to clean
     * @return the cleaned text
     */    
    public static String removeFNC1(String code) {
        int len = code.length();
        StringBuffer buf = new StringBuffer(len);
        for (int k = 0; k < len; ++k) {
            char c = code.charAt(k);
            if (c >= 32 && c <= 126)
                buf.append(c);
        }
        return buf.toString();
    }
    
    /**
     * Gets the human readable text of a sequence of AI.
     * @param code the text
     * @return the human readable text
     */    
    public static String getHumanReadableUCCEAN(String code) {
        StringBuffer buf = new StringBuffer();
        String fnc1 = String.valueOf(FNC1);
        try {
            while (true) {
                if (code.startsWith(fnc1)) {
                    code = code.substring(1);
                    continue;
                }
                int n = 0;
                int idlen = 0;
                for (int k = 2; k < 5; ++k) {
                    if (code.length() < k)
                        break;
                    if ((n = ais.get(Integer.parseInt(code.substring(0, k)))) != 0) {
                        idlen = k;
                        break;
                    }
                }
                if (idlen == 0)
                    break;
                buf.append('(').append(code.substring(0, idlen)).append(')');
                code = code.substring(idlen);
                if (n > 0) {
                    n -= idlen;
                    if (code.length() <= n)
                        break;
                    buf.append(removeFNC1(code.substring(0, n)));
                    code = code.substring(n);
                }
                else {
                    int idx = code.indexOf(FNC1);
                    if (idx < 0)
                        break;
                    buf.append(code.substring(0,idx));
                    code = code.substring(idx + 1);
                }
            }
        }
        catch (Exception e) {
            //empty
        }
        buf.append(removeFNC1(code));
        return buf.toString();
    }
    
    /** Returns <CODE>true</CODE> if the next <CODE>numDigits</CODE>
     * starting from index <CODE>textIndex</CODE> are numeric skipping any FNC1.
     * @param text the text to check
     * @param textIndex where to check from
     * @param numDigits the number of digits to check
     * @return the check result
     */    
    static boolean isNextDigits(String text, int textIndex, int numDigits) {
        int len = text.length();
        while (textIndex < len && numDigits > 0) {
            if (text.charAt(textIndex) == FNC1) {
                ++textIndex;
                continue;
            }
            int n = Math.min(2, numDigits);
            if (textIndex + n > len)
                return false;
            while (n-- > 0) {
                char c = text.charAt(textIndex++);
                if (c < '0' || c > '9')
                    return false;
                --numDigits;
            }
        }
        return numDigits == 0;
    }
    
    /** Packs the digits for charset C also considering FNC1. It assumes that all the parameters
     * are valid.
     * @param text the text to pack
     * @param textIndex where to pack from
     * @param numDigits the number of digits to pack. It is always an even number
     * @return the packed digits, two digits per character
     */    
    static String getPackedRawDigits(String text, int textIndex, int numDigits) {
        StringBuilder out = new StringBuilder("");
        int start = textIndex;
        while (numDigits > 0) {
            if (text.charAt(textIndex) == FNC1) {
                out.append(FNC1_INDEX);
                ++textIndex;
                continue;
            }
            numDigits -= 2;
            int c1 = text.charAt(textIndex++) - '0';
            int c2 = text.charAt(textIndex++) - '0';
            out.append((char)(c1 * 10 + c2));
        }
        return (char)(textIndex - start) + out.toString();
    }
    
    /** Converts the human readable text to the characters needed to
     * create a barcode. Some optimization is done to get the shortest code.
     * @param text the text to convert
     * @param ucc <CODE>true</CODE> if it is an UCC/EAN-128. In this case
     * the character FNC1 is added
     * @return the code ready to be fed to getBarsCode128Raw()
     */    
    public static String getRawText(String text, boolean ucc) {
        String out = "";
        int tLen = text.length();
        if (tLen == 0) {
            out += START_B;
            if (ucc)
                out += FNC1_INDEX;
            return out;
        }
        int c = 0;
        for (int k = 0; k < tLen; ++k) {
            c = text.charAt(k);
            if (c > 127 && c != FNC1)
                throw new RuntimeException(MessageLocalization.getComposedMessage("there.are.illegal.characters.for.barcode.128.in.1", text));
        }
        c = text.charAt(0);
        char currentCode = START_B;
        int index = 0;
        if (isNextDigits(text, index, 2)) {
            currentCode = START_C;
            out += currentCode;
            if (ucc)
                out += FNC1_INDEX;
            String out2 = getPackedRawDigits(text, index, 2);
            index += out2.charAt(0);
            out += out2.substring(1);
        }
        else if (c < ' ') {
            currentCode = START_A;
            out += currentCode;
            if (ucc)
                out += FNC1_INDEX;
            out += (char)(c + 64);
            ++index;
        }
        else {
            out += currentCode;
            if (ucc)
                out += FNC1_INDEX;
            if (c == FNC1)
                out += FNC1_INDEX;
            else
                out += (char)(c - ' ');
            ++index;
        }
        while (index < tLen) {
            switch (currentCode) {
                case START_A:
                    {
                        if (isNextDigits(text, index, 4)) {
                            currentCode = START_C;
                            out += CODE_AB_TO_C;
                            String out2 = getPackedRawDigits(text, index, 4);
                            index += out2.charAt(0);
                            out += out2.substring(1);
                        }
                        else {
                            c = text.charAt(index++);
                            if (c == FNC1)
                                out += FNC1_INDEX;
                            else if (c > '_') {
                                currentCode = START_B;
                                out += CODE_AC_TO_B;
                                out += (char)(c - ' ');
                            }
                            else if (c < ' ')
                                out += (char)(c + 64);
                            else
                                out += (char)(c - ' ');
                        }
                    }
                    break;
                case START_B:
                    {
                        if (isNextDigits(text, index, 4)) {
                            currentCode = START_C;
                            out += CODE_AB_TO_C;
                            String out2 = getPackedRawDigits(text, index, 4);
                            index += out2.charAt(0);
                            out += out2.substring(1);
                        }
                        else {
                            c = text.charAt(index++);
                            if (c == FNC1)
                                out += FNC1_INDEX;
                            else if (c < ' ') {
                                currentCode = START_A;
                                out += CODE_BC_TO_A;
                                out += (char)(c + 64);
                            }
                            else {
                                out += (char)(c - ' ');
                            }
                        }
                    }
                    break;
                case START_C:
                    {
                        if (isNextDigits(text, index, 2)) {
                            String out2 = getPackedRawDigits(text, index, 2);
                            index += out2.charAt(0);
                            out += out2.substring(1);
                        }
                        else {
                            c = text.charAt(index++);
                            if (c == FNC1)
                                out += FNC1_INDEX;
                            else if (c < ' ') {
                                currentCode = START_A;
                                out += CODE_BC_TO_A;
                                out += (char)(c + 64);
                            }
                            else {
                                currentCode = START_B;
                                out += CODE_AC_TO_B;
                                out += (char)(c - ' ');
                            }
                        }
                    }
                    break;
            }
        }
        return out;
    }
    
    /** Generates the bars. The input has the actual barcodes, not
     * the human readable text.
     * @param text the barcode
     * @return the bars
     */    
    public static byte[] getBarsCode128Raw(String text) {
        int idx = text.indexOf('\uffff');
        if (idx >= 0)
            text = text.substring(0, idx);
        int chk = text.charAt(0);
        for (int k = 1; k < text.length(); ++k)
            chk += k * text.charAt(k);
        chk = chk % 103;
        text += (char)chk;
        byte bars[] = new byte[(text.length() + 1) * 6 + 7];
        int k;
        for (k = 0; k < text.length(); ++k)
            System.arraycopy(BARS[text.charAt(k)], 0, bars, k * 6, 6);
        System.arraycopy(BARS_STOP, 0, bars, k * 6, 7);
        return bars;
    }
    
    /** Gets the maximum area that the barcode and the text, if
     * any, will occupy. The lower left corner is always (0, 0).
     * @return the size the barcode occupies.
     */
    public Rectangle getBarcodeSize() {
        float fontX = 0;
        float fontY = 0;
        String fullCode;
        if (font != null) {
            if (baseline > 0)
                fontY = baseline - font.getFontDescriptor(BaseFont.DESCENT, size);
            else
                fontY = -baseline + size;
            if (codeType == CODE128_RAW) {
                int idx = code.indexOf('\uffff');
                if (idx < 0)
                    fullCode = "";
                else
                    fullCode = code.substring(idx + 1);
            }
            else if (codeType == CODE128_UCC)
                fullCode = getHumanReadableUCCEAN(code);
            else
                fullCode = removeFNC1(code);
            fontX = font.getWidthPoint(altText != null ? altText : fullCode, size);
        }
        if (codeType == CODE128_RAW) {
            int idx = code.indexOf('\uffff');
            if (idx >= 0)
                fullCode = code.substring(0, idx);
            else
                fullCode = code;
        }
        else {
            fullCode = getRawText(code, codeType == CODE128_UCC);
        }
        int len = fullCode.length();
        float fullWidth = (len + 2) * 11 * x + 2 * x;
        fullWidth = Math.max(fullWidth, fontX);
        float fullHeight = barHeight + fontY;
        return new Rectangle(fullWidth, fullHeight);
    }
    
    /** Places the barcode in a <CODE>PdfContentByte</CODE>. The
     * barcode is always placed at coordinates (0, 0). Use the
     * translation matrix to move it elsewhere.<p>
     * The bars and text are written in the following colors:<p>
     * <P><TABLE BORDER=1>
     * <TR>
     *   <TH><P><CODE>barColor</CODE></TH>
     *   <TH><P><CODE>textColor</CODE></TH>
     *   <TH><P>Result</TH>
     *   </TR>
     * <TR>
     *   <TD><P><CODE>null</CODE></TD>
     *   <TD><P><CODE>null</CODE></TD>
     *   <TD><P>bars and text painted with current fill color</TD>
     *   </TR>
     * <TR>
     *   <TD><P><CODE>barColor</CODE></TD>
     *   <TD><P><CODE>null</CODE></TD>
     *   <TD><P>bars and text painted with <CODE>barColor</CODE></TD>
     *   </TR>
     * <TR>
     *   <TD><P><CODE>null</CODE></TD>
     *   <TD><P><CODE>textColor</CODE></TD>
     *   <TD><P>bars painted with current color<br>text painted with <CODE>textColor</CODE></TD>
     *   </TR>
     * <TR>
     *   <TD><P><CODE>barColor</CODE></TD>
     *   <TD><P><CODE>textColor</CODE></TD>
     *   <TD><P>bars painted with <CODE>barColor</CODE><br>text painted with <CODE>textColor</CODE></TD>
     *   </TR>
     * </TABLE>
     * @param cb the <CODE>PdfContentByte</CODE> where the barcode will be placed
     * @param barColor the color of the bars. It can be <CODE>null</CODE>
     * @param textColor the color of the text. It can be <CODE>null</CODE>
     * @return the dimensions the barcode occupies
     */
    public Rectangle placeBarcode(PdfContentByte cb, BaseColor barColor, BaseColor textColor) {
        String fullCode;
        if (codeType == CODE128_RAW) {
            int idx = code.indexOf('\uffff');
            if (idx < 0)
                fullCode = "";
            else
                fullCode = code.substring(idx + 1);
        }
        else if (codeType == CODE128_UCC)
            fullCode = getHumanReadableUCCEAN(code);
        else
            fullCode = removeFNC1(code);
        float fontX = 0;
        if (font != null) {
            fontX = font.getWidthPoint(fullCode = altText != null ? altText : fullCode, size);
        }
        String bCode;
        if (codeType == CODE128_RAW) {
            int idx = code.indexOf('\uffff');
            if (idx >= 0)
                bCode = code.substring(0, idx);
            else
                bCode = code;
        }
        else {
            bCode = getRawText(code, codeType == CODE128_UCC);
        }
        int len = bCode.length();
        float fullWidth = (len + 2) * 11 * x + 2 * x;
        float barStartX = 0;
        float textStartX = 0;
        switch (textAlignment) {
            case Element.ALIGN_LEFT:
                break;
            case Element.ALIGN_RIGHT:
                if (fontX > fullWidth)
                    barStartX = fontX - fullWidth;
                else
                    textStartX = fullWidth - fontX;
                break;
            default:
                if (fontX > fullWidth)
                    barStartX = (fontX - fullWidth) / 2;
                else
                    textStartX = (fullWidth - fontX) / 2;
                break;
        }
        float barStartY = 0;
        float textStartY = 0;
        if (font != null) {
            if (baseline <= 0)
                textStartY = barHeight - baseline;
            else {
                textStartY = -font.getFontDescriptor(BaseFont.DESCENT, size);
                barStartY = textStartY + baseline;
            }
        }
        byte bars[] = getBarsCode128Raw(bCode);
        boolean print = true;
        if (barColor != null)
            cb.setColorFill(barColor);
        for (int k = 0; k < bars.length; ++k) {
            float w = bars[k] * x;
            if (print)
                cb.rectangle(barStartX, barStartY, w - inkSpreading, barHeight);
            print = !print;
            barStartX += w;
        }
        cb.fill();
        if (font != null) {
            if (textColor != null)
                cb.setColorFill(textColor);
            cb.beginText();
            cb.setFontAndSize(font, size);
            cb.setTextMatrix(textStartX, textStartY);
            cb.showText(fullCode);
            cb.endText();
        }
        return getBarcodeSize();
    }
    
    /** Creates a <CODE>java.awt.Image</CODE>. This image only
     * contains the bars without any text.
     * @param foreground the color of the bars
     * @param background the color of the background
     * @return the image
     */    
    public java.awt.Image createAwtImage(java.awt.Color foreground, java.awt.Color background) {
        int f = foreground.getRGB();
        int g = background.getRGB();
        Canvas canvas = new Canvas();
        String bCode;
        if (codeType == CODE128_RAW) {
            int idx = code.indexOf('\uffff');
            if (idx >= 0)
                bCode = code.substring(0, idx);
            else
                bCode = code;
        }
        else {
            bCode = getRawText(code, codeType == CODE128_UCC);
        }
        int len = bCode.length();
        int fullWidth = (len + 2) * 11 + 2;
        byte bars[] = getBarsCode128Raw(bCode);
        
        boolean print = true;
        int ptr = 0;
        int height = (int)barHeight;
        int pix[] = new int[fullWidth * height];
        for (int k = 0; k < bars.length; ++k) {
            int w = bars[k];
            int c = g;
            if (print)
                c = f;
            print = !print;
            for (int j = 0; j < w; ++j)
                pix[ptr++] = c;
        }
        for (int k = fullWidth; k < pix.length; k += fullWidth) {
            System.arraycopy(pix, 0, pix, k, fullWidth); 
        }
        Image img = canvas.createImage(new MemoryImageSource(fullWidth, height, pix, 0, fullWidth));
        
        return img;
    }
    
    /**
     * Sets the code to generate. If it's an UCC code and starts with '(' it will
     * be split by the AI. This code in UCC mode is valid:
     * <p>
     * <code>(01)00000090311314(10)ABC123(15)060916</code>
     * @param code the code to generate
     */
    public void setCode(String code) {
        if (getCodeType() == Barcode128.CODE128_UCC && code.startsWith("(")) {
            int idx = 0;
            StringBuilder ret = new StringBuilder("");
            while (idx >= 0) {
                int end = code.indexOf(')', idx);
                if (end < 0)
                    throw new IllegalArgumentException(MessageLocalization.getComposedMessage("badly.formed.ucc.string.1", code));
                String sai = code.substring(idx + 1, end);
                if (sai.length() < 2)
                    throw new IllegalArgumentException(MessageLocalization.getComposedMessage("ai.too.short.1", sai));
                int ai = Integer.parseInt(sai);
                int len = ais.get(ai);
                if (len == 0)
                    throw new IllegalArgumentException(MessageLocalization.getComposedMessage("ai.not.found.1", sai));
                sai = String.valueOf(ai);
                if (sai.length() == 1)
                    sai = "0" + sai;
                idx = code.indexOf('(', end);
                int next = (idx < 0 ? code.length() : idx);
                ret.append(sai).append(code.substring(end + 1, next));
                if (len < 0) {
                    if (idx >= 0)
                        ret.append(FNC1);
                }
                else if (next - end - 1 + sai.length() != len)
                    throw new IllegalArgumentException(MessageLocalization.getComposedMessage("invalid.ai.length.1", sai));
            }
            super.setCode(ret.toString());
        }
        else
            super.setCode(code);
    }
    
    static {
        ais.put(0, 20);
        ais.put(1, 16);
        ais.put(2, 16);
        ais.put(10, -1);
        ais.put(11, 9);
        ais.put(12, 8);
        ais.put(13, 8);
        ais.put(15, 8);
        ais.put(17, 8);
        ais.put(20, 4);
        ais.put(21, -1);
        ais.put(22, -1);
        ais.put(23, -1);
        ais.put(240, -1);
        ais.put(241, -1);
        ais.put(250, -1);
        ais.put(251, -1);
        ais.put(252, -1);
        ais.put(30, -1);
        for (int k = 3100; k < 3700; ++k)
            ais.put(k, 10);
        ais.put(37, -1);
        for (int k = 3900; k < 3940; ++k)
            ais.put(k, -1);
        ais.put(400, -1);
        ais.put(401, -1);
        ais.put(402, 20);
        ais.put(403, -1);
        for (int k = 410; k < 416; ++k)
            ais.put(k, 16);
        ais.put(420, -1);
        ais.put(421, -1);
        ais.put(422, 6);
        ais.put(423, -1);
        ais.put(424, 6);
        ais.put(425, 6);
        ais.put(426, 6);
        ais.put(7001, 17);
        ais.put(7002, -1);
        for (int k = 7030; k < 7040; ++k)
            ais.put(k, -1);
        ais.put(8001, 18);
        ais.put(8002, -1);
        ais.put(8003, -1);
        ais.put(8004, -1);
        ais.put(8005, 10);
        ais.put(8006, 22);
        ais.put(8007, -1);
        ais.put(8008, -1);
        ais.put(8018, 22);
        ais.put(8020, -1);
        ais.put(8100, 10);
        ais.put(8101, 14);
        ais.put(8102, 6);
        for (int k = 90; k < 100; ++k)
            ais.put(k, -1);
    }
}
