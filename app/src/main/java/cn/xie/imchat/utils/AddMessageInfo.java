package cn.xie.imchat.utils;

import org.jivesoftware.smack.packet.ExtensionElement;

/**
 * message扩展类
 * 扩展前：<message from='发送方jid' to='接收方jid' type='消息类型(普通消息/群聊)'>
 *          <body>消息内容</body>
 *        </message>
 * 扩展后：<message id='76Ws9-11' from='发送方jid' to='接收方jid' type='消息类型(普通消息/群聊)'>
 *          <body>hello 你好</body>
 *          <userinfo xmlns="com.xml.extension">
 *              <nickname>菜鸟</nickname>
 *              <icon>http://www.liaoku.org/</url>
 *          </userinfo>
 *        </message>
 * @author xiejinbo
 * @date 2019/10/11 0011 10:01
 */
public class AddMessageInfo implements ExtensionElement {
    public static final String NAME_SPACE = "com.xml.extension";
    //用户信息元素名称
    public static final String ELEMENT_NAME = "userinfo";

    //用户昵称元素名称
    private String nickNameElement = "nickname";
    //用户昵称值(对外开放)
    private String nickNameText = "";

    //用户头像元素名称
    private String iconElement = "icon";
    //用户头像值(对外开放)
    private String iconText = "";

    public String getNickNameText() {
        return nickNameText;
    }

    public void setNickNameText(String nickNameText) {
        this.nickNameText = nickNameText;
    }

    public String getIconText() {
        return iconText;
    }

    public void setIconText(String iconText) {
        this.iconText = iconText;
    }

    @Override
    public String getNamespace() {
        return NAME_SPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public CharSequence toXML(String enclosingNamespace) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(ELEMENT_NAME).append(" xmlns=\"").append(NAME_SPACE).append("\">");
        sb.append("<" + nickNameElement + ">").append(nickNameText).append("</"+nickNameElement+">");
        sb.append("<" + iconElement + ">").append(iconText).append("</"+iconElement+">");
        sb.append("</"+ELEMENT_NAME+">");
        return sb.toString();
    }
}
