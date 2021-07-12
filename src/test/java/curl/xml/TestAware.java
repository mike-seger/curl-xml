package curl.xml;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public interface TestAware extends IOAware {
    default String dos2IUnixString(String s) {
        return s.replace("\r", "");
    }

    default void assertXmlMatches(String location, String xml) throws IOException {
        assertEqualXml(loadResource("classpath:"+location), xml);
    }

    default void assertEqualXml(String xml1, String xml2) {
        assertEquals(
            dos2IUnixString(xml1).trim(), dos2IUnixString(xml2).trim());
    }
}
