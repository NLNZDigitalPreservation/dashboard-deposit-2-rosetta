package nz.govt.natlib.dashboard.common.mock;

import com.exlibrisgroup.xsd.dps.backoffice.service.ProducerAccountType;
import com.exlibrisgroup.xsd.dps.backoffice.service.ProducerInfoDocument;
import com.exlibrisgroup.xsd.dps.backoffice.service.ProducerStatus;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;

public class MockProducerWebServices {
    @Test
    public void test() throws IOException, XmlException {
        String xml = getProducerXml();

//        System.out.println(xml);
        assert xml != null;
        assert xml.contains("xml");
    }

    public static String getProducerXml() throws IOException {
        ProducerInfoDocument producerDoc = ProducerInfoDocument.Factory.newInstance();
        ProducerInfoDocument.ProducerInfo producerInfo_01 = producerDoc.addNewProducerInfo();
        producerInfo_01.setStatus(ProducerStatus.ACTIVE);
        producerInfo_01.setExpiryDate("08/02/2010");
        producerInfo_01.setTelephone1("1800-888-999");
        producerInfo_01.setTelephone2("");
        producerInfo_01.setWebSite("");
        producerInfo_01.setZipCode(Long.parseLong("68745"));
        producerInfo_01.setEmail("mail@info.org");
        producerInfo_01.setInstitution("my institution");
        producerInfo_01.setDepartment("my department");
        producerInfo_01.setStreet("Emek Hachula 13");
        producerInfo_01.setSuburb("");
        producerInfo_01.setCity("Michil");
        producerInfo_01.setCountry("Israel");
        producerInfo_01.setAuthoritativeName("Meat and Wool");
        producerInfo_01.setAccountType(ProducerAccountType.GROUP);
        producerInfo_01.setEmailNotify("Y");
        producerInfo_01.setNotes("");
        producerInfo_01.setLocalField1("");
        producerInfo_01.setFax("");
        producerInfo_01.setNegotiatorId(Long.parseLong("68745"));
        producerInfo_01.setContactUserId2(Long.parseLong("0"));
        producerInfo_01.setContactUserId3(Long.parseLong("0"));
        producerInfo_01.setContactUserId4(Long.parseLong("0"));
        producerInfo_01.setContactUserId5(Long.parseLong("0"));
        producerInfo_01.setProducerProfileId(1L);

        ProducerInfoDocument.ProducerInfo producerInfo_02 = producerDoc.addNewProducerInfo();
        producerInfo_02.setStatus(ProducerStatus.ACTIVE);
        producerInfo_02.setExpiryDate("08/02/2010");
        producerInfo_02.setTelephone1("1800-888-999");
        producerInfo_02.setTelephone2("");
        producerInfo_02.setWebSite("");
        producerInfo_02.setZipCode(Long.parseLong("68745"));
        producerInfo_02.setEmail("mail@info.org");
        producerInfo_02.setInstitution("my institution");
        producerInfo_02.setDepartment("my department");
        producerInfo_02.setStreet("Emek Hachula 13");
        producerInfo_02.setSuburb("");
        producerInfo_02.setCity("Michil");
        producerInfo_02.setCountry("Israel");
        producerInfo_02.setAuthoritativeName("Meat and Wool");
        producerInfo_02.setAccountType(ProducerAccountType.GROUP);
        producerInfo_02.setEmailNotify("Y");
        producerInfo_02.setNotes("");
        producerInfo_02.setLocalField1("");
        producerInfo_02.setFax("");
        producerInfo_02.setNegotiatorId(Long.parseLong("68745"));
        producerInfo_02.setContactUserId2(Long.parseLong("0"));
        producerInfo_02.setContactUserId3(Long.parseLong("0"));
        producerInfo_02.setContactUserId4(Long.parseLong("0"));
        producerInfo_02.setContactUserId5(Long.parseLong("0"));
        producerInfo_02.setProducerProfileId(1L);

        OutputStream os = new ByteArrayOutputStream();
        producerDoc.save(os);

        return os.toString();
    }
}
