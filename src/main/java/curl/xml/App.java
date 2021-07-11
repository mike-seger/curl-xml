package curl.xml;

import net.sf.saxon.s9api.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.toilelibre.libe.curl.Curl.curl;

public class App {
    public static void main(final String[] args) {
        new App().run(args);
    }

    public void run(String[] args) {
        final List<String> inputTransformations=new ArrayList<>();
        final List<String> outputTransformations=new ArrayList<>();
        final AtomicBoolean inputIsCsv = new AtomicBoolean(false);
        final AtomicBoolean hasHttp = new AtomicBoolean(false);
        final List<String> argList= Arrays.stream(args).filter(arg -> {
            arg = arg.trim();
            if(!hasHttp.get()) {
                hasHttp.set(arg.matches("https*://.*"));
            }
            if(arg.length()==0) {
                return false;
            } else if(arg.matches("-inTR=.*")) {
                inputTransformations.add(arg.substring(arg.indexOf('=')+1));
                return false;
            } else if(arg.matches("-outTR=.*")) {
                outputTransformations.add(arg.substring(arg.indexOf('=')+1));
                return false;
            } else if(arg.equals("-inCSV")) {
                inputIsCsv.set(true);
                return false;
            }
            return true;
        }).map(arg -> {
            arg = arg.trim();
            if(arg.matches("^[\"@\\-].*")) return arg;
            return "\""+arg.replaceAll("\"", "\\\"")+"\"";
        }).collect(Collectors.toList());

        String appArgs = String.join(" ", argList);
        try {
            final String fileArgMatcher = "-d  *[\"']*@([^\"']*)[\"']*";
            final String input = appArgs.replaceAll(".*" + fileArgMatcher + ".*", "$1");
            String inputString;
            if (System.in.available() > 0) {
                inputString = readInput(System.in).replace("'", "\\'");
            } else {
                final File inputFile = new File(input.trim());
                if (!inputFile.exists())
                    throw new FileNotFoundException(inputFile.getAbsolutePath());
                else
                    inputString = readInput(getResource(inputFile.toURI().toString())).replace("'", "\\'");
            }

            System.out.printf("INPUT:\n%s\n\n", inputString.trim());
            if (inputIsCsv.get()) {
                String xsl = "classpath:/xsl/csv2xml.xsl";
                inputString = transform(string2InputStream("<x/>"),
                    getResource(xsl), Map.of("csv-data", inputString));
                System.out.printf("CSV -> XML %s:\n%s\n", xsl, inputString);
            }

            for (final String xsl : inputTransformations) {
                inputString = transform(string2InputStream(inputString), getResource(xsl));
                System.out.printf("Input TR %s ->:\n%s\n", xsl, inputString);
            }

            String result = inputString;
            boolean callSucceeded = true;
            if (hasHttp.get()) {
                callSucceeded = false;
                final String curlArgs =
                    appArgs.replaceAll(fileArgMatcher, "")
                    +" -d '" + inputString + "'";
                final HttpResponse response = curl(curlArgs);
                final HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    result = readInput(responseEntity.getContent());
                    System.out.printf("CURL result:\n%s\n\n", result.trim());
                    result = transform(string2InputStream(result), getClass().getResourceAsStream("/xsl/strip-ns.xsl"));
//                    result = transform(string2InputStream(result), getClass().getResourceAsStream("/xsl/format.xsl"));
                    callSucceeded = true;
                }
            }
            if (callSucceeded) {
                for (final String xsl : outputTransformations) {
                    result = transform(string2InputStream(result), getResource(xsl));
                    System.out.printf("Output TR %s ->:\n%s\n", xsl, result);
                }
                result = transform(string2InputStream(result), getClass().getResourceAsStream("/xsl/strip-ns.xsl"));
                System.out.printf("Final result:\n%s\n\n", result.trim());
            }
        } catch (Exception e) {
            System.out.println(appArgs);
            e.printStackTrace();
        }
    }

    private InputStream getResource(String url) throws IOException {
        if(url.startsWith("classpath:")) {
            return getClass().getResourceAsStream(url.substring(url.indexOf(':')+1));
        }
        return new FileInputStream(url.substring(url.indexOf(":/")+1).replaceAll("^//*", "/"));
    }

    private InputStream string2InputStream(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    public String readInput(InputStream is) throws IOException {
        try (InputStream inputStream = is) {
            return new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
        }
    }

    public String transform(InputStream xmlIn, InputStream xslIn) throws IOException, SaxonApiException {
        return  transform(xmlIn, xslIn, Collections.emptyMap());
    }

    public String transform(InputStream xmlIn, InputStream xslIn, Map<String, String> params) throws IOException, SaxonApiException {
        try (InputStream xml = xmlIn) {
            try (InputStream xslt = xslIn) {
                Processor processor = new Processor(false);
                Source xmlSource = new StreamSource(xml);
                StringWriter output = new StringWriter();
                XsltCompiler compiler = processor.newXsltCompiler();
                XsltExecutable stylesheet = compiler.compile(new StreamSource(xslt));
                Serializer serializer = processor.newSerializer(output);
                Xslt30Transformer transformer = stylesheet.load30();
                Map<QName, XdmValue> stylesheetParameters = params.entrySet().stream()
                    .map(e -> new AbstractMap.SimpleEntry<QName, XdmValue>(
                        new QName(e.getKey()), new XdmAtomicValue(e.getValue())))
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
                transformer.setStylesheetParameters(stylesheetParameters);
                transformer.transform(xmlSource, serializer);
                return output.toString();
            }
        }
    }
}
