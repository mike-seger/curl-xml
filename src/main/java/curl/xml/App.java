package curl.xml;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;

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
        String appArgs = String.join(" ", args);
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

    public String transform(InputStream xmlIn, InputStream xslIn) throws TransformerException, IOException {
        try (InputStream xml = xmlIn) {
            try (InputStream xslt = xslIn) {
                Source xmlSource = new StreamSource(xml);
                StringWriter sw = new StringWriter();
                Result out = new StreamResult(sw);
                TransformerFactory factory = TransformerFactory.newInstance();
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
                Transformer transformer = factory.newTransformer(new StreamSource(xslt));
                transformer.transform(xmlSource, out);
                return sw.toString();
            }
        }
    }
}
