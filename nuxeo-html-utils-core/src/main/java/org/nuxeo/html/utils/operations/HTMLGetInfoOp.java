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

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.html.utils.HTMLParser;

/**
 * Parses the HTML input (blob, Document or String) and return a JSON String containing an object with at least a
 * "title" field. Also contains the contant of any "meta" field passed (using the name of the tag)
 * 
 * @since 8.1
 */
@Operation(id = HTMLGetInfoOp.ID, category = Constants.CAT_SERVICES, label = "HTML: Get Info", description = "Returns a JSON string of an object whose properties are title and every meta required (comma-separated list). For example, {title: \"The title\", keywords: \"kw1, kw2\"}. If input is a Document, optional xpath is the field holding the blob. The operation handles Note documents (whatever value in xpath).")
public class HTMLGetInfoOp {

    public static final String ID = "HTML.GetInfo";

    protected HTMLParser htmlParser;

    @Param(name = "metaList", required = false)
    String metaList;

    @Param(name = "xpath", required = false)
    String xpath;

    protected JSONObject buildInfo() throws JSONException {

        JSONObject obj = new JSONObject();

        obj.put("title", htmlParser.getTitle());

        if (StringUtils.isNotBlank(metaList)) {
            String[] metas = metaList.split(",");
            for (String oneMeta : metas) {
                oneMeta = oneMeta.trim();
                obj.put(oneMeta, htmlParser.getMetaValue(oneMeta));
            }
        }

        return obj;
    }

    @OperationMethod
    public String run(Blob inBlob) throws IOException, JSONException {

        JSONObject obj = new JSONObject();

        if (inBlob != null) {
            htmlParser = new HTMLParser(inBlob);
            obj = buildInfo();
        }

        return obj.toString();
    }

    @OperationMethod
    public String run(DocumentModel inDoc) throws IOException, JSONException {

        JSONObject obj = new JSONObject();

        if (inDoc != null) {
            htmlParser = new HTMLParser(inDoc, xpath);
            obj = buildInfo();

        }

        return obj.toString();
    }

    @OperationMethod
    public String run(String inStr) throws IOException, JSONException {

        JSONObject obj = new JSONObject();

        if (inStr != null) {
            htmlParser = new HTMLParser(inStr);
            obj = buildInfo();
        }

        return obj.toString();
    }

}
