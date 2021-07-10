package curl.xml;

import net.sf.saxon.s9api.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.toilelibre.libe.curl.Curl.curl;

public class App {
    public static void main(final String[] args) {
        new App().run(args);
    }

    public void run(String[] args) {
        if(args.length==0) {
            System.out.println("No curl arguments provided. See https://github.com/libetl/curl");
            System.exit(1);
        }
        boolean doQuote=false;
        List<String> argList=Arrays.asList(args).stream().map(arg -> {
            if(arg.matches("^[\"@\\-].*")) return arg;
            return "\""+arg.replaceAll("\"", "\\\"")+"\"";
        }).collect(Collectors.toList());

        String appArgs = String.join(" ", argList);
        try {
            final String fileArgMatcher = "-d  *[\"']*@([^\"']*)[\"']*";
            final File input = new File(appArgs.replaceAll(".*"+fileArgMatcher+".*", "$1"));
            if(!input.exists()) {
                throw new FileNotFoundException(input.getAbsolutePath());
            }
            final String inputString = Files.readString(input.toPath()).replace("'", "\\'");
            final String curlArgs = appArgs.replaceAll(fileArgMatcher, "-d '"+inputString+"'");
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
