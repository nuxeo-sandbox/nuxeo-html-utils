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

import org.nuxeo.ecm.core.api.Blob;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

/**
 * 
 * @since 8.1
 */
public class HTMLParser {
    
    public static final String [] HANDLED_LINKS_ATTRIBUTES = {"href", "src"};
    
    Source source;
    
    protected ArrayList<LinkInfo> links;
    
    public HTMLParser(Blob inBlob) throws IOException {
        source = new Source(inBlob.getStream());
    }
    
    public ArrayList<LinkInfo> getLinks() {
        
        if(links == null) {

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

}
