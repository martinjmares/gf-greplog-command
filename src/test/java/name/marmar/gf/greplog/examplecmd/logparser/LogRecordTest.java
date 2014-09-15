/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package name.marmar.gf.greplog.examplecmd.logparser;

import junit.framework.TestCase;
import name.marmar.gf.greplog.logparser.LogRecord;
import org.junit.Test;

/**
 *
 * @author martin.mares(at)oracle.com
 */
public class LogRecordTest extends TestCase {

    @Test
    public void testParse() throws Exception {
        LogRecord rec = new LogRecord("[2014-09-10T15:29:31.182+0200] [glassfish 4.1] [INFO] [NCLS-LOGGING-00009] [javax.enterprise.logging] [tid: _ThreadID=15 _ThreadName=RunLevelControllerThread-1410355771103] [timeMillis: 1410355771182] [levelValue: 800] [[\n" +
                "  Running GlassFish Version: GlassFish Server Open Source Edition  4.1]]");
        assertTrue(rec.isParsed());
        assertEquals("glassfish 4.1", rec.getProductName());
        assertEquals("NCLS-LOGGING-00009", rec.getMsgId());
        assertEquals("javax.enterprise.logging", rec.getPckg());
        assertEquals(800, rec.getLevel());
        assertEquals(1410355771182L, rec.getTime());
        assertEquals("Running GlassFish Version: GlassFish Server Open Source Edition  4.1", rec.getMessage());
    }

    @Test
    public void testParseCommonMessage() throws Exception {
        LogRecord rec = new LogRecord("Some non standard message");
        assertFalse(rec.isParsed());
    }

    @Test
    public void testParseTime() throws Exception {
        LogRecord rec = new LogRecord("[2014-09-10T15:29:31.182+0200] [glassfish 4.1] [INFO] [NCLS-LOGGING-00009] [javax.enterprise.logging] [tid: _ThreadID=15 _ThreadName=RunLevelControllerThread-1410355771103] [timeMillis: ] [levelValue: 800] [[\n" +
                "  Running GlassFish Version: GlassFish Server Open Source Edition  4.1]]");
        assertTrue(rec.isParsed());
        assertEquals(1410355771182L, rec.getTime());
    }

    @Test
    public void testParseLevel() throws Exception {
        LogRecord rec = new LogRecord("[2014-09-10T15:29:31.182+0200] [glassfish 4.1] [INFO] [NCLS-LOGGING-00009] [javax.enterprise.logging] [tid: _ThreadID=15 _ThreadName=RunLevelControllerThread-1410355771103] [timeMillis: ] [levelValue: ] [[\n" +
                "  Running GlassFish Version: GlassFish Server Open Source Edition  4.1]]");
        assertTrue(rec.isParsed());
        assertEquals(800, rec.getLevel());
    }
}