package com.ucc.addrbook;

import org.apache.log4j.Logger;
import org.jasig.cas.client.util.AssertionHolder;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchScope;
import org.ldaptive.beans.reflect.DefaultLdapEntryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

import javax.naming.directory.DirContext;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by Administrator on 2016/5/4.
 */
//@RequestMapping("/v1/corps")
@RestController
public class GroupControllerV1 {

    @Autowired
    private  BookServiceV1 bookServiceV1;
    private static final Logger logger = Logger.getLogger(GroupControllerV1.class);
    /**
     * 新建Group
     * @param group
     * @param corpid
     * @param parentid
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/v1/corps/{corpid:\\d+}/groups/{parentid:\\d+}",method = RequestMethod.POST)
    public Response addGroup(@RequestBody GroupEntry group, @PathVariable("corpid") Long corpid,
                             @PathVariable("parentid") Long parentid,
                             HttpServletResponse response) {
        logger.debug("addGroup Enter");
        response.setHeader("Access-Control-Allow-Origin","*");

        group.setCorpId(corpid);
        group.setParentId(parentid);
        Response rep;
        boolean checkname=true;
        if(ToolUntil.CheckRole("admin", corpid.toString(), AssertionHolder.getAssertion())) {
            ErrorCode error = bookServiceV1.addGroup(group);
            rep = new Response(error.getCode(),error.getMsg(),group);
        }else
        {
            rep = new Response(ErrorCode.AuthFail.getCode(),ErrorCode.AuthFail.getMsg(),"");
        }
        logger.debug("addGroup Leave");
        return rep;
    }

    /**
     * 删除指定Group
     * @param corpid
     * @param groupid
     * @return
     */
    @RequestMapping(value="/v1/corps/{corpid:\\d+}/groups/{groupid:\\d+}",method = RequestMethod.DELETE)
    public Response delGroup(@PathVariable("corpid") Long corpid,@PathVariable("groupid") Long groupid,
                             HttpServletResponse response) {
        logger.debug("delGroup enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        Response rep = null;
        if(ToolUntil.CheckRole("admin", corpid.toString(),AssertionHolder.getAssertion())) {

            ErrorCode error = bookServiceV1.delGroup(groupid);
            rep = new Response(error.getCode(),error.getMsg(),null);
        }else
        {
            rep =  new Response(ErrorCode.AuthFail.getCode(),ErrorCode.AuthFail.getMsg(),"");
        }
        logger.debug("delGroup leave");
        return rep;
    }

    /**
     * 更新Group
     * @param group
     * @param corpid
     * @param groupid
     * @return
     */
    @RequestMapping(value="/v1/corps/{corpid:\\d+}/groups/{groupid:\\d+}",method = RequestMethod.PUT)
    public Response updateGroup(@RequestBody GroupEntry group,@PathVariable("corpid") Long corpid,
                                @PathVariable("groupid") Long groupid,
                                @RequestParam(value = "op", required = true, defaultValue = "add") String op,
                                HttpServletResponse response) {
        logger.debug("updateGroup Enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        Response rep;
        if(ToolUntil.CheckRole("admin", corpid.toString(),AssertionHolder.getAssertion())) {
            group.setCorpId(corpid);
            group.setDeptId(groupid);
            ErrorCode error = bookServiceV1.updateGroup(group,op);
            rep = new Response(error.getCode(), error.getMsg(), group);
        }else
        {
            rep = new Response(ErrorCode.AuthFail.getCode(),ErrorCode.AuthFail.getMsg(),"");
        }
        logger.debug("updateGroup Leave");
        return rep;
    }

    /**
     * 获取Group信息
     * @param corpid
     * @param groupid
     * @return
     */
    @RequestMapping(value="/v1/corps/{corpid:\\d+}/groups/{groupid:\\d+}",method = RequestMethod.GET)
    public Response getGroup(@PathVariable("corpid") Long corpid,
                             @PathVariable("groupid") Long groupid,
                             @RequestParam(value = "fullname", required = false, defaultValue = "") String fullname,
                             HttpServletResponse response)
    {
        logger.debug("getGroup Enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        Response rep = null;
        if(ToolUntil.CheckRole("normal", corpid.toString(),AssertionHolder.getAssertion()))
        {
            GroupEntry group=null;
            //通过定位部门
            if(groupid!=0l)
            {
                LdapEntry entry = bookServiceV1.getEntry(groupid);
                if(entry!=null)
                {
                    DefaultLdapEntryMapper<GroupEntry> mapper= new DefaultLdapEntryMapper<GroupEntry>();
                    group = new GroupEntry();
                    mapper.map(entry,group);
                }
            }else if(!fullname.isEmpty())
            {
                //通过部门全名定位部门
                String dn = ToolUntil.fullName2dn(fullname,bookServiceV1.getDnFromUid(corpid));
                group = (GroupEntry) bookServiceV1.getEntry(dn);
            }
            if(group==null)
            {
                rep = new Response(ErrorCode.NOTFOUD.getCode(),ErrorCode.NOTFOUD.getMsg(),null);
            }else
            {
                String parentdn = ToolUntil.dn2Parentdn(group.getDn());
                if(parentdn.startsWith("ou=groups"))
                {
                    //顶级部门的父部门id设为0
                    group.setParentId(0L);
                }else
                {
                    group.setParentId(bookServiceV1.getUidFormDn(parentdn));
                }
                group.setCorpId(corpid);
                Map tmp = bookServiceV1.queryGroupEx(corpid,group.getDeptId(),null,SearchScope.ONELEVEL,1,1);
                if((Long)tmp.get("total")>0)
                {
                    group.setIsleaf(false);
                }else
                {
                    group.setIsleaf(true);
                }

                Map<String,Object> users = bookServiceV1.queryGroupMembersEx(group.getDeptId(),null,null,null,1,1);
                if(users!=null)
                {
                    group.setUcount((Long)users.get("total"));
                }else
                {
                    group.setUcount(0l);
                }

                String rootdn = ToolUntil.dn2Rootdn(group.getDn());
                group.setRootDeptId(bookServiceV1.getUidFormDn(rootdn));
                group.setFullname(ToolUntil.dn2Fullname(group.getDn(),false));
                rep = new Response(ErrorCode.OK.getCode(),ErrorCode.OK.getMsg(),group);
            }

        }else
        {
            rep = new Response(ErrorCode.AuthFail.getCode(),ErrorCode.AuthFail.getMsg(),null);
        }
        logger.debug("getGroup Leave");
        return rep;
    }

    /**
     * 获取Group信息
     * @param corpid
     * @param groupid
     * @return
     */
    @RequestMapping(value="/internal/v1/corps/{corpid:\\d+}/groups/{groupid:\\d+}",method = RequestMethod.GET)
    public Response unauthgetGroup(@PathVariable("corpid") Long corpid,
                             @PathVariable("groupid") Long groupid,
                             @RequestParam(value = "fullname", required = false, defaultValue = "") String fullname,
                             HttpServletResponse response)
    {
        logger.debug("getGroup Enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        Response rep = null;

        GroupEntry group=null;
        //通过定位部门
        if(groupid!=0l)
        {
            LdapEntry entry = bookServiceV1.getEntry(groupid);
            if(entry!=null)
            {
                DefaultLdapEntryMapper<GroupEntry> mapper= new DefaultLdapEntryMapper<GroupEntry>();
                group = new GroupEntry();
                mapper.map(entry,group);
            }
        }else if(!fullname.isEmpty())
        {
            //通过部门全名定位部门
            String dn = ToolUntil.fullName2dn(fullname,bookServiceV1.getDnFromUid(corpid));
            group = (GroupEntry) bookServiceV1.getEntry(dn);
        }
        if(group==null)
        {
            rep = new Response(ErrorCode.NOTFOUD.getCode(),ErrorCode.NOTFOUD.getMsg(),null);
        }else
        {

            String parentdn = ToolUntil.dn2Parentdn(group.getDn());
            if(parentdn.startsWith("ou=groups"))
            {
                //顶级部门的父部门id设为0
                group.setParentId(0L);
            }else
            {
                group.setParentId(bookServiceV1.getUidFormDn(parentdn));
            }
            group.setCorpId(corpid);
            Map tmp = bookServiceV1.queryGroupEx(corpid,group.getDeptId(),null,SearchScope.ONELEVEL,1,1);
            if((Long)tmp.get("total")>0)
            {
                group.setIsleaf(false);
            }else
            {
                group.setIsleaf(true);
            }
            String rootdn = ToolUntil.dn2Rootdn(group.getDn());
            group.setRootDeptId(bookServiceV1.getUidFormDn(rootdn));
            group.setFullname(ToolUntil.dn2Fullname(group.getDn(),false));

            Map<String,Object> users = bookServiceV1.queryGroupMembersEx(group.getDeptId(),null,null,null,1,1);
            if(users!=null)
            {
                group.setUcount((Long)users.get("total"));
            }else
            {
                group.setUcount(0l);
            }
            rep = new Response(ErrorCode.OK.getCode(),ErrorCode.OK.getMsg(),group);
        }
        logger.debug("getGroup Leave");
        return rep;
    }

    /**
     *
     * @param basedn 企业dn
     * @param deptNames 部门名称列表
     * @return
     */
    private List<NodeFullName> pareDeptName(String basedn,List<String> deptNames)
    {
        List<NodeFullName> tmp = new ArrayList<NodeFullName>();
        for (String strName : deptNames) {
            Long gid = bookServiceV1.getUidFormDn(ToolUntil.fullName2dn(strName,basedn));
            if(gid!=null)
            {
                tmp.add(new NodeFullName(gid,strName));
            }
        }
        Collections.sort(tmp);
        return tmp.size()>0?tmp:null;
    }

    /**
     * 部门id列表转fullname列表
     * @param deptIds
     * @return
     */
    private List<NodeFullName> pareDeptId(List<String> deptIds)
    {
        List<NodeFullName> tmp = new ArrayList<NodeFullName>();
        for (String strgid : deptIds) {
            Long gid = Long.parseLong(strgid);
            String name = bookServiceV1.getDeptFullName(gid,false);
            if(name!=null)
            {
                tmp.add(new NodeFullName(gid,name));
            }else
            {

            }

        }
        Collections.sort(tmp);
        return tmp.size()>0?tmp:null;
    }
    /**
     * 获取group的成员
     * @param mtype
     *         "user" 获取group的user列表不包括subgroup中的user
     *         "subgrp" 获取group的subgroup列表
     *         "alluser" 获取group的user列表包括subgroup中的user
     *         "tree"    获取组织架构
     * @param corpid
     * @param groupid
     * @param page
     * @param pagesize
     * @return
     */
    @RequestMapping(value="/v1/corps/{corpid:\\d+}/groups/{groupid:\\d+}/members",method = RequestMethod.GET)
    public @ResponseBody  Object getMembers(@RequestParam(value = "mtype", required = true, defaultValue = "alluser") String mtype,
                                            @RequestParam(value = "page", required = true, defaultValue = "1") int page,
                                            @RequestParam(value = "pagesize", required = true, defaultValue = "10") int pagesize,
                                            @PathVariable("corpid") Long corpid,@PathVariable("groupid") Long groupid,
                                            @RequestParam(value = "callback", required = false, defaultValue = "") String callback,
                                            HttpServletResponse response ) {
        logger.debug("getMembers Enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        Object ret=null;
        if(ToolUntil.CheckRole("normal", corpid.toString(),AssertionHolder.getAssertion())) {
            Map data=null;

            if (mtype.equals("user") ) {
                if(groupid==0l)
                {
                    //未分组用户
                    FilterItem filter = new FilterItem();
                    filter.setOp("&");
                    filter.getFilters().add(filter.new Item("o",corpid.toString(),"equal"));
                    FilterItem filter2 = new FilterItem();
                    filter2.setOp("!");
                    filter2.getFilters().add(filter2.new Item("departmentNumber","*","equal"));
                    filter.getFilters().add(filter2);
                    data = bookServiceV1.queryUser(filter,null,page,pagesize,null);
                }else
                {
                    data = bookServiceV1.queryGroupMembersEx(groupid,null,null, SearchScope.OBJECT,page,pagesize);
                }
                if(data!=null)
                {
                    data.put("users",data.get("recodes"));
                    data.remove("recodes");
                    List<UserEntry> li = (List<UserEntry>)data.get("users");
                    if(li!=null && li.size()>0)
                    {
                        for (UserEntry e:li
                                ) {
                            if(e.getDeptIds()!=null)
                            {
                                e.setDepts(pareDeptId(e.getDeptIds()));
                            }

                        }
                    }
                }

            } else if (mtype.equals("subgrp")) {
                data = bookServiceV1.queryGroupEx(corpid,groupid,null,SearchScope.ONELEVEL,page,pagesize);
                if(data!=null)
                {
                    fillGroup(corpid,groupid,data);
                    data.put("groups",data.get("recodes"));
                    data.remove("recodes");
                }

            } else if (mtype.equals("alluser")) {
                if(groupid==0l)
                {
                    //企业下所有用户
                    FilterItem filter = new FilterItem();
                    filter.setOp("&");
                    filter.getFilters().add(filter.new Item("o",corpid.toString(),"equal"));
                    data = bookServiceV1.queryUser(filter,null,page,pagesize,null);
                }else
                {
                    data = bookServiceV1.queryGroupsAllMemebers(corpid,groupid,null,page,pagesize);
                }
                if(data!=null)
                {
                    data.put("users",data.get("recodes"));
                    data.remove("recodes");
                    List<UserEntry> li = (List<UserEntry>)data.get("users");
                    if(li!=null && li.size()>0)
                    {
                        for (UserEntry e:li
                                ) {
                            if(e.getDeptIds()!=null)
                            {
                                e.setDepts(pareDeptId(e.getDeptIds()));
                            }

                        }
                    }
                }
            } else if(mtype.equals("tree") )
            {
                data = new HashMap<String,Object>();
                Map groups = bookServiceV1.queryGroupEx(corpid,groupid,null,SearchScope.SUBTREE,1,0);
                if(groups!=null)
                {
                    fillGroup(corpid,groupid,groups);
                    data.put("groups",groups.get("recodes"));
                    data.put("dtotal",groups.get("total"));
                }
                Map users = null;
                if(groupid==0l)
                {
                    //企业下所有用户
                    FilterItem filter = new FilterItem();
                    filter.setOp("&");
                    filter.getFilters().add(filter.new Item("o",corpid.toString(),"equal"));
                    users = bookServiceV1.queryUser(filter,null,page,pagesize,null);
                }else
                {
                    users = bookServiceV1.queryGroupsAllMemebers(corpid,groupid,null,1,0);
                }
                if(users!=null)
                {
                    data.put("users",users.get("recodes"));
                    data.put("utotal",users.get("total"));
                    List<UserEntry> li = (List<UserEntry>)data.get("users");
                    if(li!=null && li.size()>0)
                    {
                        for (UserEntry e:li
                                ) {
                            if(e.getDeptIds()!=null)
                            {
                                e.setDepts(pareDeptId(e.getDeptIds()));
                            }
                        }
                    }
                }

                //data = (Map)bookDao.getGrpAllSubgrp(corpid,groupid);
            }else if(mtype.equals("allsubgrp"))
            {
                data = bookServiceV1.queryGroupEx(corpid,groupid,null,SearchScope.SUBTREE,page,pagesize);
                if(data!=null)
                {
                    fillGroup(corpid,groupid,data);
                    data.put("groups",data.get("recodes"));
                    data.remove("recodes");
                }
            }
            else {
                ret =  new Response(ErrorCode.INVALIDARG.getCode(), ErrorCode.INVALIDARG.getMsg(), null);
            }
            if(data!=null)
            {
                data.put("mtype",mtype);
            }
            ret = new Response(ErrorCode.OK.getCode(), ErrorCode.OK.getMsg(), data);
        }else
        {
            ret= new Response(ErrorCode.AuthFail.getCode(),ErrorCode.AuthFail.getMsg(),null);
        }
        if(!callback.equals(""))
        {
            MappingJacksonValue tmp = new MappingJacksonValue(ret);
            tmp.setJsonpFunction(callback);
            ret = tmp;
        }
        logger.debug("getMembers Leave");
        return ret;
    }

    /**
     * 获取group的成员
     * @param mtype
     *         "user" 获取group的user列表不包括subgroup中的user
     *         "subgrp" 获取group的subgroup列表
     *         "alluser" 获取group的user列表包括subgroup中的user
     *         "tree"    获取组织架构
     * @param corpid
     * @param groupid
     * @param page
     * @param pagesize
     * @return
     */
    @RequestMapping(value="/internal/v1/corps/{corpid:\\d+}/groups/{groupid:\\d+}/members",method = RequestMethod.GET)
    public @ResponseBody  Object unauthgetMembers(@RequestParam(value = "mtype", required = true, defaultValue = "alluser") String mtype,
                                            @RequestParam(value = "page", required = true, defaultValue = "1") int page,
                                            @RequestParam(value = "pagesize", required = true, defaultValue = "10") int pagesize,
                                            @PathVariable("corpid") Long corpid,@PathVariable("groupid") Long groupid,
                                            @RequestParam(value = "callback", required = false, defaultValue = "") String callback,
                                            HttpServletResponse response ) {
        logger.debug("getMembers Enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        Object ret=null;
        Map data=null;

        if (mtype.equals("user") ) {
            if(groupid==0l)
            {
                //未分组用户
                FilterItem filter = new FilterItem();
                filter.setOp("&");
                filter.getFilters().add(filter.new Item("o",corpid.toString(),"equal"));
                FilterItem filter2 = new FilterItem();
                filter2.setOp("!");
                filter2.getFilters().add(filter2.new Item("departmentNumber","*","equal"));
                filter.getFilters().add(filter2);
                data = bookServiceV1.queryUser(filter,null,page,pagesize,null);
            }else
            {
                data = bookServiceV1.queryGroupMembersEx(groupid,null,null, SearchScope.OBJECT,page,pagesize);
            }
            if(data!=null)
            {
                data.put("users",data.get("recodes"));
                data.remove("recodes");
                List<UserEntry> li = (List<UserEntry>)data.get("users");
                if(li!=null && li.size()>0)
                {
                    for (UserEntry e:li
                            ) {
                        if(e.getDeptIds()!=null)
                        {
                            e.setDepts(pareDeptId(e.getDeptIds()));
                        }

                    }
                }
            }

        } else if (mtype.equals("subgrp")) {
            data = bookServiceV1.queryGroupEx(corpid,groupid,null,SearchScope.ONELEVEL,page,pagesize);
            if(data!=null)
            {
                fillGroup(corpid,groupid,data);
                data.put("groups",data.get("recodes"));
                data.remove("recodes");
            }

        } else if (mtype.equals("alluser")) {
            if(groupid==0l)
            {
                //企业下所有用户
                FilterItem filter = new FilterItem();
                filter.setOp("&");
                filter.getFilters().add(filter.new Item("o",corpid.toString(),"equal"));
                data = bookServiceV1.queryUser(filter,null,page,pagesize,null);
            }else
            {
                data = bookServiceV1.queryGroupsAllMemebers(corpid,groupid,null,page,pagesize);
            }
            if(data!=null)
            {
                data.put("users",data.get("recodes"));
                data.remove("recodes");
                List<UserEntry> li = (List<UserEntry>)data.get("users");
                if(li!=null && li.size()>0)
                {
                    for (UserEntry e:li
                            ) {
                        if(e.getDeptIds()!=null)
                        {
                            e.setDepts(pareDeptId(e.getDeptIds()));
                        }

                    }
                }
            }
        } else if(mtype.equals("tree") )
        {
            data = new HashMap<String,Object>();
            Map groups = bookServiceV1.queryGroupEx(corpid,groupid,null,SearchScope.SUBTREE,1,0);
            if(groups!=null)
            {
                fillGroup(corpid,groupid,groups);
                data.put("groups",groups.get("recodes"));
                data.put("dtotal",groups.get("total"));
            }
            Map users = null;
            if(groupid==0l)
            {
                //企业下所有用户
                FilterItem filter = new FilterItem();
                filter.setOp("&");
                filter.getFilters().add(filter.new Item("o",corpid.toString(),"equal"));
                users = bookServiceV1.queryUser(filter,null,page,pagesize,null);
            }else
            {
                users = bookServiceV1.queryGroupsAllMemebers(corpid,groupid,null,1,0);
            }
            if(users!=null)
            {
                data.put("users",users.get("recodes"));
                data.put("utotal",users.get("total"));
                List<UserEntry> li = (List<UserEntry>)data.get("users");
                if(li!=null && li.size()>0)
                {
                    for (UserEntry e:li
                            ) {
                        if(e.getDeptIds()!=null)
                        {
                            e.setDepts(pareDeptId(e.getDeptIds()));
                        }
                    }
                }
            }

            //data = (Map)bookDao.getGrpAllSubgrp(corpid,groupid);
        }else if(mtype.equals("allsubgrp"))
        {
            data = bookServiceV1.queryGroupEx(corpid,groupid,null,SearchScope.SUBTREE,page,pagesize);
            if(data!=null)
            {
                fillGroup(corpid,groupid,data);
                data.put("groups",data.get("recodes"));
                data.remove("recodes");
            }
        }
        else {
            ret =  new Response(ErrorCode.INVALIDARG.getCode(), ErrorCode.INVALIDARG.getMsg(), null);
        }
        if(data!=null)
        {
            data.put("mtype",mtype);
        }
        ret = new Response(ErrorCode.OK.getCode(), ErrorCode.OK.getMsg(), data);

        if(!callback.equals(""))
        {
            MappingJacksonValue tmp = new MappingJacksonValue(ret);
            tmp.setJsonpFunction(callback);
            ret = tmp;
        }
        logger.debug("getMembers Leave");
        return ret;
    }

    private void fillGroup(Long corpid ,Long parentid,Map groups)
    {
        //判断是否为叶子部门
        for (GroupEntry e :
                (List<GroupEntry>)groups.get("recodes")) {
            e.setParentId(parentid);
            e.setCorpId(corpid);
            Map tmp = bookServiceV1.queryGroupEx(corpid,e.getDeptId(),null,SearchScope.ONELEVEL,1,1);
            if((Long)tmp.get("total")>0)
            {
                e.setIsleaf(false);
            }else
            {
                e.setIsleaf(true);
            }
            String rootdn = ToolUntil.dn2Rootdn(e.getDn());
            e.setRootDeptId(bookServiceV1.getUidFormDn(rootdn));
            e.setFullname(ToolUntil.dn2Fullname(e.getDn(),false));
        }
    }
}

