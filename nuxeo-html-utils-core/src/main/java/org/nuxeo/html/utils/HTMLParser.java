/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package org.nuxeo.html.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.BlobHolderAdapterService;
import org.nuxeo.runtime.api.Framework;

import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

/**
 * Wrapper around Jericho HTML Parser.
 * <p>
 * This class extracts links, plain text, etc. from an html source, Blob, String or Document
 * <p>
 * Thanks to http://jericho.htmlparser.net and their example code.
 * 
 * @since 8.1
 */
public class HTMLParser {

    public static final String[] HANDLED_LINKS_ATTRIBUTES = { "href", "src" };

    Source source;

    protected ArrayList<LinkInfo> links;

    protected ArrayList<String> imgSrc;

    public HTMLParser(Blob inBlob) throws IOException {
        source = new Source(inBlob.getStream());
    }

    public HTMLParser(String inHTML) throws IOException {
        source = new Source(inHTML);
    }

    public HTMLParser(DocumentModel inDoc) throws IOException {
        this(inDoc, null);
    }

    /**
     * In this constructor, if the code fails top ge a blob from the xpath (or if it is empty and file:content is null),
     * the method tries checks if the Document is Note (has the "note" schema, more precisely)
     * 
     * @param inDoc
     * @param inXPath
     * @throws IOException
     */
    public HTMLParser(DocumentModel inDoc, String inXPath) throws IOException {

        Blob blob = null;

        if (StringUtils.isBlank(inXPath)) {
            if (inDoc.hasSchema("note")) {
                BlobHolderAdapterService bhas = Framework.getLocalService(BlobHolderAdapterService.class);
                BlobHolder bh = bhas.getBlobHolderAdapter(inDoc);
                blob = bh.getBlob();
            } else {
                blob = (Blob) inDoc.getPropertyValue("file:content");
            }
        } else {
            blob = (Blob) inDoc.getPropertyValue(inXPath);
        }

        source = new Source(blob.getStream());
    }

    /**
     * Returns every links of type <code>HANDLED_LINKS_ATTRIBUTES</code>
     * <p>
     * Excludes comments, of course.
     * 
     * @return
     * @since 8.1
     */
    public ArrayList<LinkInfo> getLinks() {

        if (links == null) {

            String text, link;
            List<Element> linkElements;
            links = new ArrayList<LinkInfo>();
            for (String attr : HANDLED_LINKS_ATTRIBUTES) {
                linkElements = source.getAllElements(attr, null);
                for (Element linkElement : linkElements) {
                    link = linkElement.getAttributeValue(attr);
                    text = linkElement.getContent().getTextExtractor().toString();
                    links.add(new LinkInfo(linkElement.getName(), attr, text, link));
                }
            }
        }

        return links;

    }

    /**
     * Returns every links of type <code>src</code> inside an <code>img</code> tag.
     * <p>
     * Calling @{getLinks} and walking thru the resulting list would do the same.
     * <p>
     * Excludes comments, of course.
     * 
     * @return
     * @since 8.1
     */
    public List<String> getImgSrc() {

        if (imgSrc == null) {
            imgSrc = new ArrayList<String>();
            String src;
            List<Element> linkElements = source.getAllElements(HTMLElementName.IMG);
            for (Element linkElement : linkElements) {
                src = linkElement.getAttributeValue("src");
                if (StringUtils.isNotBlank(src)) {
                    imgSrc.add(src);
                }
            }
        }

        return imgSrc;
    }

    /**
     * Returns the plain text of the html using:
     * <ul>
     * <li>\n as line separator</li>
     * <li><code>includeHyperlinkURLs</code>: <code>false</code></li>
     * <li><code>includeAlternateText</code>: <code>false</code></li>
     * <li><code>convertNonBreakingSpaces</code>: <code>false</code></li>
     * <li></li>
     * <li></li>
     * </ul>
     * 
     * @return the plain text
     * @since 8.1
     */
    public String getPlainText() {
        return getPlainText("\n", false, false, false);
    }

    /**
     * Returns the plain text of the HTML. More or less text depending on the parameters.
     * 
     * @param lineSeparator
     * @param includeHyperlinkURLs
     * @param includeAlternateText
     * @param convertNonBreakingSpaces
     * @return the plain te
     * @since 8.1
     */
    public String getPlainText(String lineSeparator, boolean includeHyperlinkURLs, boolean includeAlternateText,
            boolean convertNonBreakingSpaces) {

        if (StringUtils.isBlank(lineSeparator)) {
            lineSeparator = "\n";
        }

        source.fullSequentialParse();
        Renderer renderer = source.getRenderer();

        renderer.setIncludeHyperlinkURLs(includeHyperlinkURLs);
        renderer.setIncludeAlternateText(includeAlternateText);
        renderer.setConvertNonBreakingSpaces(convertNonBreakingSpaces);

        renderer.setDecorateFontStyles(false);
        renderer.setMaxLineLength(Integer.MAX_VALUE);

        renderer.setNewLine(lineSeparator);

        return renderer.toString();

    }

    /**
     * Return the title of the document. If there is no title, returns ""
     * 
     * @return the title of the document, or ""
     * @since 8.1
     */
    public String getTitle() {

        String title = null;

        source.fullSequentialParse();
        Element titleElement = source.getFirstElement(HTMLElementName.TITLE);
        if (titleElement != null) {

            // TITLE element never contains other tags so just decode it collapsing whitespace:
            title = CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent());
        }

        return title == null ? "" : title;
    }

    /**
     * Extract the value of the key, return it. Returns "" if not found.
     * <p>
     * For example, to get the keywords, call <code>getMetaValue("keywords")</code>. For the description:
     * <code>getMetaValue("description")</code>.
     * 
     * @param key
     * @return the value of the meta. "" if not found
     * @since 8.1
     */
    public String getMetaValue(String key) {

        String value = null;

        source.fullSequentialParse();
        int max = source.length();
        for (int pos = 0; pos < max;) {
            StartTag startTag = source.getNextStartTag(pos, "name", key, false);
            if (startTag == null) {
                return "";
            }
            if (startTag.getName() == HTMLElementName.META) {
                value = startTag.getAttributeValue("content"); // Attribute values are automatically decoded
                break;
            }
            pos = startTag.getEnd();
        }

        return value == null ? "" : value;
    }
}
