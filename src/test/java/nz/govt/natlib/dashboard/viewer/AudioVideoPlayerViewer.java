package nz.govt.natlib.dashboard.viewer;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import nz.govt.natlib.ndha.rosettaIEMetaDataParser.DOMBasedIEMetaDataParser;
import nz.govt.natlib.ndha.rosettaIEMetaDataParser.FileModel;
import nz.govt.natlib.ndha.rosettaIEMetaDataParser.IEMetaDataParseException;
import nz.govt.natlib.ndha.rosettaIEMetaDataParser.IEModel;
import nz.govt.natlib.ndha.rosettaIEMetaDataParser.PreservationType;
import nz.govt.natlib.ndha.rosettaIEMetaDataParser.RepresentationModel;

import org.apache.commons.io.IOUtils;

import com.exlibris.dps.DeliveryAccessWS;
import com.exlibris.dps.DeliveryAccessWS_Service;
import com.exlibris.dps.Exception_Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AudioVideoPlayerViewer is a web based application that acts as a Rosetta viewer
 * to handle the audio or video files from Rosetta which are not included to be
 * played with the wowza media viewer (streamingmediaviewer). This viewer will
 * use JWPlayer as the default player and will replace the stand-alone
 * MP3Viewer  VideoViewer within Rosetta.
 * <p>
 * The viewer will receive the request with the IE PID or FILE PID and will process
 * the request accordingly and redirect to the jsp page with the JWPlayer to play the
 * requested media element. The class also processes the mets.xml response to retrieve
 * the media files for the play list.
 * <p>
 * Enabled the STAFF MODE check and feature to handle the play list/file. The staff mode
 * is detected if the PDS_HANDLE cookie is set. Added this feature on 18.Jan.2016
 *
 * @author Mathachan Kulathinal
 * @since 20.Apr.2015 Enhancements: 02.Nov.2017 - Mathachan Kulathinal
 * - Upgraded jwplayer to version 7.12.10
 * - Added 'application/mp4,video/mp4' to the media mime type list
 * - Added feature to check for remote IP address from http request
 * and redirect the request to use old verison of jwplayer (v6.12.4956)
 */
public class AudioVideoPlayerViewer extends HttpServlet {

    private static final long serialVersionUID = -5267529130308614006L;
    private static Logger logger = LoggerFactory.getLogger(AudioVideoPlayerViewer.class);

    // Representation Codes for FILES within IEs
    private static final String REP_CODE_HIGH = "HIGH";
    private static final String REP_CODE_MEDIUM = "MEDIUM";
    private static final String REP_CODE_LOW = "LOW";

    //	private static final String NDHA_DELIVERY_HOSTNAME = "http://hokai.natlib.govt.nz:1801"; // DEV
    private static final String NDHA_DELIVERY_HOSTNAME = "http://ndhadelivertest.natlib.govt.nz"; // UAT
//    private static final String NDHA_DELIVERY_HOSTNAME = "http://ndhadeliver.natlib.govt.nz"; // PROD

    // The Rosetta Web Services is available on localhost as this viewer is deployed
    // on the Rosetta Delivery Servers.
//    private static final String NDHA_WEB_SERVICES_HOSTNAME = "localhost:1801";

    // Delivery Web Services wsdl URL to initialize the web service method calls
//    private String DELIVERY_WS_WSDL_URL = "https://wlguatdpsilb.natlib.govt.nz/dpsws/delivery/DeliveryAccessWS?wsdl";
    //    private String DELIVERY_WS_WSDL_URL = "http://wlguatrosiapp01.natlib.govt.nz:1801/dpsws/delivery/DeliveryAccessWS?wsdl";
    // Delivery Servlet URL for viewers (Rosetta) - used for generating the dps_dvs session
    private String DELIVERY_WS_WSDL_URL;
    private static final String DELIVERY_VIEWER_URL = "https://wlguatdpsilb.natlib.govt.nz/dpsws/delivery/DeliveryManagerServlet?dps_pid=";

    private static final String URL_SEPARATOR = "|";

    public String getDELIVERY_WS_WSDL_URL() {
        return DELIVERY_WS_WSDL_URL;
    }

