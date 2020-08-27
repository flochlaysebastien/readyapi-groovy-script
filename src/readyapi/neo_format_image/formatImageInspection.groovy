package readyapi.neo_format_image

//import org.apache.logging.log4j.core.Logger
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.common.base.Strings
import groovy.json.*
import org.apache.tools.tar.TarEntry
import org.apache.tools.tar.TarInputStream
import org.w3c.dom.Document
import org.w3c.dom.NodeList

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import static java.nio.charset.StandardCharsets.UTF_8

try {
    LogUtils.log = log
    LogUtils.setProcess("1", "Prepare working environment", "3")
    LogUtils.setStep("1", "get testCase properties")
    String catalogID = context.expand( '${Properties#returnedCatalogItemID}' )
    String formatDownloadURL = context.expand( '${Properties#returnedDownloadLink}' )
    String userRole = context.expand('${#TestCase#userRole}')
    String userName = context.expand('${#Project#username.' + userRole + '}')
    String apikey = context.expand('${#Global#apikey.' + userName + '}')
    String gcpserviceaccount = context.expand('${gcp.serviceaccount}')
    String referenceFilePath = context.expand( '${#TestCase#referenceFilePath}' )

    if (Strings.isNullOrEmpty(catalogID) || Strings.isNullOrEmpty(formatDownloadURL) || Strings.isNullOrEmpty(referenceFilePath) || Strings.isNullOrEmpty(apikey) || Strings.isNullOrEmpty(gcpserviceaccount)) {
        throw new MissingPropertyException("Failed to get property value. catalogID='$catalogID' | formatDownloadURL='$formatDownloadURL' | referenceFilePath='$referenceFilePath' | apikey='${apikey?.take(10)}...'  | gcpserviceaccount='${gcpserviceaccount?.take(10)}...'")
    }
    if (!referenceFilePath.endsWith(".zip")) {
        throw new Exception("The reference file must be contained in zip archive format. ['$referenceFilePath']")
    }
    LogUtils.print()

    DownloadUtils formatDownloaderUtils = new DownloadUtils(catalogID, formatDownloadURL, referenceFilePath)
    formatDownloaderUtils.createWorkingDirectories()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("workingDirectory", formatDownloaderUtils.workingDir.getParent())

    LogUtils.setProcess("2", "Download and extract format archived file", "3")
    formatDownloaderUtils.downloadAndExtractFormatArchivedResult(apikey)

    LogUtils.setProcess("3", "Download reference files from google storage", "7")
    formatDownloaderUtils.downloadAndExtractReferenceFiles(gcpserviceaccount)

    LogUtils.setProcess("4", "Prepare reference files for comparison", "5")
    FormatDataUtils referenceFormatDataUtils = new FormatDataUtils(formatDownloaderUtils.referenceDir.path)
    referenceFormatDataUtils.scanWorkingDirectory(AppConfig.rootVolumeFileName)
    referenceFormatDataUtils.retrieveDataFromRootVolumeFile()
    referenceFormatDataUtils.prepareDataForComparison()

    LogUtils.setProcess("5", "Prepare result files for comparison", "5")
    FormatDataUtils resultFormatDataUtils = new FormatDataUtils(formatDownloaderUtils.resultDir.path)
    resultFormatDataUtils.scanWorkingDirectory(AppConfig.rootVolumeFileName)
    resultFormatDataUtils.retrieveDataFromRootVolumeFile()
    resultFormatDataUtils.prepareDataForComparison()

    LogUtils.setProcess("6", "Control format archived result", "2")
    CompareDataUtils.compareDataStructure(referenceFormatDataUtils.formattedDirectoryFileList, resultFormatDataUtils.formattedDirectoryFileList)
    CompareDataUtils.searchForEmptyElement(formatDownloaderUtils.resultDir)

    LogUtils.done()
} catch (Exception ex) {
    LogUtils.fail(ex.getMessage())
    testRunner.fail("Format file inspection failed")
}

class LogUtils {
    public static String processDescription
    public static String processID
    public static String processStepsNumber
    public static String stepDescription
    public static String stepID
//    public static Logger log

