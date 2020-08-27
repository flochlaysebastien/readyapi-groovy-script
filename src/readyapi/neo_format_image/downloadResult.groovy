package readyapi.neo_format_image

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/*
String catalogID = "89b21a6d-ac35-44f3-b07b-fa2b54c3cebd"
String formatDownloadURL = "https://view-qual.idp.private.geoapi-airbusds.com/api/v1/items/89b21a6d-ac35-44f3-b07b-fa2b54c3cebd/download"
String referenceFilePath = "PHR/Primary_Bundle_Reflectance_16_JP2K.zip"
String apikey = "O2F7dUsNpfBje5sB0wIg95rNQIykAes4q6KBFZtvAh5NVBfec9Vr8RJr1EH_14RKd5HyLM6ziCFeX68xRW1twQ=="
String gcpserviceaccount = "eyAgICJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsICAgInByb2plY3RfaWQiOiAiaWRwLWRhdGFzdG9yZSIsICAgInByaXZhdGVfa2V5X2lkIjogIjg2MGY2OTdmYWRlZmQ5MzA0NDA5MzRiNjY2N2M0NzNhM2ZhNDZmMTMiLCAgICJwcml2YXRlX2tleSI6ICItLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS1cbk1JSUV2Z0lCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktnd2dnU2tBZ0VBQW9JQkFRQzNNWU52SjViVmc5Z2JcbjBzaDNLRWFtUnBVUUVEeS9QbloxUVl1MUFTYWxVZHdYS1QxZ0hpUHJCRi9JZThHUmc5Y1l2RjJtR0MvTWF1NXFcbkxCZ3p4TmdQb3NRTFhHYkNaNWYvNld5eFVXNUoraHl0cHNaRkdFcllxdTFRdG5sYktGSysxNkZWQ3lhTE9KRGdcbm1XVkVrNnBvcklrbFlENXFiOXRjS1NVME9jK0E5bHh2bnhzODZReVZyb1RXcmx0R3JJejNGVStENUUzdlVOV2pcbjI4ME5SR2JVYXYyZnA5NTlLY1h0SnFseDFINkp2OXB1OUdJeGRIcnVjeTByNW14S01oODJUVHZnNkxtT1pSZHpcbnlOZG0vKzZieW1vd1pxZ0pJTTRMMlp4ZW9uTHBOOHBxZmtLc2lDcEdXRWl0dnZlV0F1eTYwWDBIcmVlTTFLUEVcblh0OTRwOHNGQWdNQkFBRUNnZ0VBSlNSaFVSNldPdGtkN2xvNHNjd1ZNdTQ1bDhXckExMWZPQnZmZndYcU4zZVdcbjU0MUJkK1QwaEZoaE1TMUNURVlPVE9JWUhLNW50b3J4cHM0azVBNGpmbHdTWW5sTURsdGhtbzNScjB2aTBCWHBcblRaMzBNaHNnUmJpMkgvaTNOeS9BUXhoR0NnK1dNNEQzQXNxbkdSWFZaWjZodWRXL0llWm04OU1WeTNaekoyS3pcbi9ZTlA5R3hqQ3hoYkN0RHhMODZKd1h2aFdid1ByNWJuM29YQjBIOWs5eFhCOTFCMUxaUE5ZU3REQVUwUWJkekRcbnV3VjFaWTU1ZVFxblhJYXlYTjIzTWdrRUhkaUhFdW5US2Nva2xyNWtCR2diWHVVTXpjeXp5QWI0bWFqcElrMkdcbmoxenZ6azl5WE5zOVFYS3pQT0t4WnU2THVpMU1HS2l3KzhUdDJrRS8wUUtCZ1FEYmRrNE0va2RCejcraXR0RGZcblVNckN2WUhDeHpHZVowMGpDazRmY3RrODBWWDFQTUhXQ01UcmdQenVhRFI2bGlxd3FsRmh5a0Z0Ym9COVZBMmdcbnBocURONERLVVhaT3BmOGxsNkNpNmlnWEZ2K2YvdFRZR25aSExuU2FBZWN5UDRjMnh5cUdqejJHK0ZWNHIveWtcbjB6eUI5SFY5RS9WRStmR2p4d0xrYUNTY3ZRS0JnUURWc1daTUdwM3BHN0xkeVVoMXU1R0VHRzBiY0prY2ZSSElcbk5WR0Z3WGVjWGZ4TnR1YVovYmJoU0NiV1d4dzRhak43U2FNeUlRUFprN1p6OE9SUEpxcm1BdVh5KzkvZTMxTm1cblNKbktta2hHU0w4ZWFBSUJXbUlyd3BJMGtVZW1TY0Fna0I1a25MRzZhS0tMN2dBS0FCSm9QU3BpM3NUTDRhUTZcblZCM25FQWRmNlFLQmdRQzYrdC9qQ0I1ei91bTZXMkk0Y2VCZTdSRUtmNlpSM0plRy9EZTFhYWVXdnBBckFsbnZcbjNzYlFFN1RyOXRLSGd6dmFDektOUzhKVTF5b1lIYTRDcXFLNkcyYmFlVzkvOE1RakpqM1JzQWo2S3prZCt3TWpcbkgrQ1lJd2RyVTZYRVZRWUI4TXpWbW1NRWhMNWx0aU5kMXkrZFZVZG9pc0dVeEJOUnc3UStJRkRaVFFLQmdFSmVcbjFmbi9MbHFQZXM5OXk5NmRRa1gzM0ROd2xtMk52dThpK0U0RjErT2VJVlgxMzh1bVRKM043aG9YdEQwbFp4WDhcbnVnNUF5TlJVRjVBTkdxd213MStyT01adVNvc0xhL2pSeGNweWZzNHRuQWFTb1VUUVVMdHN0RUpWZVI2QnU0V2hcbm5YNzhXQXNnb3BPNWVST2lFQldHSWRzUmU2Z2pUZ0c3ekluT3BhdTVBb0dCQUtuaGNYaFBmR09JYmlrOEM0ZmRcbnNGc2F0eURSeGNNTHh5MldrTG1RdVRSNURPSzRTWnlUa2FWUHErVnNMckdBYTFhOVgwekUxN3N2Sm9BZWJBWFVcbjl0TFR0ZEhRVnREOE42blF1YnBVbXdKaVFOLytSNExjS1JxRldVKzV1T05SamFlRzlEdnJkVStiWjdDckdzT25cbnZUMWdKcFlsYXZPak4zMXM2L3J0Ym5sNFxuLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLVxuIiwgICAiY2xpZW50X2VtYWlsIjogInJlYWR5YXBpQGlkcC1kYXRhc3RvcmUuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iLCAgICJjbGllbnRfaWQiOiAiMTA1MjI4MTc0Mjk5MDEzMzkzMjc3IiwgICAiYXV0aF91cmkiOiAiaHR0cHM6Ly9hY2NvdW50cy5nb29nbGUuY29tL28vb2F1dGgyL2F1dGgiLCAgICJ0b2tlbl91cmkiOiAiaHR0cHM6Ly9vYXV0aDIuZ29vZ2xlYXBpcy5jb20vdG9rZW4iLCAgICJhdXRoX3Byb3ZpZGVyX3g1MDlfY2VydF91cmwiOiAiaHR0cHM6Ly93d3cuZ29vZ2xlYXBpcy5jb20vb2F1dGgyL3YxL2NlcnRzIiwgICAiY2xpZW50X3g1MDlfY2VydF91cmwiOiAiaHR0cHM6Ly93d3cuZ29vZ2xlYXBpcy5jb20vcm9ib3QvdjEvbWV0YWRhdGEveDUwOS9yZWFkeWFwaSU0MGlkcC1kYXRhc3RvcmUuaWFtLmdzZXJ2aWNlYWNjb3VudC5jb20iIH0"

//    String catalogID = testRunner.testCase.getPropertyValue("catalogId")
//    String formatDownloadURL = testRunner.testCase.getPropertyValue("formatDownloadURL")
//    String userRole = context.expand('${#TestCase#userRole}')
//    String userName = context.expand('${#Project#username.' + userRole + '}')
//    String apikey = context.expand('${#Global#apikey.' + userName + '}')
//    String gcpserviceaccount = context.expand('${gcp.serviceaccount}')
*/

