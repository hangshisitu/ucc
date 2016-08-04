package com.ucc.addrbook;

import org.apache.log4j.Logger;
import org.ldaptive.*;
import org.ldaptive.SearchResult;
import org.ldaptive.beans.reflect.DefaultLdapEntryMapper;
import org.ldaptive.control.PagedResultsControl;
import org.ldaptive.control.SortKey;
import org.ldaptive.control.SortRequestControl;
import org.ldaptive.control.util.VirtualListViewClient;
import org.ldaptive.control.util.VirtualListViewParams;
import org.ldaptive.handler.SearchEntryHandler;
import org.ldaptive.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//import javax.naming.NameAlreadyBoundException;
//import javax.naming.NameNotFoundException;
//import javax.naming.NamingEnumeration;
//import javax.naming.NamingException;
//import javax.naming.directory.*;
//import javax.naming.directory.SearchResult;
//import javax.naming.ldap.*;
import javax.naming.directory.*;
import java.io.IOException;
import java.util.*;

/**
 * Created by Administrator on 2016/3/18.
 */
@Service("bookDao")
public class BookDao {

    @Autowired
    private PooledConnectionFactory pooledLdapConnectionFactory;

    @Autowired
    public ldapEntity ldapEntityins;

    private int sequenceBits = 28;
    private int workerId=1;

    private static final Logger logger = Logger.getLogger(BookDao.class);

    private static byte[] lock = new byte[1];

    /**
     * id生成器
     * @return
     */
    public String nextId()
    {
        logger.info("nextId Enter");
        Long cid=0L;
        Connection conn = null;
        try{
            conn = pooledLdapConnectionFactory.getConnection();
            SearchOperation search = new SearchOperation(conn);
            SearchRequest searchRequest = new SearchRequest(getBaseDn(),"objectClass=*","uid");
            searchRequest.setSearchScope(SearchScope.OBJECT);

            synchronized (lock) {
                SearchResult result = search.execute(searchRequest).getResult();
                LdapEntry entry = result.getEntry();
                cid = Long.parseLong(entry.getAttribute("uid").getStringValue());
                Long nextId = cid + 1;
                cid = (workerId << sequenceBits)|cid;

                ModifyOperation modify = new ModifyOperation(conn);
                ModifyRequest modifyRequest = new ModifyRequest(entry.getDn(),new AttributeModification(
                        AttributeModificationType.REPLACE,new LdapAttribute("uid",nextId.toString())));
                modify.execute(modifyRequest);
            }

            logger.info(String.format("curId:%d",cid));
        }catch (Exception e)
        {
            cid=0L;
            e.printStackTrace();
            logger.error(e.getMessage());
            logger.error("nextId exception");
        }finally
        {
            if(conn!=null)
            {
                conn.close();
            }
        }
        logger.info("nextId Leave");
        return cid.toString();
    }



    /**
     * 返回根dn
     * @return
     */
    public String getBaseDn()
    {
        return (String)ldapEntityins.getEntitys().get("basedn");
    }

    public String getDeprecatedDn()
    {
        return (String)ldapEntityins.getEntitys().get("deprecateddn");
    }

