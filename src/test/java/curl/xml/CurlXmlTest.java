package curl.xml;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CurlXmlTest implements TestAware {
    @Test
    public void testSoapServer() throws IOException {
        String result = new CurlXml().call(
            "https://ms-soap-server.herokuapp.com/ws",
            "name\nSpain\nFrance",
            List.of("classpath:/soap-server/name-list-2-request.xsl"),
            Collections.emptyList(), true);
        assertXmlMatches("/soap-server/result.xml", result);
    }

    @Test
    public void testInputTransform() throws IOException {
        String result = new CurlXml().call(
            null,
            "name\nSpain\nFrance",
            List.of("classpath:/soap-server/name-list-2-request.xsl"),
            Collections.emptyList(), true);
        assertXmlMatches("/soap-server/request-ns-stripped.xml", result);
    }
}
