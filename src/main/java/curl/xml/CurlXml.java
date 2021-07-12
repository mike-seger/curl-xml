package curl.xml;

import com.roxstudio.utils.CUrl;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.toilelibre.libe.curl.Curl.curl;

public class CurlXml implements IOAware {
    private static final Logger logger = LoggerFactory.getLogger(CurlXml.class);
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
        List<String> argList = new ArrayList<>();
        headers.forEach(header -> {
            argList.add("-H");
            argList.add(header);
        });
        argList.add(url);
        return call(argList, inputString, inputTransformations,
            outputTransformations, false, inputIsCsv, url!=null);
    }

    public String call(List<String> argList, String inputString,
       final List<String> inputTransformations,
       final List<String> outputTransformations,
       boolean debug, boolean inputIsCsv, boolean remoteCall) {
        final List<String> curlArgs = new ArrayList<>();
        try {
            if(inputString==null) {
                if (System.in.available() > 0) {
                    inputString = readInput(System.in);
                } else {
                    int dataIndex = argList.indexOf("-d");
                    if(dataIndex>=0 && argList.size()>dataIndex+1) {
                        String input = argList.get(dataIndex+1);
                        if(input.startsWith("@")) {
                            input = input.substring(1);
                            final File inputFile = new File(input.trim());
                            if (!inputFile.exists())
                                throw new FileNotFoundException(inputFile.getAbsolutePath());
                            else
                                inputString = readInput(getResource(inputFile.toURI().toString())).replace("'", "\\'");
                        } else {
                            inputString = input;
                        }
                        argList.remove(dataIndex+1);
                        argList.remove(dataIndex);
                    }
                }
            }

            inputString=inputString!=null?inputString.trim():"";
            debug(debug, String.format("INPUT:\n%s\n\n", inputString));
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
                argList.stream().map(this::quoteNonOption).forEach(curlArgs::add);
                curlArgs.add("-d");
                curlArgs.add(quoteNonOption(inputString));
//                    argList.stream().map(this::quoteNonOption)
//                        .collect(Collectors.joining(" "))
//                        +" -d" + quoteNonOption(inputString);

//                final HttpResponse response = curl(String.join(" ", curlArgs));
//                final HttpEntity responseEntity = response.getEntity();
//                if (responseEntity != null) {
//                    result = readInput(responseEntity.getContent());
//                    debug(debug, String.format("CURL result:\n%s\n\n", result.trim()));
//                } else {
//                    throw new IllegalStateException("Curl call failed");
//                }

                CUrl curl = new CUrl().opt(curlArgs.toArray(new String[0]));
                result = curl.exec("UTF8");
                if(curl.getHttpCode()>=400) {
                    throw new IllegalStateException("Curl call failed: "+curl.getHttpCode()+"\n"+result);
                }

                result = xslTransformer.transform(string2InputStream(result), getClass().getResourceAsStream("/xsl/strip-ns.xsl"));
            }
            for (final String xsl : outputTransformations) {
                result = xslTransformer.transform(string2InputStream(result), getResource(xsl));
                debug(debug, String.format("Output TR %s ->:\n%s\n", xsl, result));
            }
            result = xslTransformer.transform(string2InputStream(result), getClass().getResourceAsStream("/xsl/strip-ns.xsl"));
            debug(debug, String.format("Final result:\n%s\n\n", result.trim()));
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("CURL args: ["+String.join(" ", curlArgs)+"]", e);
        }
    }

    private String quoteNonOption(String s) {
        if(s.startsWith("-")) return s;
        return "'"+s.replace("'", "\\'")+"'";
    }

    private void debug(boolean debug, String message) {
        if(logger.isDebugEnabled()) {
            logger.debug(message);
        } else if(debug)
            System.out.print(message);
    }
}
