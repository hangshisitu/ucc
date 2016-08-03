package com.ucc.addrbook;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.ldaptive.beans.Attribute;
import org.ldaptive.beans.Entry;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2016/4/28.
 */
@Entry(
        dn = "dn",
        attributes = {
                @Attribute(name = "uid", property = "uid"),
                @Attribute(name = "o", property = "corpId"),
                @Attribute(name = "departmentNumber", property = "deptIds"),
                @Attribute(name = "labeledURI", property = "userId"),
                @Attribute(name = "cn", property = "userId"),
                @Attribute(name = "sn", property = "userId"),
                @Attribute(name = "userPassword", property = "password"),
                @Attribute(name = "displayName", property = "name"),
                @Attribute(name = "givenName", property = "pingyin"),
                @Attribute(name = "mail", property = "email"),
                @Attribute(name = "mobile", property = "mobile"),
                @Attribute(name = "employeeType", property = "role"),
                @Attribute(name = "objectClass", values = {"top","person","organizationalPerson", "inetOrgPerson"})
        })
public class UserEntry
{
    public static interface BaseUserEntry{}
    public static interface  InputUserEntry extends BaseUserEntry{}
    public static interface  OutputUserEntry extends BaseUserEntry{}

    @JsonIgnore
    private String dn;
    private Long uid;
    private Long corpId;
    @JsonIgnore
    private String domain;
    private List<String> deptIds;
    private List<NodeFullName> depts;
    private String userId;
    private String password;
    private String name;
    @JsonIgnore
    private String pingyin;
    private String email;
    private String mobile;
    private String role;

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getCorpId() {
        return corpId;
    }

    public void setCorpId(Long corpId) {
        this.corpId = corpId;
    }

    public List<String> getDeptIds() {
        return deptIds;
    }

    public void setDeptIds(List<String> deptIds) {
        this.deptIds = deptIds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<NodeFullName> getDepts() {
        return depts;
    }

    public void setDepts(List<NodeFullName> depts) {
        this.depts = depts;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
