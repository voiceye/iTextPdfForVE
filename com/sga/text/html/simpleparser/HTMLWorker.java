/*
 * $Id: HTMLWorker.java 4695 2011-02-03 12:12:31Z redlab_b $
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
package com.sga.text.html.simpleparser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.sga.text.Chunk;
import com.sga.text.DocListener;
import com.sga.text.DocumentException;
import com.sga.text.Element;
import com.sga.text.ExceptionConverter;
import com.sga.text.FontProvider;
import com.sga.text.Image;
import com.sga.text.ListItem;
import com.sga.text.Paragraph;
import com.sga.text.Phrase;
import com.sga.text.Rectangle;
import com.sga.text.TextElementArray;
import com.sga.text.html.HtmlTags;
import com.sga.text.html.HtmlUtilities;
import com.sga.text.pdf.PdfPCell;
import com.sga.text.pdf.PdfPTable;
import com.sga.text.pdf.draw.LineSeparator;
import com.sga.text.xml.simpleparser.SimpleXMLDocHandler;
import com.sga.text.xml.simpleparser.SimpleXMLParser;

public class HTMLWorker implements SimpleXMLDocHandler, DocListener {

	/**
	 * DocListener that will listen to the Elements
	 * produced by parsing the HTML.
	 * This can be a com.lowagie.text.Document adding
	 * the elements to a Document directly, or an
	 * HTMLWorker instance strong the objects in a List
	 */
	protected DocListener document;

	/**
	 * The map with all the supported tags.
	 * @since 5.0.6
	 */
	protected Map<String, HTMLTagProcessor> tags;

	/** The object defining all the styles. */
	private StyleSheet style = new StyleSheet();

	/**
	 * Creates a new instance of HTMLWorker
	 * @param document A class that implements <CODE>DocListener</CODE>
	 */
	public HTMLWorker(DocListener document) {
		this(document, null, null);
	}

	/**
	 * Creates a new instance of HTMLWorker
	 * @param document	A class that implements <CODE>DocListener</CODE>
	 * @param tags		A map containing the supported tags
	 * @param style		A StyleSheet
	 * @since 5.0.6
	 */
	public HTMLWorker(DocListener document, Map<String, HTMLTagProcessor> tags, StyleSheet style) {
		this.document = document;
		setSupportedTags(tags);
		setStyleSheet(style);
	}

	/**
	 * Sets the map with supported tags.
	 * @param tags
	 * @since 5.0.6
	 */
	public void setSupportedTags(Map<String, HTMLTagProcessor> tags) {
		if (tags == null)
			tags = new HTMLTagProcessors();
		this.tags = tags;
	}

	/**
	 * Setter for the StyleSheet
	 * @param style the StyleSheet
	 */
	public void setStyleSheet(StyleSheet style) {
		if (style == null)
			style = new StyleSheet();
		this.style = style;
	}

	/**
	 * Parses content read from a java.io.Reader object.
	 * @param reader	the content
	 * @throws IOException
	 */
	public void parse(Reader reader) throws IOException {
		SimpleXMLParser.parse(this, null, reader, true);
	}

	// state machine

	/**
	 * Stack with the Elements that already have been processed.
	 * @since iText 5.0.6 (private => protected)
	 */
	protected Stack<Element> stack = new Stack<Element>();

	/**
	 * Keeps the content of the current paragraph
	 * @since iText 5.0.6 (private => protected)
	 */
	protected Paragraph currentParagraph;

	/**
	 * The current hierarchy chain of tags.
	 * @since 5.0.6
	 */
	private final ChainedProperties chain = new ChainedProperties();

	/**
	 * @see com.sga.text.xml.simpleparser.SimpleXMLDocHandler#startDocument()
	 */
	public void startDocument() {
		HashMap<String, String> attrs = new HashMap<String, String>();
		style.applyStyle(HtmlTags.BODY, attrs);
		chain.addToChain(HtmlTags.BODY, attrs);
	}

    /**
     * @see com.sga.text.xml.simpleparser.SimpleXMLDocHandler#startElement(java.lang.String, java.util.HashMap)
     */
    public void startElement(String tag, HashMap<String, String> attrs) {
		HTMLTagProcessor htmlTag = tags.get(tag);
		if (htmlTag == null) {
			return;
		}
		// apply the styles to attrs
		style.applyStyle(tag, attrs);
		// deal with the style attribute
		StyleSheet.resolveStyleAttribute(attrs, chain);
		// process the tag
		try {
			htmlTag.startElement(this, tag, attrs);
		} catch (DocumentException e) {
			throw new ExceptionConverter(e);
		} catch (IOException e) {
			throw new ExceptionConverter(e);
		}
	}

	/**
	 * @see com.sga.text.xml.simpleparser.SimpleXMLDocHandler#text(java.lang.String)
	 */
	public void text(String content) {
		if (skipText)
			return;
		if (currentParagraph == null) {
			currentParagraph = createParagraph();
		}
		if (!insidePRE) {
			// newlines and carriage returns are ignored
			if (content.trim().length() == 0 && content.indexOf(' ') < 0) {
				return;
			}
			content = HtmlUtilities.eliminateWhiteSpace(content);
		}
		Chunk chunk = createChunk(content);
		currentParagraph.add(chunk);
	}

	/**
	 * @see com.sga.text.xml.simpleparser.SimpleXMLDocHandler#endElement(java.lang.String)
	 */
	public void endElement(String tag) {
		HTMLTagProcessor htmlTag = tags.get(tag);
		if (htmlTag == null) {
			return;
		}
		// process the tag
		try {
			htmlTag.endElement(this, tag);
		} catch (DocumentException e) {
			throw new ExceptionConverter(e);
		}
	}

	/**
	 * @see com.sga.text.xml.simpleparser.SimpleXMLDocHandler#endDocument()
	 */
	public void endDocument() {
		try {
			// flush the stack
			for (int k = 0; k < stack.size(); ++k)
				document.add(stack.elementAt(k));
			// add current paragraph
			if (currentParagraph != null)
				document.add(currentParagraph);
			currentParagraph = null;
		} catch (Exception e) {
			throw new ExceptionConverter(e);
		}
	}

	// stack and current paragraph operations

	/**
	 * Adds a new line to the currentParagraph.
	 * @since 5.0.6
	 */
	public void newLine() {
		if (currentParagraph == null) {
			currentParagraph = new Paragraph();
		}
		currentParagraph.add(createChunk("\n"));
	}

	/**
	 * Flushes the current paragraph, indicating that we're starting
	 * a new block.
	 * If the stack is empty, the paragraph is added to the document.
	 * Otherwise the Paragraph is added to the stack.
	 * @since 5.0.6
	 */
	public void carriageReturn() throws DocumentException {
		if (currentParagraph == null)
			return;
		if (stack.empty())
			document.add(currentParagraph);
		else {
			Element obj = stack.pop();
			if (obj instanceof TextElementArray) {
				TextElementArray current = (TextElementArray) obj;
				current.add(currentParagraph);
			}
			stack.push(obj);
		}
		currentParagraph = null;
	}

	/**
	 * Stacks the current paragraph, indicating that we're starting
	 * a new span.
	 * @since 5.0.6
	 */
	public void flushContent() {
		pushToStack(currentParagraph);
		currentParagraph = new Paragraph();
	}

	/**
	 * Pushes an element to the Stack.
	 * @param element
	 * @since 5.0.6
	 */
	public void pushToStack(Element element) {
		if (element != null)
			stack.push(element);
	}

	/**
	 * Updates the chain with a new tag and new attributes.
	 * @param tag	the new tag
	 * @param attrs	the corresponding attributes
	 * @since 5.0.6
	 */
	public void updateChain(String tag, Map<String, String> attrs) {
		chain.addToChain(tag, attrs);
	}

	/**
	 * Updates the chain by removing a tag.
	 * @param tag	the new tag
	 * @since 5.0.6
	 */
	public void updateChain(String tag) {
		chain.removeChain(tag);
	}

	// providers that help find resources such as images and fonts

	/**
	 * Key used to store the image provider in the providers map.
	 * @since 5.0.6
	 */
	public static final String IMG_PROVIDER = "img_provider";

	/**
	 * Key used to store the image processor in the providers map.
	 * @since 5.0.6
	 */
	public static final String IMG_PROCESSOR = "img_interface";

	/**
	 * Key used to store the image store in the providers map.
	 * @since 5.0.6
	 */
	public static final String IMG_STORE = "img_static";

	/**
	 * Key used to store the image baseurl provider in the providers map.
	 * @since 5.0.6
	 */
	public static final String IMG_BASEURL = "img_baseurl";

	/**
	 * Key used to store the font provider in the providers map.
	 * @since 5.0.6
	 */
	public static final String FONT_PROVIDER = "font_factory";

	/**
	 * Key used to store the link provider in the providers map.
	 * @since 5.0.6
	 */
	public static final String LINK_PROVIDER = "alink_interface";

	/**
	 * Map containing providers such as a FontProvider or ImageProvider.
	 * @since 5.0.6 (renamed from interfaceProps)
	 */
	private Map<String, Object> providers = new HashMap<String, Object>();

	/**
	 * Setter for the providers.
	 * If a FontProvider is added, the ElementFactory is updated.
	 * @param providers a Map with different providers
	 * @since 5.0.6
	 */
	public void setProviders(Map<String, Object> providers) {
		if (providers == null)
			return;
		this.providers = providers;
		FontProvider ff = null;
		if (providers != null)
			ff = (FontProvider) providers.get(FONT_PROVIDER);
		if (ff != null)
			factory.setFontProvider(ff);
	}

	// factory that helps create objects

	/**
	 * Factory that is able to create iText Element objects.
	 * @since 5.0.6
	 */
	private final ElementFactory factory = new ElementFactory();

	/**
	 * Creates a Chunk using the factory.
	 * @param content	the content of the chunk
	 * @return	a Chunk with content
	 * @since 5.0.6
	 */
	public Chunk createChunk(String content) {
		return factory.createChunk(content, chain);
	}
	/**
	 * Creates a Paragraph using the factory.
	 * @return	a Paragraph without any content
	 * @since 5.0.6
	 */
	public Paragraph createParagraph() {
		return factory.createParagraph(chain);
	}
	/**
	 * Creates a List object.
	 * @param tag should be "ol" or "ul"
	 * @return	a List object
	 * @since 5.0.6
	 */
	public com.sga.text.List createList(String tag) {
		return factory.createList(tag, chain);
	}
	/**
	 * Creates a ListItem object.
	 * @return a ListItem object
	 * @since 5.0.6
	 */
	public ListItem createListItem() {
		return factory.createListItem(chain);
	}
	/**
	 * Creates a LineSeparator object.
	 * @param attrs	properties of the LineSeparator
	 * @return a LineSeparator object
	 * @since 5.0.6
	 */
	public LineSeparator createLineSeparator(Map<String, String> attrs) {
		return factory.createLineSeparator(attrs, currentParagraph.getLeading()/2);
	}

	/**
	 * Creates an Image object.
	 * @param attrs properties of the Image
	 * @return an Image object (or null if the Image couldn't be found)
	 * @throws DocumentException
	 * @throws IOException
	 * @since 5.0.6
	 */
	public Image createImage(Map<String, String> attrs) throws DocumentException, IOException {
		String src = attrs.get(HtmlTags.SRC);
		if (src == null)
			return null;
		Image img = factory.createImage(
				src, attrs, chain, document,
				(ImageProvider)providers.get(IMG_PROVIDER),
				(ImageStore)providers.get(IMG_STORE),
				(String)providers.get(IMG_BASEURL));
		return img;
	}

	/**
	 * Creates a Cell.
	 * @param tag	the tag
	 * @return	a CellWrapper object
	 * @since 5.0.6
	 */
	public CellWrapper createCell(String tag) {
		return new CellWrapper(tag, chain);
	}

	// processing objects

	/**
	 * Adds a link to the current paragraph.
	 * @since 5.0.6
	 */
	public void processLink() {
		if (currentParagraph == null) {
			currentParagraph = new Paragraph();
		}
		// The link provider allows you to do additional processing
		LinkProcessor i = (LinkProcessor) providers.get(HTMLWorker.LINK_PROVIDER);
		if (i == null || !i.process(currentParagraph, chain)) {
			// sets an Anchor for all the Chunks in the current paragraph
			String href = chain.getProperty(HtmlTags.HREF);
			if (href != null) {
				for (Chunk ck : currentParagraph.getChunks()) {
					ck.setAnchor(href);
				}
			}
		}
		// a link should be added to the current paragraph as a phrase
		if (stack.isEmpty()) {
			// no paragraph to add too, 'a' tag is first element
			Paragraph tmp = new Paragraph(new Phrase(currentParagraph));
			currentParagraph = tmp;
		} else {
			Paragraph tmp = (Paragraph) stack.pop();
			tmp.add(new Phrase(currentParagraph));
			currentParagraph = tmp;
		}
	}

	/**
	 * Fetches the List from the Stack and adds it to
	 * the TextElementArray on top of the Stack,
	 * or to the Document if the Stack is empty.
	 * @throws DocumentException
	 * @since 5.0.6
	 */
	public void processList() throws DocumentException {
		if (stack.empty())
			return;
		Element obj = stack.pop();
		if (!(obj instanceof com.sga.text.List)) {
			stack.push(obj);
			return;
		}
		if (stack.empty())
			document.add(obj);
		else
			((TextElementArray) stack.peek()).add(obj);
	}

	/**
	 * Looks for the List object on the Stack,
	 * and adds the ListItem to the List.
	 * @throws DocumentException
	 * @since 5.0.6
	 */
	public void processListItem() throws DocumentException {
		if (stack.empty())
			return;
		Element obj = stack.pop();
		if (!(obj instanceof ListItem)) {
			stack.push(obj);
			return;
		}
		if (stack.empty()) {
			document.add(obj);
			return;
		}
		ListItem item = (ListItem) obj;
		Element list = stack.pop();
		if (!(list instanceof com.sga.text.List)) {
			stack.push(list);
			return;
		}
		((com.sga.text.List) list).add(item);
		item.adjustListSymbolFont();
		stack.push(list);
	}

	/**
	 * Processes an Image.
	 * @param img
	 * @param attrs
	 * @throws DocumentException
	 * @since	5.0.6
	 */
	public void processImage(Image img, Map<String, String> attrs) throws DocumentException {
		ImageProcessor processor = (ImageProcessor)providers.get(HTMLWorker.IMG_PROCESSOR);
		if (processor == null || !processor.process(img, attrs, chain, document)) {
			String align = attrs.get(HtmlTags.ALIGN);
			if (align != null) {
				carriageReturn();
			}
			if (currentParagraph == null) {
				currentParagraph = createParagraph();
			}
			currentParagraph.add(new Chunk(img, 0, 0, true));
			currentParagraph.setAlignment(HtmlUtilities.alignmentValue(align));
			if (align != null) {
				carriageReturn();
			}
		}
	}

	/**
	 * Processes the Table.
	 * @throws DocumentException
	 * @since 5.0.6
	 */
	public void processTable() throws DocumentException{
		TableWrapper table = (TableWrapper) stack.pop();
		PdfPTable tb = table.createTable();
		tb.setSplitRows(true);
		if (stack.empty())
			document.add(tb);
		else
			((TextElementArray) stack.peek()).add(tb);
	}

	/**
	 * Gets the TableWrapper from the Stack and adds a new row.
	 * @since 5.0.6
	 */
	public void processRow() {
		ArrayList<PdfPCell> row = new ArrayList<PdfPCell>();
        ArrayList<Float> cellWidths = new ArrayList<Float>();
        boolean percentage = false;
        float width;
        float totalWidth = 0;
        int zeroWidth = 0;
		TableWrapper table = null;
		while (true) {
			Element obj = stack.pop();
			if (obj instanceof CellWrapper) {
                CellWrapper cell = (CellWrapper)obj;
                width = cell.getWidth();
                cellWidths.add(new Float(width));
                percentage |= cell.isPercentage();
                if (width == 0) {
                	zeroWidth++;
                }
                else {
                	totalWidth += width;
                }
                row.add(cell.getCell());
			}
			if (obj instanceof TableWrapper) {
				table = (TableWrapper) obj;
				break;
			}
		}
        table.addRow(row);
        if (cellWidths.size() > 0) {
            // cells come off the stack in reverse, naturally
        	totalWidth = 100 - totalWidth;
            Collections.reverse(cellWidths);
            float[] widths = new float[cellWidths.size()];
            for (int i = 0; i < widths.length; i++) {
                widths[i] = cellWidths.get(i).floatValue();
                if (widths[i] == 0 && percentage && zeroWidth > 0) {
                	widths[i] = totalWidth / zeroWidth;
                }
            }
            table.setColWidths(widths);
        }
		stack.push(table);
	}

	// state variables and methods

	/** Stack to keep track of table tags. */
	private final Stack<boolean[]> tableState = new Stack<boolean[]>();

	/** Boolean to keep track of TR tags. */
	private boolean pendingTR = false;

	/** Boolean to keep track of TD and TH tags */
	private boolean pendingTD = false;

	/** Boolean to keep track of LI tags */
	private boolean pendingLI = false;

	/**
	 * Boolean to keep track of PRE tags
	 * @since 5.0.6 renamed from isPRE
	 */
	private boolean insidePRE = false;

	/**
	 * Indicates if text needs to be skipped.
	 * @since iText 5.0.6 (private => protected)
	 */
	protected boolean skipText = false;

	/**
	 * Pushes the values of pendingTR and pendingTD
	 * to a state stack.
	 * @since 5.0.6
	 */
	public void pushTableState() {
		tableState.push(new boolean[] { pendingTR, pendingTD });
	}

	/**
	 * Pops the values of pendingTR and pendingTD
	 * from a state stack.
	 * @since 5.0.6
	 */
	public void popTableState() {
		boolean[] state = tableState.pop();
		pendingTR = state[0];
		pendingTD = state[1];
	}

	/**
	 * @return the pendingTR
	 * @since 5.0.6
	 */
	public boolean isPendingTR() {
		return pendingTR;
	}

	/**
	 * @param pendingTR the pendingTR to set
	 * @since 5.0.6
	 */
	public void setPendingTR(boolean pendingTR) {
		this.pendingTR = pendingTR;
	}

	/**
	 * @return the pendingTD
	 * @since 5.0.6
	 */
	public boolean isPendingTD() {
		return pendingTD;
	}

	/**
	 * @param pendingTD the pendingTD to set
	 * @since 5.0.6
	 */
	public void setPendingTD(boolean pendingTD) {
		this.pendingTD = pendingTD;
	}

	/**
	 * @return the pendingLI
	 * @since 5.0.6
	 */
	public boolean isPendingLI() {
		return pendingLI;
	}

	/**
	 * @param pendingLI the pendingLI to set
	 * @since 5.0.6
	 */
	public void setPendingLI(boolean pendingLI) {
		this.pendingLI = pendingLI;
	}

	/**
	 * @return the insidePRE
	 * @since 5.0.6
	 */
	public boolean isInsidePRE() {
		return insidePRE;
	}

	/**
	 * @param insidePRE the insidePRE to set
	 * @since 5.0.6
	 */
	public void setInsidePRE(boolean insidePRE) {
		this.insidePRE = insidePRE;
	}

	/**
	 * @return the skipText
	 * @since 5.0.6
	 */
	public boolean isSkipText() {
		return skipText;
	}

	/**
	 * @param skipText the skipText to set
	 * @since 5.0.6
	 */
	public void setSkipText(boolean skipText) {
		this.skipText = skipText;
	}

	// static methods to parse HTML to a List of Element objects.

	/** The resulting list of elements. */
	protected List<Element> objectList;

	/**
	 * Parses an HTML source to a List of Element objects
	 * @param reader	the HTML source
	 * @param style		a StyleSheet object
	 * @return a List of Element objects
	 * @throws IOException
	 */
	public static List<Element> parseToList(Reader reader, StyleSheet style)
			throws IOException {
		return parseToList(reader, style, null);
	}

	/**
	 * Parses an HTML source to a List of Element objects
	 * @param reader	the HTML source
	 * @param style		a StyleSheet object
	 * @param providers	map containing classes with extra info
	 * @return a List of Element objects
	 * @throws IOException
	 */
	public static List<Element> parseToList(Reader reader, StyleSheet style,
			HashMap<String, Object> providers) throws IOException {
		return parseToList(reader, style, null, providers);
	}

	/**
	 * Parses an HTML source to a List of Element objects
	 * @param reader	the HTML source
	 * @param style		a StyleSheet object
	 * @param tags		a map containing supported tags and their processors
	 * @param providers	map containing classes with extra info
	 * @return a List of Element objects
	 * @throws IOException
	 * @since 5.0.6
	 */
	public static List<Element> parseToList(Reader reader, StyleSheet style,
			Map<String, HTMLTagProcessor> tags, HashMap<String, Object> providers) throws IOException {
		HTMLWorker worker = new HTMLWorker(null, tags, style);
		worker.document = worker;
		worker.setProviders(providers);
		worker.objectList = new ArrayList<Element>();
		worker.parse(reader);
		return worker.objectList;
	}

	// DocListener interface

	/**
	 * @see com.sga.text.ElementListener#add(com.sga.text.Element)
	 */
	public boolean add(Element element) throws DocumentException {
		objectList.add(element);
		return true;
	}

	/**
	 * @see com.sga.text.DocListener#close()
	 */
	public void close() {
	}

	/**
	 * @see com.sga.text.DocListener#newPage()
	 */
	public boolean newPage() {
		return true;
	}

	/**
	 * @see com.sga.text.DocListener#open()
	 */
	public void open() {
	}

	/**
	 * @see com.sga.text.DocListener#resetPageCount()
	 */
	public void resetPageCount() {
	}

	/**
	 * @see com.sga.text.DocListener#setMarginMirroring(boolean)
	 */
	public boolean setMarginMirroring(boolean marginMirroring) {
		return false;
	}

	/**
     * @see com.sga.text.DocListener#setMarginMirroring(boolean)
	 * @since	2.1.6
	 */
	public boolean setMarginMirroringTopBottom(boolean marginMirroring) {
		return false;
	}

	/**
	 * @see com.sga.text.DocListener#setMargins(float, float, float, float)
	 */
	public boolean setMargins(float marginLeft, float marginRight,
			float marginTop, float marginBottom) {
		return true;
	}

	/**
	 * @see com.sga.text.DocListener#setPageCount(int)
	 */
	public void setPageCount(int pageN) {
	}

	/**
	 * @see com.sga.text.DocListener#setPageSize(com.sga.text.Rectangle)
	 */
	public boolean setPageSize(Rectangle pageSize) {
		return true;
	}

	// deprecated methods

	/**
	 * Sets the providers.
	 * @deprecated use setProviders() instead
	 */
	@Deprecated
	public void setInterfaceProps(HashMap<String, Object> providers) {
		setProviders(providers);
	}
	/**
	 * Gets the providers
	 * @deprecated use getProviders() instead
	 */
	@Deprecated
	public Map<String, Object> getInterfaceProps() {
		return providers;
	}

}
