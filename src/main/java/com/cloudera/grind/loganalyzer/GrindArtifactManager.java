package com.cloudera.grind.loganalyzer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class GrindArtifactManager {

    @Value("${analyzer.surefire.filepattern}")
    String surefireReportFilePattern = "";

    void fetchSurefireReport(URL artifactUrl, Consumer<InputStream> reportParser) throws IOException {
        Pattern filePattern = Pattern.compile(surefireReportFilePattern);

        try (ZipInputStream artifactZip = new ZipInputStream(artifactUrl.openStream())) {
            ZipEntry entry;
            while ((entry = artifactZip.getNextEntry()) != null) {
                if (filePattern.matcher(entry.getName()).matches()) {
                    reportParser.accept(artifactZip);
                }
            }
        } catch (IOException ex) {
            if (!"Stream closed".equals(ex.getMessage())) {
                throw ex;
            }
        }
    }
}