    public void setDELIVERY_WS_WSDL_URL(String DELIVERY_WS_WSDL_URL) {
        this.DELIVERY_WS_WSDL_URL = DELIVERY_WS_WSDL_URL;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String dps_pid = null;
        String dps_dvs = null;
        String dps_staff = null;

        String redirect = "audio_video_player.jsp?files=";
        List<MediaFile> mediaFiles = new ArrayList<MediaFile>();

        if (request.getParameter("dps_pid") != null) {
            dps_pid = request.getParameter("dps_pid");
        }
        if (request.getParameter("dps_dvs") != null) {
            dps_dvs = request.getParameter("dps_dvs");
        }

        // Ensure the DPS PID and DPS SESSION is not null
        if ((dps_pid != null) && (!(dps_pid.isEmpty()))) {

            DeliveryAccessWS deliveryWS = null;
            String ieMetsXml = null;
            String delServer = "";
            String coverImage = "";

            try {
                // Initialize the Web Service call
                deliveryWS = new DeliveryAccessWS_Service(new URL(DELIVERY_WS_WSDL_URL), new QName("http://dps.exlibris.com/", "DeliveryAccessWS")).getDeliveryAccessWSPort();

                if (deliveryWS != null) {

                    logger.info("Rosetta Delivery Web Service initialized for pid:[" + dps_pid + "]");

                    if ((dps_dvs == null) || (dps_dvs.isEmpty())) {
                        dps_dvs = generateDPSSession(dps_pid);
                        logger.info("New Rosetta Delivery session:[" + dps_dvs + "] generated for pid:[" + dps_pid + "]");

                    } else {
                        logger.info("Rosetta Delivery session:[" + dps_dvs + "] received for pid:[" + dps_pid + "]");
                    }

                    delServer = deliveryWS.getBaseFileUrl(dps_dvs);
                    logger.info("Delivery Server URL:[" + delServer + "]");

                    // Retrieve the rosetta IE METS xml to retrieve the file path
                    ieMetsXml = deliveryWS.getExtendedIEByDVS(dps_dvs, 0);

//                    logger.debug("Received rosetta IE METS xml:\r\n" + ieMetsXml + "\r\n");
                    logger.debug("Received rosetta IE METS xml, length={}", ieMetsXml.length());

                    // Parse the IE Mets xml to retrieve the file path for the given file ID
                    IEModel ieObj = new DOMBasedIEMetaDataParser().parseIEMetadata(dps_pid, ieMetsXml);

                    // Retrieve the file path based on the file pid from the IE/REP/FILEGRP
                    if (ieObj != null) {
                        // Retrieve all the Representations
                        Map<String, RepresentationModel> repModels = ieObj.getRepresentations();
                        if (repModels != null) {

                            // Check if STAFF MODE
                            // If staff mode, then the PDS HANDLE cookie should be available
                            // Handle the request if it is from within the staff mode
                            Cookie[] reqCookies = request.getCookies();
                            for (Cookie eachCookie : reqCookies) {
                                logger.info("Cookies received: " + eachCookie.getName() + "=" + eachCookie.getValue());
                                if ("PDS_HANDLE".equalsIgnoreCase(eachCookie.getName())) {
                                    dps_staff = eachCookie.getValue();
                                    break; //No need to look further. STAFF MODE detected.
                                }
                            }

                            if (dps_staff != null) {

                                logger.info("STAFF MODE detected for requested PID:[" + dps_pid + "] with PDS HANDLE:[" + dps_staff + "].");

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
                                                coverImage = NDHA_DELIVERY_HOSTNAME + DELIVERY_VIEWER_URL + fmStaff.getId() + "&dps_func=stream";
                                                logger.info("STAFF MODE - Added cover image [" + coverImage + "] for FILE ID:[" + fmStaff.getId() + "]");

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
                                                    logger.info("STAFF MODE - Added media file with FILE ID:[" + fmStaff.getId() + "]");

                                                } else {
                                                    // Add only the requested file for FILE viewer ie. if FILEPID
                                                    if (fmStaff.getId().equals(dps_pid)) {
                                                        mediaFiles.add(new MediaFile(fmStaff.getId(), fmStaff.getLabel(), fmStaff.getFileSequenceNumber(), fmStaff.getFileExtension().toLowerCase()));
                                                        logger.info("STAFF MODE - Added media file with FILE ID:[" + fmStaff.getId() + "]");
                                                    }
                                                }
                                            }

                                        } //  END OF IF NULL FILE MODEL CHECK

                                    } // END OF FILE MODELS ITERATION FOR LOOP
                                }

                            } else {

                                // Loop through all the Representations and check if DERIVATIVE_COPY is present
                                boolean ie_has_only_PM = true; // boolean flag to control the presence of DC
                                for (Map.Entry<String, RepresentationModel> repModel : repModels.entrySet()) {
                                    RepresentationModel rep = repModel.getValue();
                                    // Check if AC is present in the reps
                                    if (rep.getPreservationType().equals(PreservationType.AC)) {
                                        logger.info("Access Copy found for PID:[" + dps_pid + "]");
                                        ie_has_only_PM = false;
                                    }
                                }

                                // If IE has only PRESERVATION_MASTER rep, then PM will be used & PM won’t have a RepresentationCcode
                                if (ie_has_only_PM) {

                                    logger.info("Only Preservation Master (PM) for PID:[" + dps_pid + "] available. There is no Access Copy rep for this item.");

                                    // Retrieve the File Model from the PM RepModel
                                    for (Map.Entry<String, RepresentationModel> repModel : repModels.entrySet()) {
                                        RepresentationModel rep1 = repModel.getValue();

                                        Map<String, FileModel> fileModels = rep1.getFiles();
                                        // For each file model, check if the file pid matches to get the file location
                                        for (Map.Entry<String, FileModel> fileModel : fileModels.entrySet()) {
                                            FileModel fm1 = fileModel.getValue();
                                            if (fm1 != null) {

                                                // Check the file mime-type and assign the files to the play list
                                                // as there may be a mix of file types in the given REP/IE Ex. pdf, word icons etc
                                                String mimeType1 = "";
                                                if (fm1.getMimeType() != null) {
                                                    mimeType1 = fm1.getMimeType().toLowerCase();
                                                }

                                                if (mimeType1.equals("image/jpeg")) {
                                                    // Set the cover image attribute to the file PID
                                                    coverImage = NDHA_DELIVERY_HOSTNAME + DELIVERY_VIEWER_URL + fm1.getId() + "&dps_func=stream";
                                                    logger.info("PM ONLY - Added cover image [" + coverImage + "] for FILE ID:[" + fm1.getId() + "]");

                                                    // FILTER AND ADD MEDIA FILES BASED ON THE MIME-TYPE ONLY
                                                    // File types: AUDIO: mp3,ogg,m4a,f4a | VIDEO: mp4,m4v,f4v,flv,webm
                                                    // Reference: http://support.jwplayer.com/customer/portal/articles/1403635-media-format-reference
                                                } else if ((mimeType1.equals("video/mp4")) || (mimeType1.equals("application/mp4,video/mp4")) ||
                                                        (mimeType1.equals("video/mpeg")) || (mimeType1.equals("video/webm")) ||
                                                        (mimeType1.equals("video/flv")) || (mimeType1.equals("video/x-flv")) ||
                                                        (mimeType1.equals("audio/mp4")) || (mimeType1.equals("audio/mpeg")) ||
                                                        (mimeType1.equals("audio/mp3")) || (mimeType1.equals("audio/ogg"))) {

                                                    // Add the media files for the play list
                                                    if (dps_pid.startsWith("IE")) {
                                                        // Add all the files for IE Viewer ie, if IEPID
                                                        mediaFiles.add(new MediaFile(fm1.getId(), fm1.getLabel(), fm1.getFileSequenceNumber(), fm1.getFileExtension().toLowerCase()));
                                                        logger.info("PM ONLY - Added media file with FILE ID:[" + fm1.getId() + "]");

                                                    } else {
                                                        // Add only the requested file for FILE viewer ie. if FILEPID
                                                        if (fm1.getId().equals(dps_pid)) {
                                                            mediaFiles.add(new MediaFile(fm1.getId(), fm1.getLabel(), fm1.getFileSequenceNumber(), fm1.getFileExtension().toLowerCase()));
                                                            logger.info("PM ONLY - Added media file with FILE ID:[" + fm1.getId() + "]");
                                                        }
                                                    }
                                                }

                                            } //  END OF IF NULL FILE MODEL CHECK

                                        } // END OF FILE MODELS ITERATION FOR LOOP
                                    }


                                } else {

                                    // This IE has AC, so check the RepCodes and retrieve the play list based on the best RepCode
                                    boolean medium_rep_code_found = false;

                                    // For each representation, retrieve the File Model
                                    for (Map.Entry<String, RepresentationModel> repModel : repModels.entrySet()) {
                                        RepresentationModel rep2 = repModel.getValue();

                                        // Check if the representation code is available. REPCODE is set only for access copies
                                        String repCode = rep2.getRepresentationCode();
                                        if (repCode != null) {

                                            // If RepCode=HIGH|MEDIUM, clear any existing values and add the new values
                                            if ((repCode.equalsIgnoreCase(REP_CODE_HIGH)) ||
                                                    (repCode.equalsIgnoreCase(REP_CODE_MEDIUM))) {
                                                if (mediaFiles.size() > 0) {
                                                    mediaFiles.clear();
                                                }
                                            } else if (repCode.equalsIgnoreCase(REP_CODE_LOW)) {
                                                if ((medium_rep_code_found) && (mediaFiles.size() > 0)) {
                                                    // No need to get this file list as we already have the medium file list
                                                    break;
                                                }
                                            }

                                            Map<String, FileModel> fileModels = rep2.getFiles();
                                            // For each file model, check if the file pid matches to get the file location
                                            for (Map.Entry<String, FileModel> fileModel : fileModels.entrySet()) {
                                                FileModel fm2 = fileModel.getValue();
                                                if (fm2 != null) {

                                                    // Check the file mime-type and assign the files to the play list
                                                    // as there may be a mix of file types in the given REP/IE Ex. pdf, word icons etc
                                                    String mimeType2 = "";
                                                    if (fm2.getMimeType() != null) {
                                                        mimeType2 = fm2.getMimeType().toLowerCase();
                                                    }

                                                    if (mimeType2.equals("image/jpeg")) {
                                                        // Set the cover image attribute to the file PID
                                                        coverImage = NDHA_DELIVERY_HOSTNAME + DELIVERY_VIEWER_URL + fm2.getId() + "&dps_func=stream";

                                                        // FILTER AND ADD MEDIA FILES BASED ON THE MIME-TYPE ONLY
                                                        // File types: AUDIO: mp3,ogg,m4a,f4a | VIDEO: mp4,m4v,f4v,flv,webm
                                                        // Reference: http://support.jwplayer.com/customer/portal/articles/1403635-media-format-reference
                                                    } else if ((mimeType2.equals("video/mp4")) || (mimeType2.equals("application/mp4,video/mp4")) ||
                                                            (mimeType2.equals("video/mpeg")) || (mimeType2.equals("video/webm")) ||
                                                            (mimeType2.equals("video/flv")) || (mimeType2.equals("video/x-flv")) ||
                                                            (mimeType2.equals("audio/mp4")) || (mimeType2.equals("audio/mpeg")) ||
                                                            (mimeType2.equals("audio/mp3")) || (mimeType2.equals("audio/ogg"))) {

                                                        // Add the media files for the play list
                                                        if (dps_pid.startsWith("IE")) {
                                                            // Add all the files for IE Viewer ie, if IEPID
                                                            mediaFiles.add(new MediaFile(fm2.getId(), fm2.getLabel(), fm2.getFileSequenceNumber(), fm2.getFileExtension().toLowerCase()));
                                                            logger.info("Added media file with FILE ID:[" + fm2.getId() + "]");

                                                        } else {
                                                            // Add only the requested file for FILE viewer ie. if FILEPID
                                                            if (fm2.getId().equals(dps_pid)) {
                                                                mediaFiles.add(new MediaFile(fm2.getId(), fm2.getLabel(), fm2.getFileSequenceNumber(), fm2.getFileExtension().toLowerCase()));
                                                                logger.info("Added media file with FILE ID:[" + fm2.getId() + "]");
                                                            }
                                                        }
                                                    }


                                                } //  END OF IF NULL FILE MODEL CHECK

                                            } // END OF FILE MODELS ITERATION FOR LOOP

                                            if (repCode.equalsIgnoreCase(REP_CODE_HIGH)) {
                                                // If RepCode=HIGH, check if we have got the media files
                                                // If the file list is ready, then exit the for loop - no need to look further
                                                if (mediaFiles.size() > 0) {
                                                    break;
                                                }
                                            } else if (repCode.equalsIgnoreCase(REP_CODE_MEDIUM)) {
                                                // Set the value that the
                                                medium_rep_code_found = true;
                                            }

                                        } // END OF REPRESENTATION CODE NULL CHECK

                                    } // END OF REPRESENTATIONS ITERATION FOR LOOP

                                } // END OF IF CHECK TO SEE IF PM ONLY OR NOT?

                            } // END OF REPRESENTATIONS NOT NULL IF LOOP

                        } // END OF IF STAFF USER CHECK?

                    } // END OF IF IE OBJECT LOOP

                } // END OF IF deliveryWS NULL CHECK

            } catch (IEMetaDataParseException pex) {
                logger.error("Error occurred during the IE Metadata Parse call to retrieve the rosetta file list. " + pex.getMessage());
                pex.printStackTrace();
            } catch (Exception_Exception ex) {
                logger.error("Error occurred during the web service call to retrieve the rosetta file list. " + ex.getMessage());
                ex.printStackTrace();
                throw new IOException(ex);
            } catch (Exception ex) {
                logger.error("Error: " + ex.getMessage());
                ex.printStackTrace();
            }

            if (mediaFiles.size() > 1) {
                // No need to call the sort method unnecessarily. Call only if more than 1 element in the collection.
                Collections.sort(mediaFiles, MediaFile.MediaFilesBySequenceNumber);
            }

            // Check if the request is from a SearchStation. If yes, then set it to use older version of jwplayer6
            if (verifyRemoteIPAddressFromSearchStation(request)) {
                redirect = "audio_video_player6.jsp?files=";
                logger.info("Using the OLD JWPlayer (v6.12.4956) as the request has been detected to be from a Search Station.");
            }

            for (MediaFile mf : mediaFiles) {
                // Certain File Types are not accepted by JWPlayer, so we need to re-map those
                // to file types that are handled by JWPlayer
                // mp4 = .m4v,.f4v, aac = .m4a,.f4a
                String file_ext = mf.getFileType();
                if ((file_ext.equals("m4v")) || (file_ext.equals("f4v"))) {
                    file_ext = "mp4";
                } else if ((file_ext.equals("m4a")) || (file_ext.equals("f4a"))) {
                    file_ext = "aac";
                }
                // FIX: Issue with '&' in Title. URL Encode the Title - MJK:02.03.2016
                // Some items sent from Archives do not have Label, fixed it using empty string
                redirect += mf.getPid() + "," + file_ext + "," +
                        URLEncoder.encode((mf.getLabel() != null) ? mf.getLabel() : "", "UTF-8") + URL_SEPARATOR;
            }

            logger.info("Set the cover image:[" + coverImage + "] as an attribute for JSP - pid:[" + dps_pid + "]");
            // Set the cover image attribute
            request.setAttribute("coverImg", coverImage);

            logger.info("Set the delivery server URL: " + delServer + " as an attribute for JSP - pid:[" + dps_pid + "]");
            // Set the delivery server URL as an attribute for passing the value to JSP
            request.setAttribute("delServer", delServer);

        } else {
            // DPS PID or DPS SESSION IS MISSING. REPORT ERROR.
            redirect = "error.jsp";
        }
        logger.info("Forwarding the request to JSP page: " + redirect);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(redirect);
        requestDispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    /*
     * Method to retrieve and check the remote IP Address for the searchstation check
     */
    private static boolean verifyRemoteIPAddressFromSearchStation(HttpServletRequest httpReq) {
        // First IP is for the UAT - Test Search Station @ MailRoom/DockwayArea on LG
        // IP ranges in the reading room for Search Stations
        // UAT Test Station - 192.168.240.79
        // level 100 - 192.168.240.11-192.168.240.158
        // level 200 - 192.168.240.161-192.168.240.190
        // level 300 - 192.168.240.193-192.168.240.222
        // level 400 - 192.168.240.225-192.168.240.254
        String allowed_IPs = "192.168.240.11-192.168.240.158,192.168.240.161-192.168.240.190,192.168.240.193-192.168.240.222,192.168.240.225-192.168.240.254";
        String remote_IP = "";
        String ipAddresses = httpReq.getHeader("X-Forwarded-For");
        logger.info("verifyRemoteIPAddressFromSearchStation(). HTTP Request Header (X-Forwarded-For): " + ipAddresses);
        if ((ipAddresses == null) || (ipAddresses.isEmpty())) {
            remote_IP = httpReq.getRemoteAddr();
        } else {
            if (ipAddresses.indexOf(",") > 0) {
                String[] ips = ipAddresses.split(",");
                remote_IP = ips[0];
            } else {
                remote_IP = ipAddresses;
            }
        }

        logger.info("verifyRemoteIPAddressFromSearchStation(). Remote IP address received: " + remote_IP);
        // Check if the remote_IP is not empty or not null.
        if ((remote_IP == null) || (remote_IP.isEmpty())) {
            //If empty, return false
        } else {
            long rem_ip = ipv4ToLong(remote_IP);
            String[] ipArr = allowed_IPs.split(",");

            for (int i = 0; i < ipArr.length; i++) {
                // Check if this is a range or single IP
                if (ipArr[i].indexOf("-") > 0) {
                    // IP RANGE DETECTED - Split values
                    String[] ipRangeArr = ipArr[i].split("-");
                    if (ipRangeArr.length == 2) {
                        // Compare the lower and upper limits
                        if ((rem_ip >= ipv4ToLong(ipRangeArr[0])) && (rem_ip <= ipv4ToLong(ipRangeArr[1]))) {
                            return true;
                        }
                    }

                } else {
                    // SINGLE IP ADDRESS
                    if (ipv4ToLong(ipArr[i]) == rem_ip) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*
     * Convert a v4 IP Address to the corresponding numeric value
     *
     * @param addr IP Address to be converted @return num
     */
    private static long ipv4ToLong(String addr) {
        String[] addrArray = addr.split("\\.");
        long num = 0;
        if (addrArray.length == 4) { // Ensure that it is a valid IP String
            for (int i = 0; i < addrArray.length; i++) {
                int power = 3 - i;
                num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256, power)));
            }
        }
        return num;
    }

    /*
     * Method to generate the dps_dvs Rosetta session value for a give PID if the session is not present
     */
    private static String generateDPSSession(String pid) {
        String deliveryUrl = DELIVERY_VIEWER_URL + pid;

        logger.info("Rosetta DeliveryURL created: " + deliveryUrl);

        try {
            String rosettaDeliveryResponse = "";
            URL url = new URL(deliveryUrl);
            URLConnection urlConn = url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            InputStream is = urlConn.getInputStream();

            String char_encoding = urlConn.getContentEncoding();
            if ((char_encoding != null) && (char_encoding.equals("gzip"))) {
                rosettaDeliveryResponse = IOUtils.toString(new GZIPInputStream(is));
            } else {
                rosettaDeliveryResponse = IOUtils.toString(is);
            }

            if (rosettaDeliveryResponse == null || rosettaDeliveryResponse.trim().isEmpty()) {
                return null;

            } else {

                int iFrameStartPosition = rosettaDeliveryResponse.indexOf("<iframe");
                int iFrameEndPosition = rosettaDeliveryResponse.indexOf("</iframe>");

                if (iFrameStartPosition > 0 && iFrameEndPosition > 0) {
                    rosettaDeliveryResponse = rosettaDeliveryResponse.substring(iFrameStartPosition, iFrameEndPosition);
                }

                int parameterNamePosition = rosettaDeliveryResponse.indexOf("dps_dvs=");
                if (parameterNamePosition < 0)
                    return null;

                String remainingString = rosettaDeliveryResponse.substring(parameterNamePosition);
                String[] parameterPairs = remainingString.split("[&]");
                if (parameterPairs == null || parameterPairs.length <= 0)
                    return null;

                String parameterNameValuePair = parameterPairs[0];
                return (parameterNameValuePair == null ? null : parameterNameValuePair.replace("dps_dvs=", ""));
            }

        } catch (Exception ex) {
            logger.error("Error occurred while trying to generate a rosetta session. " + ex.getMessage());
            return null;
        }
    }
}
