package cn.xie.imchat.utils;

import org.jivesoftware.smack.packet.ExtensionElement;

/**
 * message扩展类
 * 扩展前：<message from='发送方jid' to='接收方jid' type='消息类型(普通消息/群聊)'>
 *          <body>消息内容</body>
 *        </message>
 * 扩展后：<message id='76Ws9-11' from='发送方jid' to='接收方jid' type='消息类型(普通消息/群聊)'>
 *          <body>hello 你好</body>
 *          <date>2019-10-11 10:05</date>
 *        </message>
 * @author xiejinbo
 * @date 2019/10/11 0011 10:39
 */
public class AddMessageDate implements ExtensionElement {

    //消息时间元素名称
    public static final String Element_DATE = "date";
    //消息时间值(对外开放)
    private String dateText;

    public String getDateText() {
        return dateText;
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public String getElementName() {
        return Element_DATE;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(Element_DATE).append(">");
        sb.append(dateText);
        sb.append("</"+Element_DATE+">");
        return sb.toString();
    }
}
