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
package name.marmar.gf.greplog.logparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Parse log file and provide iterator over recognized {@link name.marmar.gf.greplog.logparser.LogRecord} (log records).
 *
 * @author martin.mares(at)oracle.com
 */
public class LogStreamIterator implements Iterator<LogRecord> {

    private final BufferedReader reader;

    private LogRecord nextLog;
    private boolean finished = false;

    public LogStreamIterator(InputStream is) {
        reader = new BufferedReader(new InputStreamReader(is));
    }

    private LogRecord readLogFromSource() {
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            boolean oraLogFormat = false;
            while ((line = reader.readLine()) != null) {
                String trimLine = line.trim();
                if (trimLine.isEmpty() && sb.length() == 0) {
                    continue;
                } else {
                    if (sb.length() == 0 && line.startsWith("[")) {
                        oraLogFormat = true;
                    }
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(line);
                    if (oraLogFormat) {
                        if (trimLine.endsWith("]]")) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            String msg = sb.toString().trim();
            if (msg.length() == 0) {
                return null;
            } else {
                return new LogRecord(msg);
            }
        } catch (IOException exc) {
            throw new IllegalStateException("Can not read from log file (stream).", exc);
        }
    }

    @Override
    public boolean hasNext() {
        if (nextLog == null && !finished) {
            nextLog = readLogFromSource();
            if (nextLog == null) {
                finished = true;
                try {
                    reader.close();
                } catch (Exception exc) {
                }
            }
        }
        return !finished;
    }

    @Override
    public LogRecord next() {
        if (hasNext()) {
            LogRecord result = this.nextLog;
            this.nextLog = null;
            return result;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
