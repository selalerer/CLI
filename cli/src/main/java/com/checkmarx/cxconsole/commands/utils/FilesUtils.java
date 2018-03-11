package com.checkmarx.cxconsole.commands.utils;

import com.checkmarx.components.zipper.ZipListener;
import com.checkmarx.components.zipper.Zipper;
import com.checkmarx.cxconsole.commands.job.exceptions.CLIJobException;
import com.checkmarx.cxconsole.parameters.CLISASTParameters;
import com.checkmarx.cxconsole.utils.ConfigMgr;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Created by nirli on 01/03/2018.
 */
public class FilesUtils {

    private FilesUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static Logger log = Logger.getLogger(FilesUtils.class);

    private static int numOfZippedFiles;
    private static String[] excludeFilesPatterns;
    private static String[] excludeFoldersPatterns;

    public static void zipFolder(String location, CLISASTParameters cliSastParameters, long maxZipSize, ByteArrayOutputStream byteArrayOutputStream) {
        numOfZippedFiles = 0;
        if (!isProjectDirectoryValid(location)) {
            return;
        }
        try {
            log.info("Zipping files from: " + location + " Please wait");
            excludeFilesPatterns = createExclusionPatternsArray(ConfigMgr.KEY_EXCLUDED_FILES, cliSastParameters);
            excludeFoldersPatterns = createExclusionPatternsArray(ConfigMgr.KEY_EXCLUDED_FOLDERS, cliSastParameters);
            String[] includeAllPatterns = new String[]{"**/*"};//the default is to include all files
            ZipListener zipListener = new ZipListener() {

                @Override
                public void updateProgress(String fileName, long size) {
                    numOfZippedFiles++;
                    log.trace("Zipping (" + FileUtils.byteCountToDisplaySize(size) + "): " + fileName);
                }
            };
            Zipper zipper = new Zipper();
            zipper.zip(new File(location), ArrayUtils.addAll(excludeFilesPatterns, excludeFoldersPatterns), includeAllPatterns, byteArrayOutputStream, maxZipSize, zipListener);
            log.info("Zipping complete with " + numOfZippedFiles + " files.");
        } catch (Exception e) {
            log.trace(e);
            log.error("Error occurred during zipping source files. Error message: " + e.getMessage());
        }
    }

    public static void validateZippedSources(long maxZipSize, ByteArrayOutputStream byteArrayOutputStream) throws CLIJobException {
        // check packed sources size
        if (byteArrayOutputStream == null || byteArrayOutputStream.size() == 0) {
            // if size is greater that restricted value, stop scan
            log.error("Packing sources has failed: empty packed source ");
            throw new CLIJobException("Packing sources has failed: empty packed source ");
        }

        if (byteArrayOutputStream.size() > maxZipSize) {
            // if size greater that restricted value, stop scan
            log.error("Packed project size is greater than " + maxZipSize);
            throw new CLIJobException("Packed project size is greater than " + maxZipSize);
        }
    }

    private static boolean isProjectDirectoryValid(String location) {
        File projectDir = new File(location);
        if (!projectDir.exists()) {
            log.error("Project directory [" + location + "] does not exist.");
            return false;
        }

        if (!projectDir.isDirectory()) {
            log.error("Project path [" + location + "] should point to a directory.");
            return false;
        }
        return true;
    }

    private static String[] createExclusionPatternsArray(String defaultKey, CLISASTParameters cliSastParameters) {
        LinkedList<String> excludePatterns = new LinkedList<>();
        try {
            String defaultExcludedFiles = ConfigMgr.getCfgMgr().getProperty(defaultKey);
            for (String file : StringUtils.split(defaultExcludedFiles, ",")) {
                String trimmedPattern = file.trim();
                if (!Objects.equals(trimmedPattern, "")) {
                    excludePatterns.add("**/" + trimmedPattern.replace('\\', '/'));
                }
            }

            if (cliSastParameters.isHasExcludedFilesParam()) {
                for (String file : cliSastParameters.getExcludedFiles()) {
                    String trimmedPattern = file.trim();
                    if (!Objects.equals(trimmedPattern, "")) {
                        excludePatterns.add("**/" + trimmedPattern.replace('\\', '/'));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occurred creation of exclude patterns");
        }

        return excludePatterns.toArray(new String[]{});
    }

    public static void createReportFile(HttpResponse response, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
            InputStream is = response.getEntity().getContent();
            int read;
            byte[] buffer = new byte[32768];

            while ((read = is.read(buffer)) > 0) {
                fos.write(buffer, 0, read);
            }

            fos.close();
            is.close();
        } catch (IOException e) {
            log.error("Failed to create report file: " + filePath + " : " + e.getMessage());
        }
    }

    public static String[] getExcludeFilesPatterns() {
        return excludeFilesPatterns;
    }

    public static String[] getExcludeFoldersPatterns() {
        return excludeFoldersPatterns;
    }
}
