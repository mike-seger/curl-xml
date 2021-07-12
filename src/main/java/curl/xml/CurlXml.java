package curl.xml;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.toilelibre.libe.curl.Curl.curl;

public class CurlXml implements IOAware {
    private static final Logger logger = LoggerFactory.getLogger(CurlXml.class);
    private final static String fileArgMatcher = "-d  *[\"']*@([^\"']*)[\"']*";
    private final XslTransformer xslTransformer = new XslTransformer();

    public String call(final String url,
                       String inputString,
                       final List<String> inputTransformations,
                       final List<String> outputTransformations,
                       boolean inputIsCsv) {
        return this.call(url, List.of("Content-Type: text/xml"),
            inputString, inputTransformations, outputTransformations, inputIsCsv);
    }
    public String call(final String url,
           final List<String> headers, String inputString,
           final List<String> inputTransformations,
           final List<String> outputTransformations,
           boolean inputIsCsv) {
        String headerArgs="";
        if(headers.size()>0) {
            headerArgs="-H "+String.join("-H ",
                headers.stream()
                    .map(s -> "'" + escapeString(s) + "'")
                    .collect(Collectors.joining(", ")));
        }
        return call(headerArgs+(url==null?"":" "+url),
            inputString, inputTransformations, outputTransformations,
            false, inputIsCsv, url!=null);
    }

    public String call(String args, String inputString,
       final List<String> inputTransformations,
       final List<String> outputTransformations,
       boolean debug, boolean inputIsCsv, boolean remoteCall) {
        try {
            if(inputString==null) {
                final String input = args.replaceAll(".*" + fileArgMatcher + ".*", "$1");
                if (System.in.available() > 0) {
                    inputString = readInput(System.in).replace("'", "\\'");
                } else {
                    final File inputFile = new File(input.trim());
                    if (!inputFile.exists())
                        throw new FileNotFoundException(inputFile.getAbsolutePath());
                    else
                        inputString = readInput(getResource(inputFile.toURI().toString())).replace("'", "\\'");
                }
            }

            debug(debug, String.format("INPUT:\n%s\n\n", inputString.trim()));
            if (inputIsCsv) {
                String xsl = "classpath:/xsl/csv2xml.xsl";
                inputString = xslTransformer.transform(string2InputStream("<x/>"),
                    getResource(xsl), Map.of("csv-data", inputString));
                debug(debug, String.format("CSV -> XML %s:\n%s\n", xsl, inputString));
            }

            for (final String xsl : inputTransformations) {
                inputString = xslTransformer.transform(string2InputStream(inputString), getResource(xsl));
                debug(debug, String.format("Input TR %s ->:\n%s\n", xsl, inputString));
            }

            String result = inputString;
            if (remoteCall) {
                final String curlArgs =
                    args.replaceAll(fileArgMatcher, "")
                        +" -d '" + inputString.replace("'", "\\'") + "'";
                final HttpResponse response = curl(curlArgs);
                final HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    result = readInput(responseEntity.getContent());
                    debug(debug, String.format("CURL result:\n%s\n\n", result.trim()));
                    result = xslTransformer.transform(string2InputStream(result), getClass().getResourceAsStream("/xsl/strip-ns.xsl"));
                } else {
                    throw new IllegalStateException("Curl call failed");
                }
            }
            for (final String xsl : outputTransformations) {
                result = xslTransformer.transform(string2InputStream(result), getResource(xsl));
                debug(debug, String.format("Output TR %s ->:\n%s\n", xsl, result));
            }
            result = xslTransformer.transform(string2InputStream(result), getClass().getResourceAsStream("/xsl/strip-ns.xsl"));
            debug(debug, String.format("Final result:\n%s\n\n", result.trim()));
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("App args: "+args, e);
        }
    }

    private String escapeString(String s) {
        return s.replace("'", "\\'");
    }

    private void debug(boolean debug, String message) {
        if(logger.isDebugEnabled()) {
            logger.debug(message);
        } else if(debug)
            System.out.print(message);
    }
}
