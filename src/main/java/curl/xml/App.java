package curl.xml;

import net.sf.saxon.s9api.*;
import net.sf.saxon.value.AtomicValue;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
        final AtomicBoolean inputCsv = new AtomicBoolean(false);
        final List<String> argList= Arrays.stream(args).filter(arg -> {
            if(arg.matches("-inTR=.*")) {
                inputTransformations.add(arg.substring(arg.indexOf('=')+1));
                return false;
            } else if(arg.matches("-outTR=.*")) {
                outputTransformations.add(arg.substring(arg.indexOf('=')+1));
                return false;
            } else if(arg.equals("-inCSV")) {
                inputCsv.set(true);
                return false;
            }
            return true;
        }).map(arg -> {
            if(arg.matches("^[\"@\\-].*")) return arg;
            return "\""+arg.replaceAll("\"", "\\\"")+"\"";
        }).collect(Collectors.toList());

        if(args.length==0) {
            System.out.println("No curl arguments provided. See https://github.com/libetl/curl");
            System.exit(1);
        }

        String appArgs = String.join(" ", argList);
        try {
            final String fileArgMatcher = "-d  *[\"']*@([^\"']*)[\"']*";
            String input = appArgs.replaceAll(".*"+fileArgMatcher+".*", "$1");
            String inputString;
            if(System.in.available()>0) {
                inputString=readInput(System.in).replace("'", "\\'");
            } else {
                final File inputFile = new File(input);
                if(!inputFile.exists())
                    throw new FileNotFoundException(inputFile.getAbsolutePath());
                else
                    inputString = readInput(getResource(inputFile.toURI().toString())).replace("'", "\\'");
            }

            //System.out.println("inputString: "+inputString);
            if(inputCsv.get()) {
                inputString = transform(string2InputStream("<root></root>"),
                    getResource("classpath:/xsl/csv2xml.xsl"),
                    Map.of("csv-data", inputString));
                System.out.println(inputString);
                for(String xsl : inputTransformations) {
                    inputString = transform(string2InputStream(inputString), getResource(xsl));
                    System.out.println(inputString);
                }
            }

            final String curlArgs = appArgs.replaceAll(fileArgMatcher, "")
                .replaceAll("$", " -d '"+inputString+"'");
            final HttpResponse response = curl(curlArgs);
            final HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String result = transform(responseEntity.getContent(), getClass().getResourceAsStream("/xsl/strip-ns.xsl"));
                for(String xsl : outputTransformations) {
                    result = transform(string2InputStream(result), getResource(xsl));
                    System.out.println(result);
                }
                result = transform(string2InputStream(result), getClass().getResourceAsStream("/xsl/strip-ns.xsl"));
                System.out.println(result);
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

    public String transform(InputStream xmlIn, InputStream xslIn, Map.Entry<String, String> ... params) throws IOException, SaxonApiException {
        return  transform(xmlIn, xslIn, Arrays.stream(params).collect(Collectors.toMap(
            Map.Entry::getKey, Map.Entry::getValue)));
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
                //params.put(new QName("csv-uri"), );
                transformer.setStylesheetParameters(stylesheetParameters);
                transformer.transform(xmlSource, serializer);
                return output.toString();
            }
        }
    }
}
