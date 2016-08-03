package com.ucc.addrbook;



import org.jasig.cas.client.util.AssertionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.HttpRequestHandlerServlet;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/5/4.
 */
//@RequestMapping("/v1/corps")
@RestController
public class CorpControllerV1 {

    @Autowired
    private BookServiceV1 bookServiceV1;

    private static final Logger logger = Logger.getLogger(CorpControllerV1.class);

    /**
     * 添加企业
     * 需要认证中心的管理员权限(employeeType==root)
     * @param corp  企业信息
     * @return
     */
    @RequestMapping(value="/v1/corps",method = RequestMethod.POST)
    public @ResponseBody Response addCorp(@RequestBody CorpEntry corp,HttpServletResponse response) {
        logger.debug("addrCorp Enter");
        Response rep;
        //鉴权（只有root角色拥有添加企业的权限）
        if(ToolUntil.CheckRole("root","0",AssertionHolder.getAssertion())) {
            ErrorCode error = bookServiceV1.addCorp(corp);
            rep = new Response(error.getCode(),error.getMsg(),corp);
        }else
        {
            rep = new  Response(ErrorCode.AuthFail.getCode(), ErrorCode.AuthFail.getMsg(), null);
        }
        logger.debug("addrCorp Leave");
        return rep;
    }

