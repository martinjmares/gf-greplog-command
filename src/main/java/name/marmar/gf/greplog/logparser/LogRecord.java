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

import com.sun.enterprise.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bean representing one logged message.
 *
 * @author martin.mares(at)oracle.com
 */
//TODO: Support other log formats
public class LogRecord {

    private static final Pattern oraLogLinePattern =
            Pattern.compile("\\[(.+)\\] \\[(.*)\\] \\[(.*)\\] \\[(.*)\\] \\[(.*)\\] \\[tid\\: (.*)\\] \\[timeMillis: (.*)\\] \\[levelValue: (.*)\\] \\[\\[(.*)\\]\\]", Pattern.DOTALL);
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final String fullMessage;
    private final boolean parsed;
    private final String productName;
    private final String msgId;
    private final String pckg;
    private final String tid;
    private final long time;
    private final int level;
    private final String message;

    public LogRecord(String fullMessage) {
        this.fullMessage = fullMessage;
        Matcher matcher = oraLogLinePattern.matcher(fullMessage);
        if (matcher.matches()) {
            this.parsed = true;
            this.time = parseTime(matcher.group(1), matcher.group(7));
            this.productName = matcher.group(2);
            this.level = parseLevel(matcher.group(3), matcher.group(8));
            this.msgId = matcher.group(4);
            this.pckg = matcher.group(5);
            this.tid = matcher.group(6);
            this.message = matcher.group(9).trim();
        } else {
            parsed = false;
            this.time = -1L;
            this.productName = null;
            this.level = -1;
            this.msgId = null;
            this.pckg = null;
            this.tid = null;
            this.message = null;
        }
    }

    private static final long parseTime(String time, String millis) {
        try {
            if (StringUtils.ok(millis)) {
                return Long.parseLong(millis);
            }
            return dateFormat.parse(time).getTime();
        } catch (Exception exc) {
            return -1L;
        }
    }

    public static final int parseLevel(String levelName, String level) {
        try {
            if (StringUtils.ok(level)) {
                return Integer.parseInt(level);
            }
            return Level.parse(levelName).intValue();
        } catch (Exception exc) {
            return -1;
        }
    }

    /** Unchanged - unparsed log message.
     */
    public String getFullMessage() {
        return fullMessage;
    }

    public boolean isParsed() {
        return parsed;
    }

    public String getProductName() {
        return productName;
    }

    public String getMsgId() {
        return msgId;
    }

    /** Recognized logger name. Usually represents fully qualified name of the source class.
     */
    public String getPckg() {
        return pckg;
    }

    /** Thread ID and name.
     */
    public String getTid() {
        return tid;
    }

    public long getTime() {
        return time;
    }

    public int getLevel() {
        return level;
    }

    /** Just log message without additional attrinutes.
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return fullMessage;
    }
}
