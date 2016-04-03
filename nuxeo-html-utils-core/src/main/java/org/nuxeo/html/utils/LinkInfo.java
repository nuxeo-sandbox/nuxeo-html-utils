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

/**
 * A utility class containing misc. information about a link
 * 
 * @since 8.1
 */
public class LinkInfo {

    protected String tag;

    protected String attribute;

    protected String text;

    protected String link;

    public LinkInfo(String tag, String attribute, String text, String link) {
        this.tag = tag;
        this.attribute = attribute;
        this.text = text;
        this.link = link;
    }

    public String getTag() {
        return tag;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getText() {
        return text;
    }

    public String getLink() {
        return link;
    }

    public String toString() {
        return "tag: " + tag + " - attribute: " + attribute + " - text: " + text + " - link: " + link;
    }

    public boolean compareIgnoreNull(String tag, String attribute, String text, String link) {

        if (tag != null) {
            if (this.tag == null || !this.tag.equals(tag)) {
                return false;
            }
        }
        if (attribute != null) {
            if (this.attribute == null || !this.attribute.equals(attribute)) {
                return false;
            }
        }
        if (text != null) {
            if (this.text == null || !this.text.equals(text)) {
                return false;
            }
        }
        if (link != null) {
            if (this.link == null || !this.link.equals(link)) {
                return false;
            }
        }

        return true;
    }
}
