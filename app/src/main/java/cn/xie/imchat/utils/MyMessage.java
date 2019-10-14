package cn.xie.imchat.utils;

import java.io.Serializable;

/**
 * @author xiejinbo
 * @date 2019/10/11 0011 17:11
 */
public class MyMessage implements Serializable {
    private static final long serialVersionUID = -2790672475566638165L;
    public Object data;
    public String type;
    public String sendtime;
    public String source;

}
