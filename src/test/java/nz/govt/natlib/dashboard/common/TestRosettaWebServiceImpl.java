package nz.govt.natlib.dashboard.common;

import com.exlibris.dps.*;
import nz.govt.natlib.dashboard.common.core.RosettaWebServiceImpl;
import nz.govt.natlib.dashboard.util.DashboardHelper;
import nz.govt.natlib.dashboard.viewer.MediaFile;
import nz.govt.natlib.ndha.common.exceptions.XmlException;
import nz.govt.natlib.ndha.common.exlibris.MaterialFlow;
import nz.govt.natlib.ndha.common.exlibris.Producer;
import nz.govt.natlib.ndha.common.ilsquery.criteria.*;
import nz.govt.natlib.ndha.common.xmltransformer.DcToHtmlTransformer;
import nz.govt.natlib.ndha.common.xmltransformer.DcToHtmlTransformerImpl;
import nz.govt.natlib.ndha.rosettaIEMetaDataParser.DOMBasedIEMetaDataParser;
import nz.govt.natlib.ndha.rosettaIEMetaDataParser.FileModel;
import nz.govt.natlib.ndha.rosettaIEMetaDataParser.IEModel;
import nz.govt.natlib.ndha.rosettaIEMetaDataParser.RepresentationModel;
import nz.govt.natlib.ndha.srusearchclient.SruRequest;
import nz.govt.natlib.ndha.srusearchclient.SruService;
import nz.govt.natlib.ndha.srusearchclient.impl.SruRequestImpl;
import nz.govt.natlib.ndha.srusearchclient.impl.SruServiceImpl;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.Exception;
import java.net.CookieHandler;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestRosettaWebServiceImpl {
    private static String PDSUrl = "https://slbpdstest.natlib.govt.nz/pds?";
    private static String ProducerWsdlUrl = "https://wlguatdpsilb.natlib.govt.nz/dpsws/deposit/ProducerWebServices?wsdl";
    private static String DepositWsdlUrl = "https://wlguatdpsilb.natlib.govt.nz/dpsws/deposit/DepositWebServices?wsdl";
    private static String SipWsdlUrl = "https://wlguatoprilb.natlib.govt.nz/dpsws/repository/SipWebServices?wsdl";
    private static String DPSSearchUrl = "https://wlguatdpsilb.natlib.govt.nz/delivery/sru";
    private static String DeliveryAccessWsdlUrl = "https://wlguatdpsilb.natlib.govt.nz/dpsws/delivery/DeliveryAccessWS?wsdl";
    private static RosettaWebServiceImpl rosettaWebService = new RosettaWebServiceImpl();

    private static final String INSTITUTION = "INS00";
    private static final String USERNAME = "leefr";
    private static final String PASSWORD = "wangyang+115";

    @BeforeAll
    static void init() throws Exception {
//        String proxyHost = "192.168.1.65";
//        String proxyPort = "3128";
//        String proxyHost = "wlgproxyforservers.dia.govt.nz";
//        String proxyPort = "8080";
//        System.setProperty("http.proxyHost", proxyHost);
//        System.setProperty("http.proxyPort", proxyPort);
//        System.setProperty("https.proxyHost", proxyHost);
//        System.setProperty("https.proxyPort", proxyPort);
        rosettaWebService.init(PDSUrl, ProducerWsdlUrl, DepositWsdlUrl, SipWsdlUrl, DeliveryAccessWsdlUrl);
    }

    @Test
    public void testGetDepositActivityByUpdateDateByMaterialFlow() throws Exception {
        String pdsHandle = rosettaWebService.login(INSTITUTION, USERNAME, PASSWORD);
//        PdsUserInfo pdsUserInfo = rosettaWebService.getPdsUserByPdsHandle(pdsHandle);
        String producerAgentID = rosettaWebService.getInternalUserIdByExternalId("leefr");
        String depositActivityStatus = "all";
        String updateDateFrom = "01/01/2020";
        String updateDateTo = "18/09/2020";
        String startRecord = "1";
        String endRecord = "20";

        List<Producer> producers = rosettaWebService.getProducers("leefr");
        for (Producer producer : producers) {
            List<MaterialFlow> materialFlows = rosettaWebService.getMaterialFlows(producer.getID());
            for (MaterialFlow materialFlow : materialFlows) {
                String materialFlowId = materialFlow.getID();
                String producerID = producer.getID();
                String reply = rosettaWebService.getDepositActivityBySubmitDateByMaterialFlow(pdsHandle,
                        depositActivityStatus,
                        materialFlowId,
                        producerID,
                        producerAgentID,
                        updateDateFrom,
                        updateDateTo,
                        startRecord,
                        endRecord
                );

                System.out.println(reply);
            }
        }
    }

    @Test
    public void testGetSipIEs() {
        String sipId = "742449";
        SipWebServices sipApi = rosettaWebService.getSipWebServices();
        String reply = sipApi.getSipIEs(sipId);
        System.out.println(reply);
    }

    @Test
    public void testGetSipStatus() {
        String sipId = "742449";
        SipWebServices sipApi = rosettaWebService.getSipWebServices();
        String reply = sipApi.getSipStatus(sipId);
        System.out.println(reply);
    }

    @Test
    public void testGetSipStatusInfo() throws Exception_Exception {
        String sipId = "742449";
        SipWebServices sipApi = rosettaWebService.getSipWebServices();
        SipStatusInfo reply = sipApi.getSIPStatusInfo(sipId);
        System.out.println(reply);
    }

    @Test
    public void testHeartbeat() throws Exception_Exception {
        SipWebServices sipApi = rosettaWebService.getSipWebServices();
        String reply = sipApi.getHeartBit();
        System.out.println(reply);
    }

    @Test
    public void testGetProducerDetails() throws Exception {
        String pdsHandle = rosettaWebService.login(INSTITUTION, USERNAME, PASSWORD);
        String producerId = "6355932079";
        ProducerWebServices pApi = rosettaWebService.getProducerWebServices();
        String reply = pApi.getProducerDetails(pdsHandle, producerId);
        System.out.println(reply);
    }

    @Ignore
//    @Test
    public void testDeliveryAccess() throws Exception {
        String pdsHandle = rosettaWebService.getPdsClient().login(INSTITUTION, USERNAME, PASSWORD);

        DeliveryAccessWS deliveryWS = rosettaWebService.getDeliveryAccessWS();
        assert deliveryWS != null;

        String dps_pid = "IE40340534";
        String dps_dvs = generateDPSSession(dps_pid, pdsHandle);
//        String dps_dvs = "1601599176269~691";
        assert !DashboardHelper.isNull(dps_dvs);

        String delServer = deliveryWS.getBaseFileUrl(dps_dvs);
        assert !DashboardHelper.isNull(delServer);

        String ieMetsXml = deliveryWS.getExtendedIEByDVS(dps_dvs, 0);
        System.out.println(ieMetsXml);
        assert !DashboardHelper.isNull(ieMetsXml);

        // Parse the IE Mets xml to retrieve the file path for the given file ID
        IEModel ieObj = new DOMBasedIEMetaDataParser().parseIEMetadata(dps_pid, ieMetsXml);
        assert !DashboardHelper.isNull(ieObj);

        Map<String, RepresentationModel> repModels = ieObj.getRepresentations();
        assert !DashboardHelper.isNull(repModels);

        List<MediaFile> mediaFiles = new ArrayList<>();

        // Retrieve the File Model from the PM RepModel
        for (Map.Entry<String, RepresentationModel> repModel : repModels.entrySet()) {
            RepresentationModel repStaff = repModel.getValue();

            Map<String, FileModel> fileModels = repStaff.getFiles();
            // For each file model, check if the file pid matches to get the file location
            for (Map.Entry<String, FileModel> fileModel : fileModels.entrySet()) {
                FileModel fmStaff = fileModel.getValue();
                if (fmStaff != null) {

                    // Check the file mime-type and assign the files to the play list
                    // as there may be a mix of file types in the given REP/IE Ex. pdf, word icons etc
                    String mimeTypeStaff = "";
                    if (fmStaff.getMimeType() != null) {
                        mimeTypeStaff = fmStaff.getMimeType().toLowerCase();
                    }

                    if (mimeTypeStaff.equals("image/jpeg")) {
                        // Set the cover image attribute to the file PID
                        String coverImage = "http://" + fmStaff.getId() + "&dps_func=stream";

                        // FILTER AND ADD MEDIA FILES BASED ON THE MIME-TYPE ONLY
                        // File types: AUDIO: mp3,ogg,m4a,f4a | VIDEO: mp4,m4v,f4v,flv,webm
                        // Reference: http://support.jwplayer.com/customer/portal/articles/1403635-media-format-reference
                    } else if ((mimeTypeStaff.equals("video/mp4")) || (mimeTypeStaff.equals("application/mp4,video/mp4")) ||
                            (mimeTypeStaff.equals("video/mpeg")) || (mimeTypeStaff.equals("video/webm"))
                            || (mimeTypeStaff.equals("video/flv")) || (mimeTypeStaff.equals("video/x-flv")) ||
                            (mimeTypeStaff.equals("audio/mp4")) || (mimeTypeStaff.equals("audio/mpeg")) ||
                            (mimeTypeStaff.equals("audio/mp3")) || (mimeTypeStaff.equals("audio/ogg"))) {

                        // Add the media files for the play list
                        if (dps_pid.startsWith("IE")) {
                            // Add all the files for IE Viewer ie, if IEPID
                            mediaFiles.add(new MediaFile(fmStaff.getId(), fmStaff.getLabel(), fmStaff.getFileSequenceNumber(), fmStaff.getFileExtension().toLowerCase()));
                        } else {
                            // Add only the requested file for FILE viewer ie. if FILEPID
                            if (fmStaff.getId().equals(dps_pid)) {
                                mediaFiles.add(new MediaFile(fmStaff.getId(), fmStaff.getLabel(), fmStaff.getFileSequenceNumber(), fmStaff.getFileExtension().toLowerCase()));
                            }
                        }
                    }

                } //  END OF IF NULL FILE MODEL CHECK

            } // END OF FILE MODELS ITERATION FOR LOOP
        }

    }

    private static String generateDPSSession(String pid, String pdsHandle) throws IOException, InterruptedException {
        String deliveryUrl = String.format("https://slbpdstest.natlib.govt.nz/goto/logon/https://ndhadelivertest.natlib.govt.nz/delivery/DeliveryManagerServlet?dps_pid=%s&pds_handle=%s&calling_system=del&institute=", pid, pdsHandle);

        ConcurrentHashMap<String, List<String>> cookieHeaders = new ConcurrentHashMap<>();
        CookieHandler cookieHandler = new DeliveryCookieHandler(cookieHeaders);
        List<String> cookies = new ArrayList<>();
        cookies.add("PDS_HANDLE=" + pdsHandle);
        cookies.add("PDSILB03=wlguatrosiapp02");
        cookieHeaders.put("Cookie", cookies);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(deliveryUrl))
                .timeout(Duration.ofSeconds(30))
//                .POST(HttpRequest.BodyPublishers.ofString(loginString))
                .GET()
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(cookieHandler)
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        httpResponse.headers().map().forEach((k, v) -> {
            System.out.println(k + ": " + String.join(",", v));
        });

        String rosettaDeliveryResponse = httpResponse.body();
        if (rosettaDeliveryResponse == null || rosettaDeliveryResponse.trim().isEmpty()) {
            return null;
        }
        System.out.println(rosettaDeliveryResponse);

        int iFrameStartPosition = rosettaDeliveryResponse.indexOf("<iframe");
        int iFrameEndPosition = rosettaDeliveryResponse.indexOf("</iframe>");

        if (iFrameStartPosition > 0 && iFrameEndPosition > 0) {
            rosettaDeliveryResponse = rosettaDeliveryResponse.substring(iFrameStartPosition, iFrameEndPosition);
        }

        int parameterNamePosition = rosettaDeliveryResponse.indexOf("dps_dvs=");
        if (parameterNamePosition < 0) {
            return null;
        }

        String remainingString = rosettaDeliveryResponse.substring(parameterNamePosition);
        String[] parameterPairs = remainingString.split("[&]");
        if (parameterPairs == null || parameterPairs.length <= 0) {
            return null;
        }

        String parameterNameValuePair = parameterPairs[0];
        return (parameterNameValuePair == null ? null : parameterNameValuePair.replace("dps_dvs=", ""));
    }

