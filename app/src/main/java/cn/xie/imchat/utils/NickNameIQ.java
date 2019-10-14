package cn.xie.imchat.utils;

import org.jivesoftware.smack.packet.IQ;

/**
 * @author xiejinbo
 * @date 2019/7/30 0030 15:39
 */
public class NickNameIQ extends IQ {

    private String jid;
    private String nickName;

    public NickNameIQ(String childElementName, String childElementNamespace, String jid, String nickName) {
        super(childElementName, childElementNamespace);
        this.jid = jid;
        this.nickName = nickName;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<item jid=\'"+jid+"\' name=\'"+nickName+"\'>"+"</item>");
        return xml;
    }
}
