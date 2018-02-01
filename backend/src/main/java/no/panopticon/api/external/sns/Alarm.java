package no.panopticon.api.external.sns;


public class Alarm {

    public String AlarmName;
    public String AlarmDescription;
    public String AWSAccountId;
    public String NewStateValue;
    public String NewStateReason;
    public String StateChangeTime;
    public String Region;
    public String OldStateValue;
    public Trigger Trigger;

    @Override
    public String toString() {
        return "Alarm{" +
                "AlarmName='" + AlarmName + '\'' +
                ", AlarmDescription='" + AlarmDescription + '\'' +
                ", AWSAccountId='" + AWSAccountId + '\'' +
                ", NewStateValue='" + NewStateValue + '\'' +
                ", NewStateReason='" + NewStateReason + '\'' +
                ", StateChangeTime='" + StateChangeTime + '\'' +
                ", Region='" + Region + '\'' +
                ", OldStateValue='" + OldStateValue + '\'' +
                ", Trigger=" + Trigger +
                '}';
    }
}
