package no.panopticon.api.external;

import com.google.gson.annotations.SerializedName;

public class SnsMessage {

    public String MessageId;
    public String Type;
    public String SignatureVersion;
    public String TopicArn;
    public String Subject;
    public String Message;
    public String SubscribeURL;
    public String SigningCertURL;
    public String UnsubscribeURL;

    @Override
    public String toString() {
        return "SnsMessage{" +
                "MessageId='" + MessageId + '\'' +
                ", Type='" + Type + '\'' +
                ", SignatureVersion='" + SignatureVersion + '\'' +
                ", TopicArn='" + TopicArn + '\'' +
                ", Subject='" + Subject + '\'' +
                ", Message='" + Message + '\'' +
                ", SubscribeURL='" + SubscribeURL + '\'' +
                ", SubscribeURL='" + SigningCertURL + '\'' +
                ", SigningCertURL=" + UnsubscribeURL +
                '}';
    }
}
