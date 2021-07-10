package curl.xml;

import net.sf.saxon.s9api.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
                    inputString = Files.readString(inputFile.toPath()).replace("'", "\\'");
            }

            //System.out.println("inputString: "+inputString);
            if(inputCsv.get()) {
                System.out.println("XML: " + transform(new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8)), getClass().getResourceAsStream("/xsl/csv2xml2.xsl")));
            }

            final String curlArgs = appArgs.replaceAll(fileArgMatcher, "")
                    .replaceAll("$", " -d '"+inputString+"'");
            final HttpResponse response = curl(curlArgs);
            final HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                System.out.println(transform(responseEntity.getContent(), getClass().getResourceAsStream("/xsl/strip-ns.xsl")));
            }
        } catch (Exception e) {
            System.out.println(appArgs);
            e.printStackTrace();
        }
    }

    public String readInput(InputStream is) throws IOException {
        try (InputStream inputStream = is) {
            return new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
        }
    }

    public String transform(InputStream xmlIn, InputStream xslIn) throws IOException, SaxonApiException {
        try (InputStream xml = xmlIn) {
            try (InputStream xslt = xslIn) {
                Processor processor = new Processor(false);
                Source xmlSource = new StreamSource(xml);
                StringWriter sw = new StringWriter();
                XsltCompiler compiler = processor.newXsltCompiler();
                XsltExecutable stylesheet = compiler.compile(new StreamSource(xslt));
                Serializer out = processor.newSerializer(sw);
                Xslt30Transformer trans = stylesheet.load30();
                trans.transform(xmlSource, out);
                return sw.toString();
            }
        }
    }
}
