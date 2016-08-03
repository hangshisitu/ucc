package com.ucc.addrbook;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.ldaptive.beans.Attribute;
import org.ldaptive.beans.Entry;

import java.util.*;

/**
 * Created by Administrator on 2016/5/3.
 */
@Entry(
        dn = "dn",
        attributes = {
                @Attribute(name = "uid", property = "deptId"),
                @Attribute(name = "cn", property = "name"),
                @Attribute(name = "description", property = "pingyin"),
                @Attribute(name = "member", property = "member"),
                @Attribute(name = "objectClass", values = {"top","groupOfNames","uidObject"})
        })
public class GroupEntry {
    @JsonIgnore
    private String dn;                                 /* dn */
    private Long deptId;                              /* 全局唯一id */
    private Long corpId;                              /* 所在公司id */
    private String name;                              /* 组名 */
    @JsonIgnore
    private String pingyin;                          /* 名字拼音 */
    private List<String> member;                     /* 成员数组 */
    private Long parentId;                           /* 父组 */
    private Long rootDeptId;                           /* 根部门id */
    private String fullname;                         /* 部门全名 */
    private boolean isleaf;                         /* 是否为叶子部门（没有子部门的部门） */
    private Long ucount;

    public Long getUcount() {
        return ucount;
    }

    public void setUcount(Long ucount) {
        this.ucount = ucount;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
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

    public List<String> getMember() {
        return member;
    }

    public void setMember(List<String> member) {
        this.member = member;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getRootDeptId() {
        return rootDeptId;
    }

    public void setRootDeptId(Long rootDeptId) {
        this.rootDeptId = rootDeptId;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public boolean isleaf() {
        return isleaf;
    }

    public void setIsleaf(boolean isleaf) {
        this.isleaf = isleaf;
    }
}
