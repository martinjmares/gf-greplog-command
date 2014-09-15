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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.enterprise.util.StringUtils;
import name.marmar.gf.greplog.logparser.*;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Implementation of <b>grep-log</b> admin command. It filters all identified log files from provided {@code --target}
 * base on provided characteristics.
 *
 * @author martin.mares(at)oracle.com
 */
@ExecuteOn(value={RuntimeType.INSTANCE})
@TargetType(value={CommandTarget.DOMAIN, CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@Service(name = "grep-log")
@CommandLock(CommandLock.LockType.NONE)
@PerLookup
public class GrepLogCommand implements AdminCommand {

    public static final Logger logger = Logger.getLogger(GrepLogCommand.class.getName());

    /** Standard command target
     */
    @Param(optional=true)
    public String target;

    /** If {@code true} then <i>search</i> argument will be considered as substring and not as regexp.
     */
    @Param(optional = true, name = "fixed-strings", shortName = "F", defaultValue = "false")
    private boolean fixedStrings;

    /** Maximum number of returned log messages per one log file.
     * If there is more then {@code limit} filtered messages then last {@code limit} number will be returned.
     */
    @Param(optional = true, defaultValue = "100")
    private int limit;

    /** Minimal logging level.
     * It can be number or {@code Logger Level} name.
     */
    @Param(optional = true, name = "min-level", defaultValue = "")
    private String minLevel;

    /** Filter of the Logger. It is usually fully qualified class name.
     * If defined then only log messages which Logger name starts with this value will be returned.
     */
    @Param(optional = true, name = "package", defaultValue = "")
    private String pckg;

    /** Regular expression to filter logged message.
     * If {@code fixed-strings} is {@code true} then this is not regexp but just substring.
     */
    @Param(optional = true, primary = true)
    private String search;

    @Inject
    LoggingConfigImpl loggingConfig;

    @Inject
    ServerEnvironment env;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        //Create predicate from parameters
        Collection<Predicate<LogRecord>> predicates = new ArrayList<>(3);
        if (StringUtils.ok(search)) {
            if (fixedStrings) {
                predicates.add(new LogMessagePredicate(search));
            } else {
                predicates.add(new LogRegexpPredicate(Pattern.compile(".*" + search + ".*")));
            }
        }
        if (StringUtils.ok(minLevel)) {
            predicates.add(new LogMinLevelPredicate(minLevel));
        }
        if (StringUtils.ok(pckg)) {
            predicates.add(new LogPackagePredicate(pckg));
        }
        Predicate<LogRecord> predicate = Predicates.and(predicates);
        //Find logging directory
        String sourceDirName;
        try {
            sourceDirName = loggingConfig.getLoggingFileDetails();
            sourceDirName = sourceDirName.replace("${com.sun.aas.instanceRoot}", env.getInstanceRoot().getPath());
            sourceDirName = sourceDirName.substring(0, sourceDirName.lastIndexOf(File.separator));
        } catch (Exception exc) {
            //Configuration for the server probably does not exists. Try to locate by convention
            File logDir = new File(env.getInstanceRoot(), "logs");
            //logger.log(Level.INFO, "Hard generated logging directory: " + logDir.getPath());
            if (logDir.exists() && logDir.isDirectory()) {
                sourceDirName = logDir.getPath();
            } else {
                logger.log(Level.WARNING, "Generated log dir name [" + logDir.getPath() + "] does not exists");
                report.setMessage("Can not locate log files directory");
                report.setFailureCause(exc);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }
        //Find files
        File sourceDir = new File(sourceDirName);
        if (!sourceDir.exists()) {
            report.setMessage("Logging directory does not exists [" + sourceDirName + "]");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        File[] logFiles = sourceDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isFile();
            }
        });
        //Grep all valid logfiles
        report.setMessage("** On server (" + env.getInstanceName() + ") greps " + logFiles.length + " logging file(s)");
        for (File logFile : logFiles) {
            report.appendMessage("\n**** " + logFile.getName());
            try {
                GrepResult grep = grep(logFile, predicate);
                grep.addToReport(report);
            } catch (Exception e) {
                report.appendMessage(" [Can NOT process file!]");
                logger.log(Level.WARNING, "Can not process file " + logFile.getPath() + ".", e);
            }
        }
        //Result
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private GrepResult grep(File f, Predicate<LogRecord> predicate) throws IOException {
        GrepResult result = new GrepResult(limit);
        InputStream is = null;
        try {
            is = new FileInputStream(f);
            UnmodifiableIterator<LogRecord> iter = Iterators.filter(new LogStreamIterator(is), predicate);
            while (iter.hasNext()) {
                result.add(iter.next().getFullMessage());
            }
        } finally {
            try {
                is.close();
            } catch (Exception exc) {
            }
        }
        return result;
    }

}