//    @Test
    @Ignore
    public void testCMS2() throws XmlException, IOException {
        String url = "http://wlgortimuweb01.natlib.govt.nz:8080/sru?";
        boolean isEmu = true;
        boolean depositsExist = false;
        SruService sruService = new SruServiceImpl();

        SruRequest sruRequest = new SruRequestImpl();
        sruRequest.setUrl(url);
        sruRequest.setStartRecord(1);
        sruRequest.setMaximumRecords(10);
        sruRequest.setSchema("dps");

        Criteria firstCriteria = new SingleCriteria(SearchAttribute.IRN, "IRN", "44375"); //OK
        Criteria secondCriteria = new SingleCriteria(SearchAttribute.TI, "TI", "images"); //OK
//        Criteria secondCriteria = new SingleCriteria(SearchAttribute.Title, "TI", "images"); //OK
//        Criteria secondCriteria = new SingleCriteria(SearchAttribute.AlmaRecID, "mms_id", "455"); //Error
        Criteria criteria = new CompositeCriteria(Operator.and, firstCriteria, secondCriteria);

        SruQueryBuilderVisitorImpl queryBuilder = new SruQueryBuilderVisitorImpl();
        criteria.accept(queryBuilder);
        sruRequest.setQuery(queryBuilder);

        DcToHtmlTransformer transformer = new DcToHtmlTransformerImpl();

        sruRequest.setIsInternal(false);
        sruRequest.setUser("a");
        sruRequest.setPassword("b");
//        String xmlResult = sruService.execute(sruRequest);
        byte[] buf = sruService.executeReturnAsByteArray(sruRequest, 1000);
        String xmlResult = new String(buf);
        System.out.println(xmlResult);

//        QueryResults results = transformer.parseResults(xmlResult);
//        if (results.getNoOfRecords() > 0) {
//            depositsExist = true;
//        }
    }

