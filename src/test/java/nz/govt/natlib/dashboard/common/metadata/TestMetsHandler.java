package nz.govt.natlib.dashboard.common.metadata;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

public class TestMetsHandler {
    @Test
    public void testParse() throws IOException {
        Resource resource = new ClassPathResource("mets.xml");
        MetsXmlProperties prop = MetsHandler.parse(resource.getInputStream());
        System.out.println(prop);
    }
}
