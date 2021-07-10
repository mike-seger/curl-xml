package curl.xml;

import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.saxon.s9api.*;
import net.sf.saxon.functions.*;

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

    public String transform(InputStream xmlIn, InputStream xslIn) throws TransformerException, IOException, SaxonApiException {
        /*try (InputStream xml = xmlIn)*/ {
            /*try (InputStream xslt = xslIn)*/ {
                Processor processor = new Processor(false);
                processor.setConfigurationProperty(net.sf.saxon.lib.Feature.SUPPRESS_XSLT_NAMESPACE_CHECK, false);
                processor.setConfigurationProperty(net.sf.saxon.lib.Feature.ERROR_LISTENER_CLASS,
                        NullErrorListener.class.getName());
//                XsltCompiler comp = processor.newXsltCompiler();
//                XsltExecutable exp = comp.compile(new StreamSource(xslIn));

                Source xmlSource = new StreamSource(xmlIn);
                StringWriter sw = new StringWriter();
                //Result result = new StreamResult(sw);

                XsltCompiler compiler = processor.newXsltCompiler();
                XsltExecutable stylesheet = compiler.compile(new StreamSource(xslIn));
                Serializer out = processor.newSerializer(sw);

                //out.setOutputProperty(Serializer.Property.METHOD, "text");
                //out.setOutputProperty(Serializer.Property.INDENT, "no");
//                TransformerFactory factory = TransformerFactory.newInstance();
//                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
//                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
//                Transformer transformer = factory.newTransformer(new StreamSource(xslt));
//                transformer.transform(xmlSource, out);
                //XsltTransformer trans = exp.load();
                Xslt30Transformer trans = stylesheet.load30();
                //trans.setInitialContextNode(source);
                //trans.setDestination(out);
                trans.transform(xmlSource, out);
                return sw.toString();
            }
        }
    }

    public class NullErrorListener implements ErrorListener {
        @Override public void warning(TransformerException e) throws TransformerException { }
        @Override public void error(TransformerException e) throws TransformerException { }
        @Override public void fatalError(TransformerException e) throws TransformerException { }
    }
}