String formatDownloadURL = "https://view-int.idp.private.geoapi-airbusds.com/api/v1/items/1222938c-703e-447a-813e-25c0c40d27de/download"
String targetDirectory = "/home/sebastien/Downloads/Format/snowy"
String apikey = "O2F7dUsNpfBje5sB0wIg95rNQIykAes4q6KBFZtvAh5NVBfec9Vr8RJr1EH_14RKd5HyLM6ziCFeX68xRW1twQ=="

try {
    String authentication = "Basic " + ("APIKEY:" + apikey).bytes.encodeBase64().toString()
    URL url = new URL(formatDownloadURL)
    URLConnection connection = url.openConnection()
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setRequestProperty("Authorization", authentication)
    InputStream inputStream = connection.getInputStream()

    ZipInputStream zipStream = new ZipInputStream(inputStream)
    ZipEntry entry

    while ((entry = zipStream.getNextEntry()) != null) {
        extractEntry(entry.getName(), entry.isDirectory(), zipStream, targetDirectory)
        zipStream.closeEntry()
    }

    zipStream.close()
} catch (Exception ex) {
    println(ex.getMessage())
}

void extractEntry(String name, Boolean isDirectory, FilterInputStream stream, String targetDirectory) throws IOException {
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

void mkdirsOrThrow(File dir) throws IOException {
    if (!dir.exists() && !dir.mkdirs()) {
        throw new IOException("Failed to create working directory. [$dir]")
    }
}