    public static void setProcess(String id, String description, String stepsNumber) {
        processDescription = description
        processID = id
        processStepsNumber = stepsNumber
        this.log.info("        $processID - $processDescription:")
    }

    public static void setStep(String id, String description) {
        stepDescription = description
        stepID = id
    }

    public static void print(Boolean succeed = true) {
        String result = succeed ? 'succeed' : 'failed'
        this.log.info("            [$stepID/$processStepsNumber] $stepDescription ($result)")
    }

    public static void fail(String message) {
        LogUtils.print(false)
        this.log.error("$message")
    }

    public static void done() {
        this.log.info("            ==========")
        this.log.info("            [format] Done")
    }
}

class AppConfig {
    public static String ingestionType
    public static String rootVolumeFileName
    public static Integer minimumLineNumber
    public static List<String> extensionsToControlList = []
    public static List<String> excludingFilesList = []

    public static void setConfigValues(Object json) {
        if (!json) {
            throw new Exception("The configuration doesn't contains expected values")
        }

        ingestionType = json.'ingestion'.'type'.toString().toLowerCase()
        rootVolumeFileName = json.'ingestion'.'root-volume-file'.toString().toLowerCase()
        minimumLineNumber = json.'inspection'.'line-number-control'.'minimum-line-number'.toInteger()
        json.'inspection'.'line-number-control'.'extensions-to-control'.each { extensionsToControlList.add(it.name.toString().toLowerCase()) }
        json.'inspection'.'excluding-files'.each { excludingFilesList.add(it.name.toString().toLowerCase()) }
    }
}

class DownloadUtils {
    public File workingDir
    public File resultDir
    public File referenceDir
    public File configurationFile
    public final String catalogID
    public final String formatDownloadURL
    public final String referenceFilePath
    private final String WORKING_DIRECTORY_BASE = "/tmp/readyapi/format"
    // https://cloud.google.com/storage/docs/authentication
    private final String GOOGLE_STORAGE_SCOPE = "https://www.googleapis.com/auth/devstorage.read_only"

    DownloadUtils(catalogID, formatDownloadURL, referenceFilePath) {
        LogUtils.setStep("2", "create download utils object")
        this.catalogID = catalogID
        this.formatDownloadURL = formatDownloadURL
        this.referenceFilePath = referenceFilePath
        LogUtils.print()
    }

    void createWorkingDirectories() throws IOException {
        LogUtils.setStep("3", "create working directories")
        Date now = new Date()
        workingDir = new File("$WORKING_DIRECTORY_BASE/$catalogID/${now.format("yyyy-MM-dd-HH:mm:ss")}")

        resultDir = new File("${workingDir.toString()}/result")
        mkdirsOrThrow(resultDir)

        referenceDir = new File("${workingDir.toString()}/reference")
        mkdirsOrThrow(referenceDir)
        LogUtils.print()
    }

    void downloadAndExtractFormatArchivedResult(String apikey) throws IOException {
        LogUtils.setStep("1", "forge authorization password")
        String authentication = "Basic " + ("APIKEY:" + apikey).bytes.encodeBase64().toString()
        LogUtils.print()

        LogUtils.setStep("2", "create connection to format file")
        URL url = new URL(formatDownloadURL)
        URLConnection connection = url.openConnection()
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", authentication)
        InputStream inputStream = connection.getInputStream()
        LogUtils.print()

        LogUtils.setStep("3", "extract archived files")
        switch (connection.getContentType()) {
            case "application/zip":
                unzipStream(inputStream, resultDir.toString())
                break
            case "application/tar":
                untarStream(inputStream, resultDir.toString())
                break
            default:
                throw new IOException("Failed to download an unknown content-type. [${connection.getContentType()}]")
                break
        }
        inputStream.close()
        LogUtils.print()
    }

    void downloadAndExtractReferenceFiles(String gcpserviceaccount) throws Exception {
        LogUtils.setStep("1", "get google storage credentials")
        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(gcpserviceaccount.decodeBase64()))
        LogUtils.print()

