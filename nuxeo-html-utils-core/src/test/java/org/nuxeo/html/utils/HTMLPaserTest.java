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

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @since 8.1
 */

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "org.nuxeo.html.utils.nuxeo-html-utils-core" })
public class HTMLPaserTest {

    protected static final String HTML_TEST_FILE = "files/test.html";

    protected static FileBlob HTML_FILEBLOB;

    @Inject
    CoreSession coreSession;

    @Inject
    AutomationService automationService;

    @Before
    public void setup() {

        File f = FileUtils.getResourceFileFromContext(HTML_TEST_FILE);
        HTML_FILEBLOB = new FileBlob(f);
    }

    // If a value is null, it is not tested
    protected boolean linkListContains(ArrayList<LinkInfo> inArray, String tag, String attribute, String text,
            String link) {

        for (LinkInfo li : inArray) {

            if (li.compareIgnoreNull(tag, attribute, text, link)) {
                return true;
            }
        }

        return false;
    }

    // If a value is null, it is not tested
    protected boolean jsonArrayContains(JSONArray inArray, String tag, String attribute, String text,
            String link) throws JSONException {

        LinkInfo li;
        int length = inArray.length();
        for(int i = 0; i < length; ++i) {
            JSONObject obj = (JSONObject) inArray.get(i);
            li = new LinkInfo(obj.getString("tag"), obj.getString("attribute"), obj.getString("text"), obj.getString("link"));
            if (li.compareIgnoreNull(tag, attribute, text, link)) {
                return true;
            }
            
        }

        return false;
    }

    @Test
    public void testGetLinks() throws Exception {

        HTMLParser hp = new HTMLParser(HTML_FILEBLOB);

        ArrayList<LinkInfo> links = hp.getLinks();

        // Does not contains the links declared inside comments
        assertEquals(5, links.size());
        // Check values
        assertTrue(linkListContains(links, "link", "href", null, "does/not/exist/style.css"));
        assertTrue(linkListContains(links, "a", "href", "Here, a link to nuxeo.com", "http://nuxeo.com"));
        assertTrue(linkListContains(links, "a", "href", "(This does not exist in the test files)", "sub1/sub2/sub.html"));
        assertTrue(linkListContains(links, "script", "src", null, null));
        assertTrue(linkListContains(links, "img", "src", null, "http://does.not.exist.com/just/for/test.jpg"));

    }

    @Test
    public void testGetLinksOperation() throws Exception {

        OperationChain chain;
        OperationContext ctx = new OperationContext(coreSession);

        ctx.setInput(HTML_FILEBLOB);
        chain = new OperationChain("testGetLinksOp");
        chain.add(HTMLGetLinksOp.ID).set("getAll", true);
        String result = (String) automationService.run(ctx, chain);

        assertNotNull(result);

        assertNotEquals("", result);
        // It is a JSON array of 3 elements
        JSONArray array = new JSONArray(result);
        int size = array.length();
        // Does not contains the links declared inside comments
        assertEquals(5, size);
        // Check values
        assertTrue(jsonArrayContains(array, "link", "href", null, "does/not/exist/style.css"));
        assertTrue(jsonArrayContains(array, "a", "href", "Here, a link to nuxeo.com", "http://nuxeo.com"));
        assertTrue(jsonArrayContains(array, "a", "href", "(This does not exist in the test files)", "sub1/sub2/sub.html"));
        assertTrue(jsonArrayContains(array, "script", "src", null, null));
        assertTrue(jsonArrayContains(array, "img", "src", null, "http://does.not.exist.com/just/for/test.jpg"));
    }
    
    @Test
    public void testGetImgSrc() throws Exception {
        
        HTMLParser hp = new HTMLParser(HTML_FILEBLOB);
        
        List<String> src = hp.getImgSrc();
        assertEquals(1, src.size());
        assertTrue(src.contains("http://does.not.exist.com/just/for/test.jpg"));
    }
    
    protected void testPlainText(String plainText, boolean hasHyperLinks) throws Exception {
        
        // Contains the text
        assertTrue(plainText.indexOf("Welcome to the HTMLParserTest") > -1);
        assertTrue(plainText.indexOf("This does not exist in the test files") > -1);
        
        // Does not contains the tags, the comments, ...
        assertTrue(plainText.indexOf("<script") < 0);
        assertTrue(plainText.indexOf("These one should not be listed") < 0);
        
        // Links
        if(hasHyperLinks) {
            assertTrue(plainText.indexOf("http://nuxeo.com") > -1);
        } else {
            assertTrue(plainText.indexOf("http://nuxeo.com") < 0);
        }
        
        
    }
    
    @Test
    public void testGetPlainText() throws Exception {
        
        HTMLParser hp = new HTMLParser(HTML_FILEBLOB);
        
        // Default parameters
        String plainText = hp.getPlainText();
        testPlainText(plainText, false);
        
        // Also get the hyperlinks
        plainText = hp.getPlainText(null, true, false, false);
        testPlainText(plainText, true);
        
    }

    @Test
    public void testGetPlainTextOperation() throws Exception {

        OperationChain chain;
        OperationContext ctx = new OperationContext(coreSession);

        ctx.setInput(HTML_FILEBLOB);
        chain = new OperationChain("testGetPlainTextOp-1");
        chain.add(HTMLGetPlainTextOp.ID);
        String result = (String) automationService.run(ctx, chain);
        assertNotNull(result);
        testPlainText(result, false);
        
        // With hyperLinks
        ctx.setInput(HTML_FILEBLOB);
        chain = new OperationChain("testGetPlainTextOp-2");
        chain.add(HTMLGetPlainTextOp.ID).set("includeHyperlinkURLs", true);
        result = (String) automationService.run(ctx, chain);
        assertNotNull(result);
        testPlainText(result, true);
    }
}
