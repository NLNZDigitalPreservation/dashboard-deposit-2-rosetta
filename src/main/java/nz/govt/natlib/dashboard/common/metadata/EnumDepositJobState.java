package nz.govt.natlib.dashboard.common.metadata;

public enum EnumDepositJobState {
    INITIALED,
    RUNNING, //Submit and accept by Rosetta
    PAUSED,//Paused, but can be retried or resumed
    SUCCEED,//Successfully finished the stage
    FAILED,//Completely failed and can not be retried
    CANCELED,//Stopped manually
}
