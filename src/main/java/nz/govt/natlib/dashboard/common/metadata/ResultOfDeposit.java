package nz.govt.natlib.dashboard.common.metadata;

public class ResultOfDeposit {
    private final boolean isSuccessful;
    private final String theResultMessage;
    private final String theSipID;

    public static ResultOfDeposit create(boolean success, String resultMessage, String sipID) {
        return new ResultOfDeposit(success, resultMessage, sipID);
    }

    public ResultOfDeposit(boolean success, String resultMessage, String sipID) {
        this.isSuccessful = success;
        this.theResultMessage = resultMessage;
        this.theSipID = sipID;
    }

    public boolean isSuccess() {
        return this.isSuccessful;
    }

    public String getResultMessage() {
        return this.theResultMessage;
    }

    public String getSipID() {
        return this.theSipID;
    }
}
