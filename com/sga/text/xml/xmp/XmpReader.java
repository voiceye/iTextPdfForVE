/*
 * $Id: XmpReader.java 4125 2009-12-02 18:28:57Z blowagie $
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
package com.sga.text.xml.xmp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sga.text.ExceptionConverter;
import com.sga.text.xml.XmlDomWriter;

/**
 * Reads an XMP stream into an org.w3c.dom.Document objects.
 * Allows you to replace the contents of a specific tag.
 * @since 2.1.3
 */

public class XmpReader {

    private Document domDocument;
    
    /**
     * Constructs an XMP reader
     * @param	bytes	the XMP content
     * @throws ExceptionConverter 
     * @throws IOException 
     * @throws SAXException 
     */
	public XmpReader(byte[] bytes) throws SAXException, IOException {
		try {
	        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
	        fact.setNamespaceAware(true);
			DocumentBuilder db = fact.newDocumentBuilder();
	        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
	        domDocument = db.parse(bais);
		} catch (ParserConfigurationException e) {
			throw new ExceptionConverter(e);
		}
	}
	
	/**
	 * Replaces the content of a tag.
	 * @param	namespaceURI	the URI of the namespace
	 * @param	localName		the tag name
	 * @param	value			the new content for the tag
	 * @return	true if the content was successfully replaced
	 * @since	2.1.6 the return type has changed from void to boolean
	 */
	public boolean replaceNode(String namespaceURI, String localName, String value) {
		NodeList nodes = domDocument.getElementsByTagNameNS(namespaceURI, localName);
		Node node;
		if (nodes.getLength() == 0)
			return false;
		for (int i = 0; i < nodes.getLength(); i++) {
			node = nodes.item(i);
			setNodeText(domDocument, node, value);
		}
		return true;
	}

	/**
	 * Replaces the content of an attribute in the description tag.
	 * @param	namespaceURI	the URI of the namespace
	 * @param	localName		the tag name
	 * @param	value			the new content for the tag
	 * @return	true if the content was successfully replaced
	 * @since	5.0.0 the return type has changed from void to boolean
	 */
	public boolean replaceDescriptionAttribute(String namespaceURI, String localName, String value) {
		NodeList descNodes = domDocument.getElementsByTagNameNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","Description");
		if(descNodes.getLength() == 0) {
			return false;
		}
		Node node;
		for(int i = 0; i < descNodes.getLength(); i++) {
			node = descNodes.item(i);
			Node attr = node.getAttributes().getNamedItemNS(namespaceURI, localName);
			if(attr != null) {
				attr.setNodeValue(value);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds a tag.
	 * @param	namespaceURI	the URI of the namespace
	 * @param	parent			the tag name of the parent
	 * @param	localName		the name of the tag to add
	 * @param	value			the new content for the tag
	 * @return	true if the content was successfully added
	 * @since	2.1.6
	 */
	public boolean add(String parent, String namespaceURI, String localName, String value) {
		NodeList nodes = domDocument.getElementsByTagName(parent);
		if (nodes.getLength() == 0)
			return false;
		Node pNode;
		Node node;
		for (int i = 0; i < nodes.getLength(); i++) {
			pNode = nodes.item(i);
			NamedNodeMap attrs = pNode.getAttributes();
			for (int j = 0; j < attrs.getLength(); j++) {
				node = attrs.item(j);
				if (namespaceURI.equals(node.getNodeValue())) {
					node = domDocument.createElement(localName);
					node.appendChild(domDocument.createTextNode(value));
					pNode.appendChild(node);
					return true;
				}
			}
		}
		return false;
	}
	
    /**
     * Sets the text of this node. All the child's node are deleted and a new
     * child text node is created.
     * @param domDocument the <CODE>Document</CODE> that contains the node
     * @param n the <CODE>Node</CODE> to add the text to
     * @param value the text to add
     */
    public boolean setNodeText(Document domDocument, Node n, String value) {
        if (n == null)
            return false;
        Node nc = null;
        while ((nc = n.getFirstChild()) != null) {
            n.removeChild(nc);
        }
        n.appendChild(domDocument.createTextNode(value));
        return true;
    }
	
    /**
     * Writes the document to a byte array.
     */
	public byte[] serializeDoc() throws IOException {
		XmlDomWriter xw = new XmlDomWriter();
        ByteArrayOutputStream fout = new ByteArrayOutputStream();
        xw.setOutput(fout, null);
        fout.write(XmpWriter.XPACKET_PI_BEGIN.getBytes("UTF-8"));
        fout.flush();
        NodeList xmpmeta = domDocument.getElementsByTagName("x:xmpmeta");
        xw.write(xmpmeta.item(0));
        fout.flush();
		for (int i = 0; i < 20; i++) {
			fout.write(XmpWriter.EXTRASPACE.getBytes());
		}
        fout.write(XmpWriter.XPACKET_PI_END_W.getBytes());
        fout.close();
        return fout.toByteArray();
	}
}
