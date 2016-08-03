package com.ucc.addrbook;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.ldaptive.beans.Attribute;
import org.ldaptive.beans.Entry;

/**
 * Created by Administrator on 2016/5/3.
 */
@Entry(
        dn = "dn",
        attributes = {
                @Attribute(name = "uid", property = "corpId"),
                @Attribute(name = "description", property = "name"),
                @Attribute(name = "o", property = "domain"),
                @Attribute(name = "st", property = "pingyin"),
                @Attribute(name = "l", property = "isactive"),
                @Attribute(name = "objectClass", values = {"top","uidObject","organization"})
        })
public class CorpEntry {
    @JsonIgnore
    private String dn;                        /* dn */
    private Long corpId;                     /* 全局唯一id */
    private String name;                     /* 公司名称 */
    @JsonIgnore
    private String pingyin;                  /* 名称拼音 */
    private String domain;                   /* 公司域名 */
    private UserEntry admin;                 /* 企业管理员 */
    private String isactive;                 /* 企业是否可用 */

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public Long getCorpId() {
        return corpId;
    }

    public void setCorpId(Long corpId) {
        this.corpId = corpId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPingyin() {
        return pingyin;
    }

    public void setPingyin(String pingyin) {
        this.pingyin = pingyin;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public UserEntry getAdmin() {
        return admin;
    }

    public void setAdmin(UserEntry admin) {
        this.admin = admin;
    }

    public String getIsactive() {
        return isactive;
    }

    public void setIsactive(String isactive) {
        this.isactive = isactive;
    }
}
