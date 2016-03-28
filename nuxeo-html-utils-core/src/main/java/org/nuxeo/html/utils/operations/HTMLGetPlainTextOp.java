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

package org.nuxeo.html.utils.operations;

import java.io.IOException;

import org.json.JSONException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.html.utils.HTMLParser;

/**
 * Returns the plain text of the html.
 * <p>
 * If the input is a Document, optional xpath is the field holding the blob. Let empty if using a Note document.
 * 
 * @since 8.1
 */
@Operation(id = HTMLGetPlainTextOp.ID, category = Constants.CAT_CONVERSION, label = "HTML: Get Plain Text", description = "Returns the plain text of the HTML. If input is a Document, optional xpath is the field holding the blob. The operaiton handles Note documents (whatever value in xpath).")
public class HTMLGetPlainTextOp {

    public static final String ID = "HTML.GetPlainText";

    @Param(name = "includeHyperlinkURLs", required = false, values = { "false" })
    boolean includeHyperlinkURLs = false;

    @Param(name = "includeAlternateText", required = false, values = { "false" })
    boolean includeAlternateText = false;

    @Param(name = "convertNonBreakingSpaces", required = false, values = { "false" })
    boolean convertNonBreakingSpaces = false;

    @Param(name = "lineSeparator", required = false)
    String lineSeparator = null;

    @Param(name = "xpath", required = false)
    String xpath;

    @OperationMethod
    public String run(Blob inBlob) throws IOException, JSONException {

        String plainText = "";
        if (inBlob != null) {
            HTMLParser hp = new HTMLParser(inBlob);

            plainText = hp.getPlainText(lineSeparator, includeHyperlinkURLs, includeAlternateText,
                    convertNonBreakingSpaces);
        }
        return plainText;

    }

    @OperationMethod
    public String run(String inStr) throws IOException, JSONException {

        String plainText = "";
        if (inStr != null) {
            HTMLParser hp = new HTMLParser(inStr);

            plainText = hp.getPlainText(lineSeparator, includeHyperlinkURLs, includeAlternateText,
                    convertNonBreakingSpaces);
        }
        return plainText;

    }

    @OperationMethod
    public String run(DocumentModel inDoc) throws IOException, JSONException {

        String plainText = "";
        if (inDoc != null) {
            HTMLParser hp = new HTMLParser(inDoc, xpath);

            plainText = hp.getPlainText(lineSeparator, includeHyperlinkURLs, includeAlternateText,
                    convertNonBreakingSpaces);
        }
        return plainText;

    }

}
