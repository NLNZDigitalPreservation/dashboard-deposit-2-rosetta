package nz.govt.natlib.dashboard.common.metadata;

import nz.govt.natlib.dashboard.util.DashboardHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

public class MetsHandler extends DefaultHandler {
    private MetsXmlProperties prop;
    private String elementValue;
    private String keyId;
    private MetsXmlProperties.GeneralFileCharacters generalFileCharacters;

    @Override
    public void startDocument() throws SAXException {
        prop = new MetsXmlProperties();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.elementValue = (new String(ch, start, length)).trim();
    }

    @Override
    public void startElement(String uri, String lName, String qName, Attributes attr) throws SAXException {
        String id = attr.getValue("id");
        if (qName.equalsIgnoreCase("key")) {
            this.keyId = id;
        } else {
            this.keyId = null;
        }
        if (qName.equalsIgnoreCase("section") && !DashboardHelper.isEmpty(id) && id.equalsIgnoreCase("generalFileCharacteristics")) {
//            prop.newGeneralFileCharacters();
            generalFileCharacters = new MetsXmlProperties.GeneralFileCharacters();
            prop.getListFiles().add(generalFileCharacters);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "dc:title":
                this.prop.setTitle(this.elementValue);
                break;
            case "section":

            case "key":
                if (DashboardHelper.isEmpty(this.keyId) || DashboardHelper.isEmpty(this.elementValue)) {
                    //Skip
                } else if (this.keyId.equalsIgnoreCase("objectIdentifierType")) {
                    this.prop.setObjectIdentifierType(this.elementValue);
                } else if (this.keyId.equalsIgnoreCase("objectIdentifierValue")) {
                    this.prop.setObjectIdentifierValue(this.elementValue);
                } else if (this.keyId.equalsIgnoreCase("recordId")) {
                    prop.setRecordId(this.elementValue);
                } else if (this.keyId.equalsIgnoreCase("system")) {
                    prop.setSystem(this.elementValue);
                } else if (this.keyId.equalsIgnoreCase("label")) {
                    generalFileCharacters.setLabel(this.elementValue);
                } else if (this.keyId.equalsIgnoreCase("fileMIMEType")) {
                    generalFileCharacters.setFileMIMEType(this.elementValue);
                } else if (this.keyId.equalsIgnoreCase("fileCreationDate")) {
                    generalFileCharacters.setFileCreatedDate(this.elementValue);
                } else if (this.keyId.equalsIgnoreCase("fileModificationDate")) {
                    generalFileCharacters.setFileModificationDate(this.elementValue);
                } else if (this.keyId.equalsIgnoreCase("fileOriginalName")) {
                    generalFileCharacters.setFileOriginalName(this.elementValue);
                } else if (this.keyId.equalsIgnoreCase("fileOriginalPath")) {
                    generalFileCharacters.setFileOriginalPath(this.elementValue);
                } else if (this.keyId.equalsIgnoreCase("fileSizeBytes")) {
                    generalFileCharacters.setFileSizeBytes(this.elementValue);
                }
                System.out.println(this.keyId + ": " + this.elementValue);
                break;
        }
    }

    public static MetsXmlProperties parse(InputStream inputStream) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            MetsHandler handler = new MetsHandler();
            saxParser.parse(inputStream, handler);
            return handler.prop;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
