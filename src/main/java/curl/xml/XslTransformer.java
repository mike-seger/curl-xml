package curl.xml;

import net.sf.saxon.s9api.*;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class XslTransformer {

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
