package nz.govt.natlib.dashboard.common.exception;

public class WebServiceException extends Exception {
    public WebServiceException(String s) {
        super(s);
    }

    public WebServiceException(Exception e) {
        super(e);
    }
}
