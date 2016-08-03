package com.ucc.addrbook;

import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/2/16.
 */
public class Response {
    private int errcode=0;
    private String errmsg="";
    private Object data=null;
//    private Map objs = new HashMap();
    public Response(int code,String msg,Object ob)
    {
        errcode = code;
        errmsg = msg;
        data = ob;
    }

    public int getErrcode()
    {
        return errcode;
    }
    public void setErrcode(int code)
    {
        errcode = code;
    }
    public String getErrmsg()
    {
        return errmsg;
    }
    public void setErrmsg(String msg)
    {
        errmsg = msg;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object ob)
    {
        data = ob;
    }


//    public Map getObjs() { return objs; }
//    public void setObjs(Map v) { objs =v; }
}