        LogUtils.setStep("2", "add scope on google storage credentials")
        credentials = credentials.createScoped(GOOGLE_STORAGE_SCOPE)
        credentials.refreshIfExpired()
        LogUtils.print()

        LogUtils.setStep("3", "create google storage authentication")
        AccessToken token = credentials.getAccessToken()
        String authentication = "Bearer " + token.getTokenValue()
        LogUtils.print()

        LogUtils.setStep("4", "download reference file from google storage")
        Path refFilePath = Paths.get("readyapi/FORMAT-GOLDEN-DATA/$referenceFilePath")
        String encodedReferenceFilePath = URLEncoder.encode(refFilePath.toString(), UTF_8.name())
        URL referenceUrl = new URL("https://storage.googleapis.com/storage/v1/b/idp-datastore-readyapi/o/$encodedReferenceFilePath?alt=media")
        URLConnection connection = referenceUrl.openConnection()
        connection.setRequestProperty("Authorization", authentication)
        InputStream inputStream = connection.getInputStream()
        LogUtils.print()

        LogUtils.setStep("5", "extract reference file")
        unzipStream(inputStream, referenceDir.toString())
        inputStream.close()
        LogUtils.print()

        LogUtils.setStep("6", "download config file from google storage")
        String confFilePath = "${refFilePath.getParent()}/config.json"
        String encodedConfFilePath = URLEncoder.encode(confFilePath, UTF_8.name())
        URL configUrl = new URL("https://storage.googleapis.com/storage/v1/b/idp-datastore-readyapi/o/$encodedConfFilePath?alt=media")
        connection = configUrl.openConnection()
        connection.setRequestProperty("Authorization", authentication)
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            inputStream = connection.getInputStream()
            configurationFile = new File(workingDir, 'config.json')
            OutputStream outStream = new FileOutputStream(configurationFile);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            inputStream.close()
            outStream.close()
        } else {
            throw new Exception("No corresponding configuration file found on google storage. [$confFilePath]")
        }
        LogUtils.print()

        LogUtils.setStep("7", "parse config file")
        def json = new JsonSlurper().parse(configurationFile)
        AppConfig.setConfigValues(json)
        LogUtils.print()
    }

    private void unzipStream(InputStream inputStream, String targetDirectory) throws IOException {
        ZipInputStream zipStream = new ZipInputStream(inputStream)
        ZipEntry entry

        while ((entry = zipStream.getNextEntry()) != null) {
            extractEntry(entry.getName(), entry.isDirectory(), zipStream, targetDirectory)
            zipStream.closeEntry()
        }

        zipStream.close()
    }

    private void untarStream(InputStream inputStream, String targetDirectory) throws IOException {
        TarInputStream tarStream = new TarInputStream(inputStream)
        TarEntry entry

        while ((entry = tarStream.getNextEntry()) != null) {
            extractEntry(entry.getName(), entry.isDirectory(), tarStream, targetDirectory)
        }

        tarStream.close()
    }

    private void extractEntry(String name, Boolean isDirectory, FilterInputStream stream, String targetDirectory) throws IOException {
        File entryFile = new File(targetDirectory, name)

        if (isDirectory) {
            mkdirsOrThrow(entryFile)
            return
        }

        mkdirsOrThrow(entryFile.getParentFile())
        FileOutputStream outputStream = new FileOutputStream(entryFile.path)
        for (int c = stream.read(); c != -1; c = stream.read()) {
            outputStream.write(c)
        }
        outputStream.close()
    }

    private void mkdirsOrThrow(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create working directory. [$dir]")
        }
    }
}

class FormatDataUtils {
    public String jobID
    public String rootVolumeFilePath
    public List<String> directoryFileList = []
    public List<String> formattedDirectoryFileList = []
    public final String workingDirectory


    FormatDataUtils(String workingDirectory) {
        LogUtils.setStep("1", "create reference format utils object")
        this.workingDirectory = workingDirectory
        LogUtils.print()
    }

