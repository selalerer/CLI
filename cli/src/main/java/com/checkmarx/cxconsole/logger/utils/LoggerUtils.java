package com.checkmarx.cxconsole.logger.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by nirli on 25/02/2018.
 */
public class LoggerUtils {

    public static String getLogFileLocation(String logPath, String projectNameFromParam) {
        String logFileLocation = logPath;
        String projectName = projectNameFromParam;
        String[] parts = new String[0];
        if (projectName != null) {
            projectName = projectName.replaceAll("/", "\\\\");
            parts = projectName.split("\\\\");
        }
        String usrDir = System.getProperty("user.dir") + File.separator + normalizeLogPath(parts[parts.length - 1]) + File.separator;

        if (logFileLocation == null) {
            logFileLocation = usrDir + normalizeLogPath(parts[parts.length - 1]) + ".log";
        } else {
            String origPath = logFileLocation;
            try {
                logFileLocation = Paths.get(logFileLocation).toFile().getCanonicalPath();
            } catch (IOException e) {
                logFileLocation = origPath;
            }

            File logpath = new File(logFileLocation);
            if (logpath.isAbsolute()) {
                // Path is absolute
                if (logFileLocation.endsWith(File.separator)) {
                    // Directory path
                    logFileLocation = logFileLocation + parts[parts.length - 1] + ".log";
                } else {
                    // File path
                    if (logFileLocation.contains(File.separator)) {
                        String dirPath = logFileLocation.substring(0, logFileLocation.lastIndexOf(File.separator));
                        File logDirs = new File(dirPath);
                        if (!logDirs.exists()) {
                            logDirs.mkdirs();
                        }
                    }
                }
            } else {
                // Path is not absolute
                if (logFileLocation.endsWith(File.separator)) {
                    // Directory path
                    logFileLocation = usrDir + logFileLocation + parts[parts.length - 1] + ".log";
                } else {
                    // File path
                    if (logFileLocation.contains(File.separator)) {
                        String dirPath = logFileLocation.substring(0, logFileLocation.lastIndexOf(File.separator));
                        File logDirs = new File(usrDir + dirPath);
                        if (!logDirs.exists()) {
                            logDirs.mkdirs();
                        }
                    }

                    logFileLocation = usrDir + logFileLocation;
                }
            }
        }

        return logFileLocation;
    }

    private static String normalizeLogPath(String projectName) {
        if (projectName == null || projectName.isEmpty()) {
            return "cx_console.log";
        }

        String normalPathName = "";
        normalPathName = projectName.replace("\\", "_");
        normalPathName = normalPathName.replace("/", "_");
        normalPathName = normalPathName.replace(":", "_");
        normalPathName = normalPathName.replace("?", "_");
        normalPathName = normalPathName.replace("*", "_");
        normalPathName = normalPathName.replace("\"", "_");
        normalPathName = normalPathName.replace("<", "_");
        normalPathName = normalPathName.replace(">", "_");
        normalPathName = normalPathName.replace("|", "_");
        return normalPathName;
    }
}