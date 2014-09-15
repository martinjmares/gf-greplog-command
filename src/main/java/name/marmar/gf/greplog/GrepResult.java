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
package name.marmar.gf.greplog;

import org.glassfish.api.ActionReport;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for {@link name.marmar.gf.greplog.GrepLogCommand}, collects log filter results.
 *
 * @author martin.mares(at)oracle.com
 */
class GrepResult {

    private static final String END_LOG_DIVIDER = "\n---------------------------------------------------\n";

    private final List<String> lines = new ArrayList<>();

    private final int limit;
    private int cursor = -1;
    private int size = 0;

    GrepResult(int limit) {
        this.limit = limit;
    }

    void add(String line) {
        cursor++;
        if (limit >= 0 && cursor >= limit) {
            cursor = 0;
        }
        if (cursor >= lines.size()) {
            lines.add(line);
        } else {
            lines.set(cursor, line);
        }
        size++;
    }

    /** Write all collected results to the {@code ActionReport}.
     *
     * @param report where results will be written.
     */
    void addToReport(ActionReport report) {
        ActionReport.MessagePart msg = report.getTopMessagePart();
        msg.appendMessage(" [find " + size + " record(s)");
        if (size > limit) {
            msg.appendMessage(". Print last " + limit + " records]:\n");
        } else {
            msg.appendMessage("]:\n");
        }
        if (cursor < 0) {
            return;
        }
        int readCursor = cursor + 1;
        while (readCursor < lines.size()) {
            msg.appendMessage(formatRecord(lines.get(readCursor)));
            readCursor++;
        }
        readCursor = 0;
        while (readCursor <= cursor) {
            msg.appendMessage(formatRecord(lines.get(readCursor)));
            readCursor++;
        }
        msg.appendMessage(END_LOG_DIVIDER);
    }

    private final String formatRecord(String record) {
        if (record == null) {
            return "";
        }
        return "\n" + record + "\n";
    }

    /** Count of all collected results.
     */
    int getSize() {
        return size;
    }
}