//    @Test
    @Ignore
    public void testCms1() throws XmlException, IOException {
        //String url = "http://natlib-primo.hosted.exlibrisgroup.com/primo_library/libweb/action/dlSearch.do?vid=NLNZ&institution=64NLNZ&search_scope=NLNZ&indx=1&bulkSize=10&query=any,exact,";
//        String url = "https://wlguatdpsilb.natlib.govt.nz/delivery/sru?";
        String url = "https://ap01-psb.alma.exlibrisgroup.com/view/sru/64NLNZ_MAIN?";
        boolean isEmu = true;
        boolean depositsExist = false;
        SruService sruService = new SruServiceImpl();

        SruRequest sruRequest = new SruRequestImpl();
        sruRequest.setUrl(url);
        sruRequest.setStartRecord(1);
        sruRequest.setMaximumRecords(10);
        sruRequest.setSchema("dc");

        Criteria firstCriteria = new SingleCriteria(SearchAttribute.IRN, "IRN", "44375"); //OK
//        Criteria secondCriteria = new SingleCriteria(SearchAttribute.TI, "TI", "images"); //OK
//        Criteria secondCriteria = new SingleCriteria(SearchAttribute.Title, "Title", "images"); //OK
        Criteria secondCriteria = new SingleCriteria(SearchAttribute.AlmaRecID, "mms_id", "455"); //Error
        Criteria criteria = new CompositeCriteria(Operator.and, firstCriteria, secondCriteria);

        SruQueryBuilderVisitorImpl queryBuilder = new SruQueryBuilderVisitorImpl();
        secondCriteria.accept(queryBuilder);
        sruRequest.setQuery(queryBuilder);

        DcToHtmlTransformer transformer = new DcToHtmlTransformerImpl();

        sruRequest.setIsInternal(true);
        sruRequest.setUser("leefr");
        sruRequest.setPassword("******");
//        String xmlResult = sruService.execute(sruRequest);
        byte[] buf = sruService.executeReturnAsByteArray(sruRequest, 1000);
        String xmlResult = new String(buf);
        System.out.println(xmlResult);
    }
}