    /**
     * 添加条目
     * @param entry
     * @return
     */
    public boolean addEntry(LdapEntry entry)
    {
        logger.info("addEntry Enter");
        boolean ret =false;
        Connection conn=null;
        try {
            conn = pooledLdapConnectionFactory.getConnection();
            AddOperation add = new AddOperation(conn);
            add.execute(new AddRequest(entry.getDn(), entry.getAttributes()));
            ret= true;
        }catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
            if(((LdapException)e).getResultCode()==ResultCode.OTHER)
            {
                ret =true;
            }
        }finally {
            if(conn!=null)
            {
                conn.close();
            }
        }
        logger.info("addEntry Leave");
        return ret;
    }

    /**
     * 更新条目信息
     * @param dn
     * @param newdn
     * @param attrs
     * @return
     */
    public ResultCode updateEntry(String dn, String newdn,AttributeModification attrs[])
    {
        logger.info("updateEntry Enter");
        ResultCode ret =ResultCode.SUCCESS;
        Connection conn=null;
        try {
            conn = pooledLdapConnectionFactory.getConnection();
            if(newdn!=null && !newdn.equals(dn))
            {
                ModifyDnOperation modifyDn = new ModifyDnOperation(conn);
                modifyDn.execute(new ModifyDnRequest(dn, newdn));
                dn = newdn;
            }
            ModifyOperation modify = new ModifyOperation(conn);
            modify.execute(new ModifyRequest(dn,attrs));
        } catch(LdapException e)
        {
            ret = e.getResultCode();
            e.printStackTrace();
            logger.error(e.getMessage());
            if(ret == ResultCode.OTHER)
            {
                ret = ResultCode.SUCCESS;
            }
        }
        catch(Exception e)
        {
            ret = ResultCode.OPERATIONS_ERROR;
            e.printStackTrace();
            logger.error(e.getMessage());
        }finally {
            if(conn!=null)
            {
                conn.close();
            }
        }
        logger.info("updateEntry Leave");
        return ret;
    }

    public ResultCode delEntryEx(String dn,Long uid)
    {
        return MoveEntry(dn,String.format("uid=%d,%s",uid,getDeprecatedDn()));
    }
    public ResultCode MoveEntry(String dn, String newdn)
    {
        logger.info(String.format("MoveEntry Enter[dn:%s][newdn:%s]",dn,newdn));
        ResultCode ret =ResultCode.SUCCESS;
        Connection conn=null;
        try {
            conn = pooledLdapConnectionFactory.getConnection();
            if(newdn!=null && !newdn.equals(dn))
            {
                ModifyDnOperation modifyDn = new ModifyDnOperation(conn);
                modifyDn.execute(new ModifyDnRequest(dn, newdn));
                dn = newdn;
            }
        } catch(LdapException e)
        {
            ret = e.getResultCode();
            e.printStackTrace();
            logger.error(e.getMessage());
            if(ret == ResultCode.OTHER)
            {
                ret = ResultCode.SUCCESS;
            }
        }
        catch(Exception e)
        {
            ret = ResultCode.OPERATIONS_ERROR;
            e.printStackTrace();
            logger.error(e.getMessage());
        }finally {
            if(conn!=null)
            {
                conn.close();
            }
        }
        logger.info("MoveEntry Leave");
        return ret;
    }
    /**
     * 删除条目
     * @param dn
     * @return
     */
    public boolean delEntry(String dn)
    {
        logger.info("delEntry Enter");
        boolean ret = false;
        Connection conn=null;
        try{
            conn = pooledLdapConnectionFactory.getConnection();
            DeleteOperation delete = new DeleteOperation(conn);
            delete.execute(new DeleteRequest(dn));
            ret = true;
        }catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
            if(((LdapException)e).getResultCode()==ResultCode.OTHER)
            {
              ret = true;
            }
        }finally {
            conn.close();
        }
        logger.info("delEntry Leave");
        return ret;
    }

    public boolean delEntryRecursive(String dn,callback handle)
    {
        logger.info("delEntryResver Enter");
        boolean ret = true;
        Map<String,Object> data = Search(dn,new SearchFilter("objectClass=*"),new SortKey[]{new SortKey("uid")},SearchScope.SUBTREE,null,1,0,false);
        List<LdapEntry> liEntry = (List<LdapEntry>)data.get("recodes");
        for(int i=liEntry.size()-1;i>=0;i--)
        {
            String tmpdn =liEntry.get(i).getDn();
            if(!delEntry(tmpdn))
            {
                ret = false;
                break;
            }else if(handle!=null){
                handle.preDel(this,liEntry.get(i));
            }
        }
        logger.info("delEntryResver Leave");
        return ret;
    }

    public boolean delUserGroupRef(String dn)
    {
        logger.info("delEntryResver Enter");
        boolean ret = true;
        //查询该部门下的子部门
        Map<String,Object> data = Search(dn,new SearchFilter("objectClass=*"),new SortKey[]{new SortKey("uid")},SearchScope.SUBTREE,null,1,0,true);
        List<GroupEntry> liEntry = (List<GroupEntry>)data.get("recodes");

        FilterItem f = new FilterItem();
        f.setOp("&");
        f.getFilters().add(f.new Item("objectClass","inetOrgPerson","equal"));
        FilterItem subf = new FilterItem();
        subf.setOp("|");
        Set<String> delgroupids= new HashSet<String>();
        for(int i=liEntry.size()-1;i>=0;i--)
        {
            String id = liEntry.get(i).getDeptId().toString();
            subf.getFilters().add(subf.new Item("departmentNumber",id,"equal"));
            delgroupids.add(id);
        }
        f.getFilters().add(subf);

        SortKey sortkeys[] = new SortKey[]{new SortKey("labeledURI")};

        //查询属于这些部门的用户
        Map<String,Object> tmp=null;
        tmp = Search(getBaseDn(),new SearchFilter(f.toString()),sortkeys, SearchScope.SUBTREE,null,1,0,true);
        if(tmp!=null)
        {
            List<UserEntry> users = (List<UserEntry> )tmp.get("recodes");
            if(users!=null)
            {
                for (UserEntry user:users
                        ) {
                    if(user!=null && !user.getDn().isEmpty())
                    {
                        List<AttributeModification> modattrs= new LinkedList<AttributeModification>();
                        for(String strid:user.getDeptIds())
                        {
                            if(delgroupids.contains(strid))
                            {
                                LdapAttribute attr = new LdapAttribute("departmentNumber",strid);
                                modattrs.add(new AttributeModification(AttributeModificationType.REMOVE,attr));
                            }
                        }

                        //把用户从将要删除的部门里移除
                        if(ResultCode.SUCCESS!=updateEntry(user.getDn(),null,modattrs.toArray(new AttributeModification[0])))
                        {
                            ret = false;
                        }
                    }
                }
            }
        }
        logger.info("delEntryResver Leave");
        return ret;
    }

    String getEntryType(LdapEntry entry)
    {
//        logger.info("getEntryType Enter");
        String ret="entry";
        LdapAttribute attr = entry.getAttribute("objectClass");
        for (String str:attr.getStringValues())
        {
            if(str.equals("inetOrgPerson"))
            {
                ret="user";
                break;
            }else if(str.equals("groupOfNames"))
            {
                ret = "group";
                break;
            }else if(str.equals("organization"))
            {
                ret="corp";
                break;
            }
        }
//        logger.info("getEntryType Leave");
        return ret;
    }
    /**
     *
     * @param entry
     * @return
     */
    public Object RecodeDao(LdapEntry entry)
    {
//        logger.info("RecodeDao Enter");
        Object ret=entry;
        try{
            String entryType = getEntryType(entry);
            if(entryType.equals("user"))
            {
                DefaultLdapEntryMapper<UserEntry> mapper = new DefaultLdapEntryMapper<UserEntry>();
                UserEntry user = new UserEntry();
                mapper.map(entry,user);
                ret = user;
            }else if(entryType.equals("group"))
            {
                DefaultLdapEntryMapper<GroupEntry> mapper = new DefaultLdapEntryMapper<GroupEntry>();
                GroupEntry group = new GroupEntry();
                mapper.map(entry,group);
//                if(group.getMember()!=null)
//                {
//                    long count =(long)group.getMember().size();
//                    if(count>0 && group.getMember().contains(""))
//                    {
//                        count --;
//                    }
//                    group.setUcount(count);
//                }
                ret = group;
            }else if(entryType.equals("corp"))
            {
                DefaultLdapEntryMapper<CorpEntry> mapper = new DefaultLdapEntryMapper<CorpEntry>();
                CorpEntry corp = new CorpEntry();
                mapper.map(entry,corp);
                ret = corp;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
//        logger.info("RecodeDao Leave");
        return ret;
    }

    /**
     * 通过dn获取条目
     * @param dn
     * @return
     */
    public  LdapEntry getEntry(String dn)
    {
        logger.info("getEntry Enter");
        Connection conn = null;
        LdapEntry ret = null;
        try {
            conn = pooledLdapConnectionFactory.getConnection();
            SearchOperation op = new SearchOperation(conn);
            SearchRequest request = SearchRequest.newObjectScopeSearchRequest(dn);
            SearchResult result = (SearchResult)op.execute(request).getResult();
            ret = result.getEntry(dn);
        }catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
        }finally {
            if(conn!=null)
            {
                conn.close();
            }
        }
        logger.info("getEntry Enter");
        return  ret;
    }

    /**
     * LDAP查询
     * @param baseDn  查询DN
     * @param filter   过滤器
     * @param sortkey  排序key数组
     * @param scope    查询范围
     * @param page     页号
     * @param pagesize  页大小
     * @return
     *     map["total"]  记录总数
     *     map["recodes"] 记录数组
     */
    public Map Search(String baseDn, SearchFilter filter, SortKey sortkey[] , SearchScope scope, SearchEntryHandler handler, int page, int pagesize,boolean autodao)
    {
        logger.debug("Search Enter.");
        logger.debug(String.format("Search [baseDn: %s] [filter:%s] [sope:%s]",baseDn,filter.getFilter(),scope.toString()));

        //page最小为1
        page = Math.max(page,1);
        List recodes = new ArrayList();
        Map<String,Object> data = new HashMap<String, Object>();
        Long total = 0L;
        Connection conn =null;
        try {
            conn = pooledLdapConnectionFactory.getConnection();
            SearchRequest request = new SearchRequest(baseDn,filter);
            request.setSearchScope(scope);
//            if(sortkey!=null && sortkey.length>0)
//            {
//                SortRequestControl src = new SortRequestControl(sortkey, true); // sort by surname
//                request.setControls(src);
//                request.setSortBehavior(SortBehavior.SORTED);
//            }else{
//                request.setSortBehavior(SortBehavior.ORDERED );
//            }
            if(handler!=null)
            {
                request.setSearchEntryHandlers(handler);
            }
            SearchOperation searchop = new SearchOperation(conn);
            org.ldaptive.Response<SearchResult> response = searchop.execute(request);
            SearchResult result = response.getResult();
            total = new Long(result.size());
            data.put("total",total);
            int start = (page-1)*pagesize;
            int index=0;
            for (LdapEntry entry:result.getEntries())
            {
                if(pagesize==0 || (index>=start && index<start+pagesize)){
                    if(autodao)
                    {
                        recodes.add(RecodeDao(entry));
                    }else{
                        recodes.add(entry);
                    }
                }
                if(index>=start+pagesize && pagesize!=0)
                {
                    break;
                }
                index++;
            }
            data.put("recodes",recodes);
        }catch(Exception e)
        {
            e.printStackTrace();
            data = null;
            logger.error(e.getMessage());
        }finally {
            if(conn!=null)
            {
                conn.close();
            }
        }
        logger.info("Search Leave.");
        return data;
    }

    /**
     * LDAP查询
     * @param baseDn  查询DN
     * @param filter   过滤器
     * @param sortkey  排序key数组
     * @param scope    查询范围
     * @param page     页号
     * @param pagesize  页大小
     * @return
     *     map["total"]  记录总数
     *     map["recodes"] 记录数组
     */
    public Map SearchGrp(String baseDn, SearchFilter filter, SortKey sortkey[] , SearchScope scope, SearchEntryHandler handler, int page, int pagesize,boolean autodao,String[] attrs)
    {
        logger.debug("Search Enter.");
        logger.debug(String.format("Search [baseDn: %s] [filter:%s] [sope:%s]",baseDn,filter.getFilter(),scope.toString()));

        //page最小为1
        page = Math.max(page,1);
        List recodes = new ArrayList();
        Map<String,Object> data = new HashMap<String, Object>();
        Long total = 0L;
        Connection conn =null;
        try {
            conn = pooledLdapConnectionFactory.getConnection();
            SearchRequest request = new SearchRequest(baseDn,filter,attrs);
            request.setSearchScope(scope);
            if(sortkey!=null && sortkey.length>0)
            {
                SortRequestControl src = new SortRequestControl(sortkey, true); // sort by surname
                request.setControls(src);
                request.setSortBehavior(SortBehavior.SORTED);
            }else{
                request.setSortBehavior(SortBehavior.ORDERED );
            }
            if(handler!=null)
            {
                request.setSearchEntryHandlers(handler);
            }
            SearchOperation searchop = new SearchOperation(conn);
            org.ldaptive.Response<SearchResult> response = searchop.execute(request);
            SearchResult result = response.getResult();
            total = new Long(result.size());
            data.put("total",total);
            int start = (page-1)*pagesize;
            int index=0;
            for (LdapEntry entry:result.getEntries())
            {
                if(pagesize==0 || (index>=start && index<start+pagesize)){
                    if(autodao)
                    {
                        recodes.add(RecodeDao(entry));
                    }else{
                        recodes.add(entry);
                    }
                }
                if(index>=start+pagesize && pagesize!=0)
                {
                    break;
                }
                index++;
            }
            data.put("recodes",recodes);
        }catch(Exception e)
        {
            e.printStackTrace();
            data = null;
            logger.error(e.getMessage());
        }finally {
            if(conn!=null)
            {
                conn.close();
            }
        }
        logger.info("Search Leave.");
        return data;
    }

    /**
     * LDAP查询
     * @param baseDn  查询DN
     * @param filter   过滤器
     * @param sortkey  排序key数组
     * @param scope    查询范围
     * @param page     页号
     * @param pagesize  页大小
     * @return
     *     map["total"]  记录总数
     *     map["recodes"] 记录数组
     */
    public Map SearchD( String baseDn,SearchFilter filter,SortKey sortkey[] ,SearchScope scope,int page,int pagesize)
    {
        logger.debug("Search Enter.");
        logger.debug(String.format("Search [baseDn: %s] [filter:%s] [sope:%s]",baseDn,filter.getFilter(),scope.toString()));
        logger.debug(String.format("Search sortkey: %s",sortkey.toString()));

        //page最小为1
        page = Math.max(page,1);
        List recodes = new ArrayList();
        Map<String,Object> data = new HashMap<String, Object>();
        Long total = 0L;

        try {
            SearchRequest request = new SearchRequest(baseDn,filter);
            request.setSearchScope(scope);
            SearchOperation searchop = new SearchOperation(pooledLdapConnectionFactory.getConnection());
            PagedResultsControl prc = new PagedResultsControl(pagesize);
            request.setControls(prc);
            SearchResult result = new SearchResult();
            byte[] cookie = null;
            int cpage=0;
            do {
                prc.setCookie(cookie);
                org.ldaptive.Response<SearchResult> response = searchop.execute(request);
                result.addEntries(response.getResult().getEntries());
                cookie = null;
                PagedResultsControl ctl = (PagedResultsControl) response.getControl(PagedResultsControl.OID);
                if (ctl != null) {
                    if (ctl.getCookie() != null && ctl.getCookie().length > 0) {
                        cookie = ctl.getCookie();
                    }
                }
                cpage++;
                if(cpage==page)
                {
                    break;
                }
            } while (cookie != null);
            total = new Long(result.size());
            for (LdapEntry entry:result.getEntries())
            {
                recodes.add(RecodeDao(entry));
            }

        }catch(Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        data.put("total",total);
        data.put("recodes",recodes);
        logger.info("Search Leave.");
        return data;
    }

    /**
     * LDAP查询
     * @param baseDn  查询DN
     * @param filter   过滤器
     * @param sortkey  排序key数组
     * @param scope    查询范围
     * @param page     页号
     * @param pagesize  页大小
     * @return
     *     map["total"]  记录总数
     *     map["recodes"] 记录数组
     */
    public Map SearchEx( String baseDn,SearchFilter filter,SortKey sortkey[] ,SearchScope scope,int page,int pagesize)
    {
        logger.debug("Search Enter.");
        logger.debug(String.format("Search [baseDn: %s] [filter:%s] [sope:%s]",baseDn,filter.toString(),scope.toString()));
        logger.debug(String.format("Search sortkey: %s",sortkey.toString()));

        //page最小为1
        page = Math.max(page,1);
        List recodes = new ArrayList();
        Map<String,Object> data = new HashMap<String, Object>();
        Long total = 0L;
        Connection conn = null;
        try {
            conn = pooledLdapConnectionFactory.getConnection();
            VirtualListViewClient client = new VirtualListViewClient(conn,sortkey);
            SearchRequest request = new SearchRequest(baseDn,filter);
            request.setSearchScope(scope);
            org.ldaptive.Response<SearchResult> response = client.execute(request, new VirtualListViewParams((page-1)*pagesize, 0, pagesize-1));
            SearchResult result = response.getResult();
            total = new Long(result.size());
            for (LdapEntry entry:result.getEntries())
            {
                recodes.add(RecodeDao(entry));
            }

        }catch(Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
        }finally {
            if(conn!=null)
            {
                conn.close();
            }
        }
        data.put("total",total);
        data.put("recodes",recodes);
        logger.info("Search Leave.");
        return data;
    }
}