    /**
     * 删除企业
     * employeeType=admin
     * @param corpid  企业ID
     * 需要企业管理员权限
     * @return
     */
    @RequestMapping(value="/v1/corps/{corpid:\\d+}",method = RequestMethod.DELETE)
    public @ResponseBody Response delCorp(@PathVariable("corpid") Long corpid,
                                          HttpServletResponse response) {
        logger.debug("delCorp enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        Response rep;
        if(ToolUntil.CheckRole("admin",corpid.toString(),AssertionHolder.getAssertion())) {
            ErrorCode error = bookServiceV1.delCorp(corpid);
            rep = new Response(error.getCode(),error.getMsg(),corpid);
        }else
        {
            rep = new Response(ErrorCode.AuthFail.getCode(), ErrorCode.AuthFail.getMsg(), null);
        }
        logger.debug("delCorp leave");
        return rep;
    }

    /**
     * 更新企业
     * employeeType=admin
     * @param corpid  企业ID
     * @return
     */
    @RequestMapping(value="/v1/corps/{corpid:\\d+}",method = RequestMethod.PUT)
    public @ResponseBody Response updateCorp(@RequestBody CorpEntry corp,@PathVariable("corpid") Long corpid,
                                             HttpServletResponse response) {
        logger.debug("updateCorp enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        corp.setCorpId(corpid);
        Response rep ;
        if(ToolUntil.CheckRole("admin",corpid.toString(),AssertionHolder.getAssertion())) {

            ErrorCode error = bookServiceV1.updateCorp(corp);
            rep = new Response(error.getCode(), error.getMsg(), corp);
        }else
        {
            rep = new Response(ErrorCode.AuthFail.getCode(), ErrorCode.AuthFail.getMsg(), null);
        }
        logger.debug("updateCorp leave");
        return rep;
    }


    /**
     * 更新企业
     * employeeType=admin
     * @param corpid  企业ID
     * @return
     */
    @RequestMapping(value="/internal/v1/corps/{corpid:\\d+}",method = RequestMethod.PUT)
    public @ResponseBody Response unauthupdateCorp(@RequestBody CorpEntry corp,@PathVariable("corpid") Long corpid,
                                             HttpServletResponse response) {
        logger.debug("updateCorp enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        corp.setCorpId(corpid);
        Response rep ;

        ErrorCode error = bookServiceV1.updateCorp(corp);
        rep = new Response(error.getCode(), error.getMsg(), corp);

        logger.debug("updateCorp leave");
        return rep;
    }

//    /**
//     * 获取指定企业信息
//     * employeeType=normal
//     * @param corpid  企业ID
//     * @return
//     */
//    @RequestMapping(value="/{corpid:\\d+}",method = RequestMethod.GET)
//    public  @ResponseBody Response getCorp(@PathVariable("corpid") Long corpid,
//                                           HttpServletResponse response) {
//        logger.debug("getCorp enter");
//        response.setHeader("Access-Control-Allow-Origin","*");
//        Response rep = null;
//        if(ToolUntil.CheckRole("normal",corpid.toString(),AssertionHolder.getAssertion())) {
//            FilterItem filter = new FilterItem();
//            filter.setOp("&");
//            filter.getFilters().add(filter.new Item("uid",corpid.toString(),"equal"));
//            Map<String,Object> data = bookServiceV1.queryCorp(filter,null,1,1);
//            List<CorpEntry> licorps = (List<CorpEntry>)data.get("recodes");
//            if(licorps!=null && licorps.size()==1)
//            {
//                rep = new Response(ErrorCode.OK.getCode(), ErrorCode.OK.getMsg(), licorps.get(0));
//            }else{
//                rep = new Response(ErrorCode.NOTFOUD.getCode(),ErrorCode.NOTFOUD.getMsg(),null);
//            }
//
//        }else
//        {
//            rep = new Response(ErrorCode.AuthFail.getCode(), ErrorCode.AuthFail.getMsg(), null);
//        }
//        logger.debug("getCorp Leave");
//        return  rep;
//    }

    /**
     * 获取企业列表
     * employeeType=root
     * @param page  企业ID
     * @return
     */
    @RequestMapping(value="/v1/corps",method = RequestMethod.GET)
    public @ResponseBody Object getCorps(@RequestParam(value = "name",required = false,defaultValue = "") String name,
                                         @RequestParam(value = "corpid",required = false,defaultValue = "") String corpid,
                                         @RequestParam(value = "domain",required = false,defaultValue = "") String domain,
                                         @RequestParam(value = "matchrule",required = false,defaultValue = "like") String rule,
                                         @RequestParam(value = "page", required = true, defaultValue = "1") int page,
                                         @RequestParam(value = "pagesize", required = true, defaultValue = "10") int pagesize,
                                         @RequestParam(value = "callback", required = false, defaultValue = "") String callback,
                                         HttpServletResponse response
    ) {
        logger.debug("getCorps enter");
        response.setHeader("Access-Control-Allow-Origin","*");
        Object rep =null;
        Map attrs = AssertionHolder.getAssertion().getPrincipal().getAttributes();
        if(!attrs.get("role").equals("root"))
        {
            corpid = attrs.get("corpId").toString();
        }
        FilterItem filter = new FilterItem();
        filter.setOp("&");
        filter.getFilters().add(filter.new Item("uid",corpid,"equal"));

        FilterItem f = new FilterItem();
        f.setOp("|");
        f.getFilters().add(f.new Item("o",domain,rule));
        f.getFilters().add(f.new Item("description",name,rule));
        filter.getFilters().add(f);
        Map<String,Object> data = null;
        data = bookServiceV1.queryCorp(filter,null,page,pagesize);
        if(data!=null)
        {
            data.put("corps",data.get("recodes"));
            data.remove("recodes");
        }
        rep = new Response(ErrorCode.OK.getCode(),ErrorCode.OK.getMsg(), data);
        if(!callback.equals(""))
        {
            MappingJacksonValue tmp = new MappingJacksonValue(rep);
            tmp.setJsonpFunction(callback);
            rep = tmp;
        }
        logger.debug("getCorps leave");
        return rep;
    }

//    @RequestMapping(value="/v1/corps/{corpid:\\d+}/export",method = RequestMethod.GET)
//    public Response exportbook(@PathVariable("corpid") Long corpid) {
//        if(ToolUntil.CheckRole("normal",corpid.toString(),AssertionHolder.getAssertion())) {
//            Corp corp = bookDao.getCorp(corpid);
//            List<User> users = bookDao.getUsers(corpid,new UserFilter("","",""),new String[]{"cn"})
//            List<Group> = bookDao.getGroups();
//        }else
//        {
//            return  new Response(ErrorCode.AuthFail.getCode(), ErrorCode.AuthFail.getMsg(), null);
//        }
//    }
//
//    @RequestMapping(value="/v1/corps/import",method = RequestMethod.POST)
//    public Response importbook(@RequestBody book inbook) {
//        if(ToolUntil.CheckRole("root","",AssertionHolder.getAssertion())) {
//
//        }else
//        {
//            return  new Response(ErrorCode.AuthFail.getCode(), ErrorCode.AuthFail.getMsg(), null);
//        }
//    }
}

