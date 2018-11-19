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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by nirli on 01/03/2018.
 */
public class FilesUtils {

    private FilesUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static Logger log = Logger.getLogger(FilesUtils.class);

    private static int numOfZippedFiles;

    public static void zipFolder(String location, CLISASTParameters cliSastParameters, long maxZipSize, ByteArrayOutputStream byteArrayOutputStream) {
        numOfZippedFiles = 0;
        if (!isProjectDirectoryValid(location)) {
            return;
        }
        try {
            log.info("Zipping files from: " + location + " Please wait");
            String[] excludeFilesPatterns = createExclusionPatternsArray(ConfigMgr.KEY_EXCLUDED_FILES, cliSastParameters);
            String[] excludeFoldersPatterns = createExclusionPatternsArray(ConfigMgr.KEY_EXCLUDED_FOLDERS, cliSastParameters);
            String[] includeAllPatterns = new String[]{"**/*"};//the default is to include all files
            ZipListener zipListener = (fileName, size) -> {
                numOfZippedFiles++;
                log.trace("Zipping (" + FileUtils.byteCountToDisplaySize(size) + "): " + fileName);
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

    public static void createReportFile(HttpResponse response, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            InputStream is = response.getEntity().getContent();
            int read;
            byte[] buffer = new byte[32768];

            while ((read = is.read(buffer)) > 0) {
                fos.write(buffer, 0, read);
            }

            fos.close();
            is.close();
        } catch (IOException e) {
            log.error("Failed to create report file: " + file + " : " + e.getMessage());
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

    public static String[] createExclusionPatternsArray(String propertyKey, CLISASTParameters cliSastParameters) {
        List<String> configFileExclusionsList = new ArrayList<>();
        List<String> commandLineExclusionsList = new ArrayList<>();
        String formattedString = "%s";

        try {
            String[] excludesParams;
            switch (propertyKey) {
                case ConfigMgr.KEY_EXCLUDED_FILES:
                    excludesParams = cliSastParameters.getExcludedFiles();
                    break;
                case ConfigMgr.KEY_EXCLUDED_FOLDERS:
                    excludesParams = cliSastParameters.getExcludedFolders();
                    formattedString = "**/%s/**/*";
                    break;
                default:
                    log.error(String.format("default key %s is invalid", propertyKey));
                    excludesParams = new String[0];
                    break;
            }

            String configFileExclusions = ConfigMgr.getCfgMgr().getProperty(propertyKey);
            String finalFormattedString = formattedString;
            configFileExclusionsList = Arrays.stream(StringUtils.split(configFileExclusions, ",")).map(param -> String.format(finalFormattedString, param.trim())).collect(Collectors.toList());

            commandLineExclusionsList = Arrays.stream(excludesParams).map(param -> String.format(finalFormattedString, param.trim())).collect(Collectors.toList());
        } catch (Exception e) {
            log.error(String.format("Error: %s", e));
        }

        return mergeListsWithNoDuplicates(configFileExclusionsList, commandLineExclusionsList).toArray(new String[]{});
    }

    private static List<String> mergeListsWithNoDuplicates(List<String> listA, List<String> listB) {
        Set<String> set = new HashSet<>(listA);
        set.addAll(listB);
        List<String> mergedList = new ArrayList<>();
        mergedList.addAll(set);
        return mergedList;
    }
}
