package curl.xml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public interface IOAware {
    default InputStream getResource(String url) throws IOException {
        if(url.startsWith("classpath:")) {
            return getClass().getResourceAsStream(url.substring(url.indexOf(':')+1));
        }
        return new FileInputStream(url.substring(url.indexOf(":/")+1).replaceAll("^//*", "/"));
    }

    default InputStream string2InputStream(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    default String readInput(InputStream is) throws IOException {
        try (InputStream inputStream = is) {
            return new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
        }
    }

    default String loadResource(String url) throws IOException {
        return readInput(getResource(url));
    }
}
