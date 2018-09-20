# Checkmarx CLI plugin

The CxSAST scan enables you to run a static application security test (CxSAST) and an open source analysis (CxOSA) scan as a CLI command. The CxSAST scan is, by default, run in synchronous mode (Scan). This means that the CLI initiates the scan task and the scan results can be viewed in the CLI and in the log file created. In asynchronous mode (AsyncScan), the scan task ends when the scan request reaches the scan queue, as a result the scan results can only be viewed via the CxSAST web application.


## Prerequisites
- Checkmarx CxSAST/CxOSA installed
- [Checkmarx CLI Plugin](https://www.checkmarx.com/plugins/) 

## Made with help of
- [react-native](https://github.com/facebook/react-native)
- [react-native-ui-kitten](https://github.com/akveo/react-native-ui-kitten)
- [realm](https://github.com/realm/realm-js)
- [react-navigation](https://github.com/react-community/react-navigation)
- [victory-native](https://github.com/FormidableLabs/victory-native) charts
- [Google Analytics](https://github.com/idehub/react-native-google-analytics-bridge)

## Syntax

- Synchronous Mode:
    runCxConsole Scan -v -CxServer <host> -projectName <projectName> -CxUser <username> -CxPassword <password> -Locationtype <Locationtype> -locationpath <locationpath> -Preset <preset> -EnableOsa -OsaLocationPath <filename>

- Asynchronous Mode:
    runCxConsole AsyncScan -v -CxServer <host> -projectName <projectName> -CxUser <username> -CxPassword <password> -Locationtype <Locationtype> -locationpath <locationpath> -Preset <preset> -EnableOsa -OsaLocationPath <filename>
       
## Can I hire you guys?
Yes!  Visit [our homepage](http://akveo.com/) or simply leave us a message to [contact@akveo.com](mailto:contact@akveo.com). We will be happy to work with you!

## License
[Apache 2.0](LICENSE.txt) license.

## Parameters

| Key           | Mandatory     | Description  |
| ------------- |:-------------:| ------------:|
| -CxServer <server>|Mandatory|IP address or resolvable name of CxSAST web server.|
|-useSSO|Optional|Single Sign-On: Use Windows credentials of current user to log into CxSAST.|
|-CxUser <username>|Mandatory unless -useSSO is used|CxSAST login credentials (username and password)|
|-CxPassword <password> |Mandatory unless -useSSO is used|CxSAST login credentials (username and password)|
|-enableOsa |Optional |Enable open source analysis (CxOSA). -osaLocationPath should be specified or the -LocationType parameter needs to be defined as 'folder' or 'shared' (if -osaLocationPath doesn't exist, use -locationPath). |
|-OsaLocationPath <filename> |Optional |Local or network path to sources or source repository branch. May include multiple list of folders (local or shared) separated by comma. |
|-ProjectName <project name> |Mandatory |An existing or new project name with full path. If the project doesn't exist, it will be created |                                       
|-LocationType <type> |Mandatory|Source location type. One of: folder, shared, SVN, TFS, Perforce, Git|
|-WorkspaceMode <path>  |Optional |When -LocationType parameter is set to Perforce, add this parameter and add the workspace name into -locationPath |
|-LocationPath <path> |Mandatory if -LocationType is folder, SVN, TFS, Perforce or shared |Local or network path to sources or source repository branch. |
|-LocationURL <url> |Mandatory if -Locationtype is any source control system |Source control URL |
|-LocationPort <url> |Optional |Source control system port. Default: 8080 (TFS), 80 (SVN), or 1666 (Perforce). |
|-LocationBranch <branch> |Mandatory if -LocationType is GIT |Source GIT branch. |
|-LocationUser <username> |Mandatory if -Locationtype is TFS/Perforce/shared |Source control / network credentials. |
|-LocationPrivateKey <path\file> |Mandatory if -Locationtype is GIT using SSH |GIT SSH key locations. |
|-Preset <preset> |Optional |	
If not provided, will use preset defined in existing project or, for a new project, the default preset. |
|-ForceScan |Optional |Force scan on source code, which has not been changed since the last scan of the same project (not compatible with -Incremental option). |
|-Incremental  |Optional |Run incremental scan instead of a full scan. Scans only new and modified files, relative to project's last scan(-Incremental will disable any -ForceScan setting). |
|–LocationPathExclude <folders list> |Optional |Comma separated list of folder name patterns to exclude from scan. For example, exclude all test and log folders: -locationPathExclude test* log_* |
|–LocationFilesExclude <files list> |Optional |Comma separated list of file name patterns to exclude from scan. For example, exclude all files with '.class' extension: -LocationFilesExclude *.class  |
|-SASTHigh  |Optional. Not supported in AsyncScan mode |CxSAST high severity vulnerability threshold. If the number of high vulnerabilities exceeds the threshold, scan will end with an error (see Error/Exit Codes). |
|-SASTMedium  |Optional. Not supported in AsyncScan mode |CxSAST medium severity vulnerability threshold. If the number of medium vulnerabilities exceeds the threshold, scan will end with an error (see Error/Exit Codes). |
|-SASTLow |Optional. Not supported in AsyncScan mode |CxSAST low severity vulnerability threshold. If the number of low vulnerabilities exceeds the threshold, scan will end with an error (see Error/Exit Codes). |
|-Configuration <configuration> |Optional |Code language configuration |
|-Private |Optional |Scan will not be visible to other users. |
|-Log <path\file> |Optional |Log file to be created. |
|-OsaArchiveToExtract <files list> |Optional |Comma separated list of file extensions to be extracted in the OSA scan. 
                                              For example: -OsaArchiveToExtract *.zip extracts only files with .zip extension. |
|-OsaFilesInclude <files list> |Optional |Comma separated list of file name patterns to exclude from the OSA scan. 
                                          For example: *.dll will include only dll files. |
|-OsaFilesExclude <files list> |Optional |Comma separated list of file name patterns to exclude from the OSA scan. Exclude extensions by using *.<extension>, or exclude files by using */<file>.|
|-OsaPathExclude <folders list> |Optional |Comma separated list of folder path patterns to exclude from the OSA scan. 
                                           For example: -OsaPathExclude test excludes all folders which start with test prefix. |
|-OsaScanDepth <OSA analysis unzip depth> |Optional |Extraction depth of files to include in the OSA scan. |
|-executepackagedependency |Optional |Retrieve all NPM package dependencies before performing OSA scan (see Remarks section). |
|-OSAHigh |Optional. Not supported in AsyncScan mode |CxOSA high severity vulnerability threshold. If the number of high vulnerabilities exceeds the threshold, scan will end with an error (see Error/Exit Codes). |
|-OSAMedium  |Optional. Not supported in AsyncScan mode |CxOSA medium severity vulnerability threshold. If the number of medium vulnerabilities exceeds the threshold, scan will end with an error (see Error/Exit Codes).  |
|-OSALow |Optional. Not supported in AsyncScan mode |CxOSA low severity vulnerability threshold. If the number of low vulnerabilities exceeds the threshold, scan will end with an error (see Error/Exit Codes). |
|-OsaReportHtml <path\file> |OsaReportHtml has been deprecated and is no longer supported (see Remarks section). |Generate CxOSA HTML report. |
|-OsaReportPDF <path\file> |OsaReportPDF has been deprecated and is no longer supported (see Remarks section). |Generate CxOSA PDF report. |
|-OsaJson <path\file> |Optional. Not supported in AsyncScan mode |Generate CxOSA JSON report. |
|-ReportXML <file> |Optional. Not supported in AsyncScan mode |Name or path to results report, by type. |
|-ReportPDF <file> |Optional. Not supported in AsyncScan mode |Name or path to results report, by type. |
|-ReportCSV <file> |Optional. Not supported in AsyncScan mode |Name or path to results report, by type. |
|-ReportRTF <file> |Optional. Not supported in AsyncScan mode |Name or path to results report, by type. |
|-Comment <text> |Optional. Not supported in AsyncScan mode |Saves a comment with the scan results. For example: -comment 'important scan1' |
|-verbose or -v |Optional | Turns on verbose mode. All messages and events will be sent to the console or log file. |


## Error/Exit Codes

The table below describes CLI Exit/Error codes when a task is executed. The description of codes may help in identifying and troubleshooting issues.

| Code           | Description     |
| ------------- |:-------------:|
|0 | Completed successfully|
|1 |Failed to start scan (general error) |
|2 |Invalid license for SDLC |
|3 |Invalid license for OSA |
|4 |Login failed |
|5 |OSA scan requires an existing project on the server |
|10 |Failed on threshold SAST HIGH |
|11 |Failed on threshold SAST Medium |
|12 |Failed on threshold SAST LOW |
|13 |Failed on threshold OSA HIGH |
|14 |Failed on threshold OSA Medium |
|15 |Failed on threshold OSA Low |
|19 |Generic threshold failure if both SAST and OSA fail |
|130 |Canceled by user (Ctrl-C) |
