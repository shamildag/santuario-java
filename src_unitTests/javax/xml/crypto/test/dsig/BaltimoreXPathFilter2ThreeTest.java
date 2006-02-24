/*
 * Copyright 2006 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 */
package javax.xml.crypto.test.dsig;

import java.io.File;
import java.security.Security;

import junit.framework.*;

import javax.xml.crypto.test.KeySelectors;

/**
 * This is a testcase to validate the "merlin-xpath-filter2-three" testcases
 * under data/vectors/ie/baltimore/merlin-examples directory
 *
 * @author Sean Mullan
 */
public class BaltimoreXPathFilter2ThreeTest extends TestCase {

    private SignatureValidator validator = null;

    static {
        Security.insertProviderAt
            (new org.jcp.xml.dsig.internal.dom.XMLDSigRI(), 1);
    }

    public BaltimoreXPathFilter2ThreeTest(String name) {
        super(name);
	String fs = System.getProperty("file.separator");
	String base = System.getProperty("basedir") + fs + "data"
	    + fs + "interop" + fs + "xfilter2";
	validator = new SignatureValidator(new File
	    (base, "merlin-xpath-filter2-three"));
    }
    public void test_sign_spec() throws Exception {
        String file = "sign-spec.xml";

	boolean coreValidity = validator.validate(file, 
		    new KeySelectors.KeyValueKeySelector());
	assertTrue("Signature failed core validation#1", coreValidity);

 	coreValidity = validator.validate(file, 
		    new KeySelectors.RawX509KeySelector());
	assertTrue("Signature failed core validation#2", coreValidity);
    }
    public void test_sign_xfdl() throws Exception {
        String file = "sign-xfdl.xml";

	boolean coreValidity = validator.validate(file, 
		    new KeySelectors.KeyValueKeySelector());
	assertTrue("Signature failed core validation#1", coreValidity);

 	coreValidity = validator.validate(file, 
		    new KeySelectors.RawX509KeySelector());
	assertTrue("Signature failed core validation#2", coreValidity);
    }
    public static void main(String[] args) throws Exception {
        BaltimoreXPathFilter2ThreeTest it = 
	    new BaltimoreXPathFilter2ThreeTest("");
	it.test_sign_spec();
	it.test_sign_xfdl();
    }
}