package com.ucc.addrbook;

/**
 * Created by Administrator on 2016/2/17.
 */
public enum ErrorCode {
    OK(0,"ok"),INVALIDARG(400,"param is invalid"),AuthFail(401,"authentication failed"),
    NOTFOUD(404,"data not find"),DEPTDUPLUSER(409," The user is in the department"),INTELERR(500,"internal error"),DUPLICATE(501,"data already exists");
    private int code;
    private String msg;
    private ErrorCode(int code,String msg)
    {
        this.code = code;
        this.msg = msg;
    }

    public int getCode()
    {
        return code;
    }
    public String getMsg()
    {
        return msg;
    }

}