    void scanWorkingDirectory(String rootVolumeFileName) throws Exception {
        LogUtils.setStep("2", "scan working directory")
        String path
        new File(workingDirectory).eachFileRecurse() {
            directoryFileList.add(it.getAbsolutePath().replaceAll(workingDirectory, ""))
            if (path == null && it.isFile() && rootVolumeFileName.equalsIgnoreCase(it.name)) {
                path = it.getAbsolutePath()
            }
        }
        LogUtils.print()

        LogUtils.setStep("3", "locate root volume file")
        if (Strings.isNullOrEmpty(path)) {
            throw new Exception("Failed to locate root volume file. [$rootVolumeFileName]")
        }
        rootVolumeFilePath = path
        LogUtils.print()
    }

    void retrieveDataFromRootVolumeFile() throws Exception {
        LogUtils.setStep("4", "retrieve information from root volume file")
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(rootVolumeFilePath));
        NodeList list = document.getElementsByTagName("JOB_ID")

        if (list.length == 0) {
            throw new Exception("Failed to retrieve information from root volume file")
        }

        jobID = list.item(0).getTextContent()
        LogUtils.print()
    }

    void prepareDataForComparison() throws Exception {
        LogUtils.setStep("5", "prepare data for comparison")
        directoryFileList.sort().each {
            formattedDirectoryFileList.add(it.replaceAll(jobID, "<<JOBID>>")
                    .replaceAll("_P_00\\d", "_P_00X")
                    .replaceAll("_MS_00\\d", "_MS_00X")
                    .replaceAll("<<JOBID>>-00\\d", "<<JOBID>>-00X"))
        }
        LogUtils.print()
    }
}

class CompareDataUtils {
    public static void compareDataStructure(List<String> referenceFileList, List<String> resultFileList) {
        LogUtils.setStep("1", "compare result's files structure with reference")
        StringBuffer errorMessage = new StringBuffer()
        if (!referenceFileList.containsAll(resultFileList)) {
            errorMessage.append(" -> Unexpected files: (list of element present in result archive but missing from reference)\n")
            resultFileList.each {
                if (!referenceFileList.contains(it)) {
                    errorMessage.append("    - $it\n")
                }
            }
        }

        if (!resultFileList.containsAll(referenceFileList)) {
            errorMessage.append(" -> Missing files: (list of element present in reference archive but missing from result)\n")
            referenceFileList.each {
                if (!resultFileList.contains(it)) {
                    errorMessage.append("    - $it\n")
                }
            }
        }

        if (errorMessage.size() != 0) {
            throw new Exception(errorMessage.toString())
        }
        LogUtils.print()
    }

    public static void searchForEmptyElement(File resultDirectory) {
        LogUtils.setStep("2", "search for empty element into result files list")

        List<String> emptyFileList = []
        List<String> notEnoughLineFileList = []

        resultDirectory.eachFileRecurse {
            if (it.isDirectory() || AppConfig.excludingFilesList.contains(it.name.toLowerCase())) {
                return
            }

            String extension = it.name.substring(it.name.lastIndexOf(".") + 1).toLowerCase()
            if (it.size() == 0) {
                emptyFileList.add(it.path.substring(resultDirectory.path.size() + 1))
                return
            }

            if (AppConfig.extensionsToControlList.contains(extension) && it.readLines().size() <= AppConfig.minimumLineNumber) {
                notEnoughLineFileList.add(it.path.substring(resultDirectory.path.size() + 1))
                return
            }
        }

        StringBuffer errorMessage = new StringBuffer()
        if (!emptyFileList.empty) {
            errorMessage.append(" -> Empty file detected:\n")
            emptyFileList.sort().each { errorMessage.append("    - $it\n") }
        }
        if (!notEnoughLineFileList.empty) {
            errorMessage.append(" -> File with not enough lines detected:\n")
            notEnoughLineFileList.sort().each { errorMessage.append("    - $it\n") }
        }

        if (errorMessage.size() != 0) {
            throw new Exception(errorMessage.toString())
        }
        LogUtils.print()
    }
}