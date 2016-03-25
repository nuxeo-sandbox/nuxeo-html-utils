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

import org.json.JSONArray;
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
 * 
 * @since 8.1
 */

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "org.nuxeo.html.utils.nuxeo-html-utils-core" })
public class HTMLPaserTest {
    
    protected static final String HTML_TEST_FILE= "files/test.html";
    
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
    protected boolean linkListContains(ArrayList<LinkInfo> inArray, String tag, String attribute, String text, String link) {
                
        for(LinkInfo li : inArray) {
            
            if(li.compareIgnoreNull(tag, attribute, text, link)) {
                return true;
            }
        }
        
        return false;
    }

    @Test
    public void testGetLinks() throws Exception {
        
        HTMLParser hp = new HTMLParser(HTML_FILEBLOB);
        
        ArrayList<LinkInfo> links = hp.getLinks();
        
        assertEquals(5, links.size());
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
        chain = new OperationChain("testChain");
        chain.add(HTMLGetLinksOp.ID).set("getAll", true);
        String result = (String) automationService.run(ctx, chain);

        assertNotNull(result);
        assertNotEquals("", result);
        // It is a JSON array of 3 elements
        JSONArray array = new JSONArray(result);
        int size = array.length();
        assertEquals(5, size);
        // Check it's good
    }
}
