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
 *     thibaud
 */
package org.nuxeo.html.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PropertyException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.BlobHolderAdapterService;
import org.nuxeo.runtime.api.Framework;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Source;

/**
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

    public String getPlainText() {
        return getPlainText("\n", false, false, false);
    }

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
}
