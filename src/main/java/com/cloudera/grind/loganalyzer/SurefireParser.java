package com.cloudera.grind.loganalyzer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SurefireParser {

    public class ReportTestCase {
        private String methodName;
        private String className;
        private Boolean success;
        private String message;
        private byte[] systemOut;

        ReportTestCase(String methodName, String className, Boolean success, String message, byte[] systemOut) {
            this.methodName = methodName;
            this.className = className;
            this.success = success;
            this.message = message;
            this.systemOut = systemOut;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getClassName() {
            return className;
        }

        public Boolean getSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public byte[] getSystemOut() {
            return systemOut;
        }

        @Override
        public String toString() {
            return "ReportTestCase{" +
                    "methodName='" + methodName + '\'' +
                    ", className='" + className + '\'' +
                    ", success=" + success +
                    ", message='" + message + '\'' +
                    ", systemOut='" + Arrays.toString(systemOut) + '\'' +
                    '}';
        }
    }

    public List<ReportTestCase> parseSurefireReport(InputStream report) throws IOException, SAXException, ParserConfigurationException {
        Document reportDocument = Jsoup.parse(report, "UTF-8", "", Parser.xmlParser());
        reportDocument.outputSettings(new Document.OutputSettings().prettyPrint(false));

        return reportDocument.select("testcase")
                .stream()
                .map(element -> {
                            Elements failureElements = element.select("failure");
                            String failure = failureElements.isEmpty() ? "" : failureElements.text();

                            Elements systemErrElements = element.select("system-err");
                            byte systemErr[] = new byte[] {};
                            if (!systemErrElements.isEmpty() && systemErrElements.get(0).childNodeSize()>0) {
                                systemErr = systemErrElements.get(0).childNode(0).toString().getBytes();
                            }

                            return new ReportTestCase(
                                    element.attr("name"),
                                    element.attr("classname"),
                                    failure.length() == 0,
                                    failure,
                                    systemErr
                            );
                        }
                )
                .collect(Collectors.toList());
    }
}
