package no.panopticon.api.external.sns;

public class SnsMessage {

    public String Type;
    public String MessageId;
    public String TopicArn;
    public String Subject;
    public String Message;
    public String SignatureVersion;
    public String Signature;
    public String SigningCertURL;
    public String SubscribeURL;
    public String UnsubscribeURL;

    @Override
    public String toString() {
        return "SnsMessage{" +
                "Type='" + Type + '\'' +
                ", MessageId='" + MessageId + '\'' +
                ", TopicArn='" + TopicArn + '\'' +
                ", Subject='" + Subject + '\'' +
                ", Message='" + Message + '\'' +
                ", SignatureVersion='" + SignatureVersion + '\'' +
                ", Signature='" + Signature + '\'' +
                ", SigningCertURL='" + SigningCertURL + '\'' +
                ", SubscribeURL='" + SubscribeURL + '\'' +
                ", UnsubscribeURL=" + UnsubscribeURL +
                '}';
    }
}
