package nz.govt.natlib.dashboard.common.metadata;

public class ResultOfDeposit {
    private final boolean isSuccessful;
    private final String theResultMessage;
    private final String theSipID;

    public static ResultOfDeposit create(boolean success, String sipID, String resultMessage) {
        return new ResultOfDeposit(success, sipID, resultMessage);
    }

    public ResultOfDeposit(boolean success, String sipID, String resultMessage) {
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
