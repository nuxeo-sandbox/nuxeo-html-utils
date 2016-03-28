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
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.html.utils.HTMLParser;
import org.nuxeo.html.utils.LinkInfo;

/**
 * Parses the html for every tag with a "src" or a "href" attribute, and returns a JSON string of an array of objects
 * with tag, attribute, text and link fields.
 * 
 * @since 8.1
 */
@Operation(id = HTMLGetLinksOp.ID, category = Constants.CAT_SERVICES, label = "HTML: Get Links", description = "Returns a JSON string of an array of objects with tag, attribute, text and link fields (returns all and src)")
public class HTMLGetLinksOp {

    public static final String ID = "HTML.GetLinks";

    @OperationMethod
    public String run(Blob inBlob) throws IOException, JSONException {

        JSONArray array = new JSONArray();

        if (inBlob != null) {
            HTMLParser hp = new HTMLParser(inBlob);
            ArrayList<LinkInfo> links = hp.getLinks();

            for (LinkInfo li : links) {
                JSONObject object = new JSONObject();
                object.put("tag", li.getTag());
                object.put("attribute", li.getAttribute());
                object.put("text", li.getText());
                object.put("link", li.getLink());

                array.put(object);
            }
        }

        return array.toString();

    }

    @OperationMethod
    public String run(String inHTML) throws IOException, JSONException {

        JSONArray array = new JSONArray();

        if (inHTML != null) {
            HTMLParser hp = new HTMLParser(inHTML);
            ArrayList<LinkInfo> links = hp.getLinks();

            for (LinkInfo li : links) {
                JSONObject object = new JSONObject();
                object.put("tag", li.getTag());
                object.put("attribute", li.getAttribute());
                object.put("text", li.getText());
                object.put("link", li.getLink());

                array.put(object);
            }
        }

        return array.toString();

    }

}
