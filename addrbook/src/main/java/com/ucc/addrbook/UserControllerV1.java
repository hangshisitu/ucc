package com.ucc.addrbook;

import org.apache.log4j.Logger;
import org.jasig.cas.client.util.AssertionHolder;
import org.ldaptive.control.SortKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by Administrator on 2016/5/4.
 */

@RestController
public class UserControllerV1 {

    @Autowired
    private BookServiceV1 bookServiceV1;
    private static final Logger logger = Logger.getLogger(UserControllerV1.class);

    /**
     * 添加用户
     * @param users  用户信息
     * @param corpid 用户所在公司id
     * @return
     */
    @RequestMapping(value="/internal/v1/corps/{corpid:\\d+}/users",method = RequestMethod.POST)
    public @ResponseBody Response unAuthAddUser(@RequestBody List<UserEntry> users,@PathVariable("corpid") Long corpid,
                                                @RequestParam(value = "deptidtype", required = false, defaultValue = "name") String deptidtype,
                                                HttpServletResponse response) {
        logger.debug("unAuthAddUser Enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        List<UserEntry> ret= new ArrayList<UserEntry>();
        Response rep = null;
        if(!checkInputParam(users))
        {
            return new Response(ErrorCode.INVALIDARG.getCode(), ErrorCode.INVALIDARG.getMsg(), ret);
        }
        String baseDn=bookServiceV1.getDnFromUid(corpid);
        for (UserEntry e : users
                ) {
            //将用户dn设置为所属企业的dn
            e.setDn(baseDn);
            e.setCorpId(corpid);
            e.setRole("normal");
            if(e.getPassword()!=null && !e.getPassword().isEmpty())
            {
                e.setPassword(ToolUntil.EncodePwd(e.getPassword()));
            }
            if (e.getDeptIds()!=null) {
                //将部门id转成部门全名
                if(deptidtype.equals("id"))
                {
                    e.setDepts(pareDeptId(e.getDeptIds()));
                }else{
                    e.setDepts(pareDeptName(baseDn,e));
                }
            }
            if(bookServiceV1.addUser(e) == ErrorCode.OK)
            {
                ret.add(e);
            }
        }
        if(ret.size() == users.size())
        {
            rep = new Response(ErrorCode.OK.getCode(), ErrorCode.OK.getMsg(), ret);
        }else
        {
            rep = new Response(ErrorCode.DUPLICATE.getCode(), ErrorCode.DUPLICATE.getMsg(), ret);
        }

        logger.debug("unAuthAddUser Leave");
        return rep;
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
     *
     * @param basedn 企业dn
     * @param user 用户
     * @return
     */
    private List<NodeFullName> pareDeptName(String basedn,UserEntry user)
    {
        List<NodeFullName> tmp = new ArrayList<NodeFullName>();
        List<String> deptids = new ArrayList<String>();
        if(user !=null && user.getDeptIds()!=null)
        {
            for (String strName : user.getDeptIds()) {
                Long gid = bookServiceV1.getUidFormDn(ToolUntil.fullName2dn(strName,basedn));
                if(gid!=null)
                {
                    tmp.add(new NodeFullName(gid,strName));
                    deptids.add(gid.toString());
                }
            }
            if(deptids.size()>0)
            {
                user.setDeptIds(deptids);
            }
        }

        Collections.sort(tmp);
        return tmp.size()>0?tmp:null;
    }

    private boolean checkInputParam(List<UserEntry> users)
    {
        for (UserEntry u: users
             ) {
            if(u.getName()!=null && u.getName().isEmpty())
            {
                u.setName(null);
            }
            if(u.getMobile()!=null && u.getMobile().isEmpty())
            {
                u.setMobile(null);
            }
        }
        return true;
    }
    /**
     * 添加用户
     * @param users 用户信息
     * @return
     */
    @RequestMapping(value="/v1/corps/{corpid:\\d+}/users",method = RequestMethod.POST)
    public @ResponseBody Response addUserEx(@RequestBody List<UserEntry> users,@PathVariable("corpid") Long corpid,
                                            @RequestParam(value = "deptidtype", required = false, defaultValue = "name") String deptidtype,
                                            HttpServletResponse response) {
        logger.debug("addUserEx Enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        List<UserEntry> ret= new ArrayList<UserEntry>();
        Response rep = null;
        if(!checkInputParam(users))
        {
            return new Response(ErrorCode.INVALIDARG.getCode(), ErrorCode.INVALIDARG.getMsg(), ret);
        }
        if(ToolUntil.CheckRole("admin",corpid.toString(),AssertionHolder.getAssertion())) {
            String baseDn=bookServiceV1.getDnFromUid(corpid);
            for (UserEntry e : users
                    ) {
                //将用户dn设置为所属企业的dn
                e.setDn(baseDn);
                e.setCorpId(corpid);
                e.setRole("normal");
                if(e.getPassword()!=null && !e.getPassword().isEmpty())
                {
                    e.setPassword(ToolUntil.EncodePwd(e.getPassword()));
                }
                if (e.getDeptIds()!=null) {
                    //将部门id转成部门全名
                    if(deptidtype.equals("id"))
                    {
                        e.setDepts(pareDeptId(e.getDeptIds()));
                    }else{
                        e.setDepts(pareDeptName(baseDn,e));
                    }
                }
                if(bookServiceV1.addUser(e) == ErrorCode.OK)
                {
                    ret.add(e);
                }
            }
            if(ret.size() == users.size())
            {
                rep = new Response(ErrorCode.OK.getCode(), ErrorCode.OK.getMsg(), ret);
            }else
            {
                rep = new Response(ErrorCode.DUPLICATE.getCode(), ErrorCode.DUPLICATE.getMsg(), ret);
            }

        }else
        {
            rep = new Response(ErrorCode.AuthFail.getCode(), ErrorCode.AuthFail.getMsg(), "");
        }

        logger.debug("addUserEx Leave");
        return rep;
    }


//    /**
//     * 删除指定用户
//     * @param corpid 用户所在公司id
//     * @param uid 用户id
//     * @return
//     */
//    @RequestMapping(value="/v1/corps/{corpid:\\d+}/users/{uid:\\d+}",method = RequestMethod.DELETE)
//    public @ResponseBody Response delUser(@PathVariable("corpid") Long corpid,
//                                          @PathVariable("uid") Long uid,
//                                          HttpServletResponse response)
//    {
//        logger.debug("delUser Enter");
//        response.setHeader("Access-Control-Allow-Origin","*");
//        Response rep = null;
//        Map attrs = AssertionHolder.getAssertion().getPrincipal().getAttributes();
//        Long cuid = Long.parseLong(attrs.get("uid").toString());
//        if(uid.longValue()==cuid.longValue())
//        {
//            logger.info("Auth Fail");
//            rep = new Response(ErrorCode.AuthFail.getCode(),ErrorCode.AuthFail.getMsg(),"");
//        }
//        if(ToolUntil.CheckRole("admin",corpid.toString(), AssertionHolder.getAssertion())) {
//            ErrorCode error = bookServiceV1.delUser(uid);
//            rep = new Response(error.getCode(), error.getMsg(), null);
//        }else
//        {
//            logger.info("Auth Fail");
//            rep = new Response(ErrorCode.AuthFail.getCode(),ErrorCode.AuthFail.getMsg(),"");
//        }
//        logger.debug("delUser Leave");
//        return rep;
//    }

    /**
     * 批量删除用户
     * @param Users 用户id
     * @return
     */
    @RequestMapping(value="/v1/corps/{corpid:\\d+}/users",method = RequestMethod.DELETE)
    public @ResponseBody Response delUsers(@RequestBody List<UserEntry> Users,
                                           @PathVariable("corpid") Long corpid,
                                           HttpServletResponse response)
    {
        logger.debug("delUsers Enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        Response rep = null;
        Map attrs = AssertionHolder.getAssertion().getPrincipal().getAttributes();
        Long cuid = Long.parseLong(attrs.get("uid").toString());

        if(ToolUntil.CheckRole("admin",corpid.toString(), AssertionHolder.getAssertion())) {
            List<UserEntry> ret = new ArrayList<UserEntry>();
            for (UserEntry e : Users
                    ) {
                if(e.getUid().longValue() == cuid.longValue())
                {
                    //忽略当前的登入用户
                    continue;
                }
                e.setCorpId(corpid);
                ErrorCode error = bookServiceV1.delUser(e.getUid());
                rep = new Response(error.getCode(), error.getMsg(), ret);
                if (error==ErrorCode.OK) {
                    ret.add(e);
                }else{
                    logger.debug(String.format("delete uid:%s is faile",e.getUid()));
                }
            }
            rep = new Response(ErrorCode.OK.getCode(), ErrorCode.OK.getMsg(), ret);
        }else
        {
            rep = new Response(ErrorCode.AuthFail.getCode(),ErrorCode.AuthFail.getMsg(),"");
        }
        logger.debug("delUsers Leave");
        return rep;
    }


    /**
     * 批量删除用户
     * @param Users 用户id
     * @return
     */
    @RequestMapping(value="/internal/v1/corps/{corpid:\\d+}/users",method = RequestMethod.DELETE)
    public @ResponseBody Response unauthdelUsers(@RequestBody List<UserEntry> Users,
                                           @PathVariable("corpid") Long corpid,
                                           HttpServletResponse response)
    {
        logger.debug("delUsers Enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        Response rep = null;

        if(ToolUntil.CheckRole("admin",corpid.toString(), AssertionHolder.getAssertion())) {
            List<UserEntry> ret = new ArrayList<UserEntry>();
            for (UserEntry e : Users
                    ) {
                e.setCorpId(corpid);
                ErrorCode error = bookServiceV1.delUser(e.getUid());
                rep = new Response(error.getCode(), error.getMsg(), ret);
                if (error==ErrorCode.OK) {
                    ret.add(e);
                }else{
                    logger.debug(String.format("delete uid:%s is faile",e.getUid()));
                }
            }
            rep = new Response(ErrorCode.OK.getCode(), ErrorCode.OK.getMsg(), ret);
        }else
        {
            rep = new Response(ErrorCode.AuthFail.getCode(),ErrorCode.AuthFail.getMsg(),"");
        }
        logger.debug("delUsers Leave");
        return rep;
    }


    /**
     * 更新用户信息，返回更新后的用户信息
     * @param corpid
     * @param uid
     * @return
     */
    @RequestMapping(value="/v1/corps/{corpid:\\d+}/users/{uid:\\d+}",method = RequestMethod.PUT)
    public @ResponseBody Response updateUser(@RequestBody UserEntry user,@PathVariable("corpid") Long corpid,
                                             @PathVariable("uid") Long uid,
                                             @RequestParam(value = "oldps",required = false,defaultValue = "") String oldps,
                                             HttpServletResponse response)
    {
        logger.debug("updateUser Enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        user.setCorpId(corpid);
        user.setUid(uid);
        Response rep = null;
        ErrorCode error = ErrorCode.OK;
        if(!oldps.isEmpty())
        {
            //验证旧密码
            if(!bookServiceV1.Auth(user.getUid(),oldps))
            {
                error = ErrorCode.AuthFail;
            }
        }
        if(error == ErrorCode.OK)
        {
            if(user.getPassword()!=null && !user.getPassword().isEmpty())
            {
                //新密码编码
                user.setPassword(ToolUntil.EncodePwd(user.getPassword()));
            }

            //当前登入用户为管理员或root
            if(ToolUntil.CheckRole("admin",corpid.toString(), AssertionHolder.getAssertion())) {
                if (user.getDeptIds()!=null) {
                    //将部门id转成部门全名
                    user.setDepts(pareDeptId(user.getDeptIds()));
                };
            }else if(ToolUntil.CheckRole("normal",corpid.toString(), AssertionHolder.getAssertion()))
            {
                //普通用户不能修改自己的角色和所属部门
                user.setRole(null);
                user.setDeptIds(null);
            }else
            {
                error = ErrorCode.AuthFail;
            }
        }
        if(error == ErrorCode.OK)
        {
            error = bookServiceV1.updateUser(user);
        }
        rep = new Response(error.getCode(),error.getMsg(),user);
        logger.debug("updateUser Leave");
        return rep;
    }

//    /**
//     * 获取指定用户信息
//     * 更新departmentNumber 实现用户在部门间的移动
//     * @param corpid
//     * @param uid
//     * @return
//     */
//    @RequestMapping(value="/v1/corps/{corpid:\\d+}/users/{uid:\\d+}",method = RequestMethod.GET)
//    public @ResponseBody Response getUser(@PathVariable("corpid") Long corpid,
//                                          @PathVariable("uid") Long uid,
//                                          HttpServletResponse response)
//    {
//        logger.debug("getUser Enter");
//        response.setHeader("Access-Control-Allow-Origin","*");
//        Response rep = null;
//        if(ToolUntil.CheckRole("normal",corpid.toString(), AssertionHolder.getAssertion())) {
//            FilterItem filter = new FilterItem();
//            filter.setOp("&");
//            filter.getFilters().add(filter.new Item("uid",uid.toString(),"equal"));
//            Map<String,Object> data = bookServiceV1.queryUser(filter,null,1,1,"");
//            List<UserEntry> users = (List<UserEntry>)data.get("recodes");
//            if(users!=null && users.size()==1)
//            {
//                rep = new Response(ErrorCode.OK.getCode(), ErrorCode.OK.getMsg(), users.get(0));
//            }else{
//                rep = new Response(ErrorCode.NOTFOUD.getCode(),ErrorCode.NOTFOUD.getMsg(),null);
//            }
//        }else
//        {
//
//            rep = new Response(ErrorCode.AuthFail.getCode(),ErrorCode.AuthFail.getMsg(),"");
//        }
//        logger.debug("getUser Leave");
//        return rep;
//    }

    /**
     * 用户搜索
     * @param corpid
     * @return
     */
    @RequestMapping(value="/v1/corps/users",method = RequestMethod.GET)
    public @ResponseBody Object getUsers(@RequestParam(value = "corpid",required = false,defaultValue = "") String corpid,
                                           @RequestParam(value = "uid",required = false,defaultValue = "") String uid,
                                           @RequestParam(value = "userId",required = false,defaultValue = "") String userId,
                                           @RequestParam(value = "name",required = false,defaultValue = "") String name,
                                           @RequestParam(value = "email",required = false,defaultValue = "") String email,
                                           @RequestParam(value = "matchrule",required = false,defaultValue = "like") String rule,
                                           @RequestParam(value = "mobile",required = false,defaultValue = "") String mobile,
                                           @RequestParam(value = "page", required = true, defaultValue = "1") int page,
                                           @RequestParam(value = "pagesize", required = true, defaultValue = "10") int pagesize,
                                           @RequestParam(value = "callback", required = false, defaultValue = "") String callback,
                                           HttpServletResponse response)
    {
        logger.debug("getUsers Enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        Object ret = null;

        //以下查询条件是或的关系
        FilterItem filter = new FilterItem();
        filter.setOp("|");
        FilterItem.Item flabeledURI =  filter.new Item("labeledURI",userId,rule);
        filter.getFilters().add(flabeledURI);
        FilterItem.Item fdisName =  filter.new Item("displayName",name,rule);
        filter.getFilters().add(fdisName);
        FilterItem.Item fmail =  filter.new Item("mail",email,rule);
        filter.getFilters().add(fmail);
        FilterItem.Item fmobile =  filter.new Item("mobile",mobile,rule);
        filter.getFilters().add(fmobile);

        //以下查询条件是与的关系
        FilterItem filters = new FilterItem();
        filters.setOp("&");
        FilterItem.Item fuid =  filters.new Item("uid",uid,"equal");
        filters.getFilters().add(fuid);
        FilterItem.Item fo =  filters.new Item("o",corpid.toString(),"equal");
        filters.getFilters().add(fo);
        filters.getFilters().add(filter);
        Map<String,Object> data = bookServiceV1.queryUser(filters,null,page,pagesize,null);
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
        ret = new Response(ErrorCode.OK.getCode(),ErrorCode.OK.getMsg(),data);
        if(!callback.equals(""))
        {
            MappingJacksonValue tmp = new MappingJacksonValue(ret);
            tmp.setJsonpFunction(callback);
            ret = tmp;
        }
        logger.debug("getUsers Leave");
        return ret;
    }

    /**
     * 用户搜索
     * @param corpid
     * @return
     */
    @RequestMapping(value="/internal/v1/corps/users",method = RequestMethod.GET)
    public @ResponseBody Object unauthgetUsers(@RequestParam(value = "corpid",required = false,defaultValue = "") String corpid,
                                           @RequestParam(value = "uid",required = false,defaultValue = "") String uid,
                                           @RequestParam(value = "userId",required = false,defaultValue = "") String userId,
                                           @RequestParam(value = "name",required = false,defaultValue = "") String name,
                                           @RequestParam(value = "email",required = false,defaultValue = "") String email,
                                           @RequestParam(value = "matchrule",required = false,defaultValue = "like") String rule,
                                           @RequestParam(value = "mobile",required = false,defaultValue = "") String mobile,
                                           @RequestParam(value = "page", required = true, defaultValue = "1") int page,
                                           @RequestParam(value = "pagesize", required = true, defaultValue = "10") int pagesize,
                                                 @RequestParam(value = "callback", required = false, defaultValue = "") String callback,
                                           HttpServletResponse response)
    {
        logger.debug("getUsers Enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        Object ret = null;

        //以下查询条件是或的关系
        FilterItem filter = new FilterItem();
        filter.setOp("|");
        FilterItem.Item flabeledURI =  filter.new Item("labeledURI",userId,rule);
        filter.getFilters().add(flabeledURI);
        FilterItem.Item fdisName =  filter.new Item("displayName",name,rule);
        filter.getFilters().add(fdisName);
        FilterItem.Item fmail =  filter.new Item("mail",email,rule);
        filter.getFilters().add(fmail);
        FilterItem.Item fmobile =  filter.new Item("mobile",mobile,rule);
        filter.getFilters().add(fmobile);

        //以下查询条件是与的关系
        FilterItem filters = new FilterItem();
        filters.setOp("&");
        FilterItem.Item fuid =  filters.new Item("uid",uid,"equal");
        filters.getFilters().add(fuid);
        FilterItem.Item fo =  filters.new Item("o",corpid.toString(),"equal");
        filters.getFilters().add(fo);
        filters.getFilters().add(filter);
        Map<String,Object> data = bookServiceV1.queryUser(filters,null,page,pagesize,null);
        if(data!=null)
        {
            data.put("users",data.get("recodes"));
            data.remove("recodes");
            List<UserEntry> li = (List<UserEntry>)data.get("users");
            for (UserEntry e:li
                 ) {
                if(e.getDeptIds()!=null)
                {
                    e.setDepts(pareDeptId(e.getDeptIds()));
                }
            }
        }
        ret = new Response(ErrorCode.OK.getCode(),ErrorCode.OK.getMsg(),data);

        if(!callback.equals(""))
        {
            MappingJacksonValue tmp = new MappingJacksonValue(ret);
            tmp.setJsonpFunction(callback);
            ret = tmp;
        }
        logger.debug("getUsers Leave");
        return ret;
    }

}
