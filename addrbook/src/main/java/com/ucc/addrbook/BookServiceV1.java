package com.ucc.addrbook;

import com.sun.jndi.ldap.LdapCtx;
import org.apache.log4j.Logger;
import org.ldaptive.*;
import org.ldaptive.beans.reflect.DefaultLdapEntryMapper;
import org.ldaptive.control.SortKey;
import org.ldaptive.handler.RecursiveEntryHandler;
import org.ldaptive.handler.SearchEntryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.*;

/**
 * Created by Administrator on 2016/5/4.
 */
@Service("bookServiceV1")
public class BookServiceV1 {

    @Autowired
    private BookDao bookDao;

    private static final Logger logger = Logger.getLogger(BookServiceV1.class);

    static callback callBack = new UpdateUserDept();
    /**
     * 添加企业
     *
     * @param corp
     * @return
     */
    public ErrorCode addCorp(CorpEntry corp) {
        logger.info("addCorp Enter");
        ErrorCode ret = ErrorCode.OK;
        Long nextid = Long.parseLong(bookDao.nextId());
        if(nextid.longValue()!=0l)
        {
            corp.setCorpId(nextid);
        }else
        {
            ret = ErrorCode.INTELERR;
            logger.info("addCorp Leave");
            return ret;
        }

        corp.setDn(String.format("o=%s,%s", corp.getDomain(), bookDao.getBaseDn()));
        corp.setPingyin(ToolUntil.getPingYin(corp.getName()));
        corp.setIsactive("0");
        if(corp.getAdmin()!=null)
        {
            //设置管理员的dn
            corp.getAdmin().setDn(String.format("labeledURI=%s,ou=people,%s", corp.getAdmin().getUserId(), corp.getDn()));
        }else{
            //添加企业时必须同时为企业创建管理员账号
            ret = ErrorCode.INVALIDARG;
        }

        LdapEntry newEntry = new LdapEntry();
        DefaultLdapEntryMapper<CorpEntry> mapper = new DefaultLdapEntryMapper<CorpEntry>();
        mapper.map(corp, newEntry);

        /* 检查企业域名和名称是否已存在 */
        FilterItem filter = new FilterItem();
        filter.setOp("|");
        filter.getFilters().add(filter.new Item("o", corp.getDomain(), "equal"));
        filter.getFilters().add(filter.new Item("description", corp.getName(), "equal"));
        Map<String, Object> data = queryCorp(filter, null, 1, 1);
        if(data!=null)
        {
            if ((Long) data.get("total") > 0) {
                ret = ErrorCode.DUPLICATE;
                logger.warn(String.format("corp domain:%s already exists", corp.getDomain()));
            }
        }else{
            ret = ErrorCode.INTELERR;
        }


        /* 检查企业管理是否已存在 */
        if (ret == ErrorCode.OK) {
            FilterItem tmpfilter = new FilterItem();
            tmpfilter.setOp("|");
            tmpfilter.getFilters().add(tmpfilter.new Item("labeledURI", corp.getAdmin().getUserId(), "equal"));
            tmpfilter.getFilters().add(tmpfilter.new Item("mail", corp.getAdmin().getEmail(), "equal"));
            tmpfilter.getFilters().add(tmpfilter.new Item("mobile", corp.getAdmin().getMobile(), "equal"));
            data = queryUser(tmpfilter, null, 1, 1, null);
            if(data != null)
            {
                if ((Long) data.get("total") > 0) {
                    ret = ErrorCode.DUPLICATE;
                    logger.warn(String.format("user userId:%s or mail:%s or mobile:%s already exists", corp.getAdmin().getUserId(),
                            corp.getAdmin().getEmail(), corp.getAdmin().getMobile()));
                }
            }else
            {
                ret = ErrorCode.INTELERR;
            }

        }
        /* 创建企业条目 */
        if (ret == ErrorCode.OK && !bookDao.addEntry(newEntry)) {
            ret = ErrorCode.INTELERR;
        }
        /*创建企业条目上下文*/
        if (ret == ErrorCode.OK && !addCorpConext(corp, corp.getAdmin())) {
            /* 创建企业条目上下文失败 回滚 */
            bookDao.delEntry(corp.getDn());
            ret = ErrorCode.INTELERR;
        }
        logger.info("addCorp Leave");
        return ret;
    }

    /**
     * 创建企业上下文
     *
     * @param corp
     * @param admin
     * @return
     */
    private boolean addCorpConext(CorpEntry corp, UserEntry admin) {
        logger.info("addCorpConext Enter");
        boolean ret = false;
        try{
            LdapEntry newEntry = new LdapEntry(String.format("ou=groups,%s", corp.getDn()),
//                new LdapAttribute("objectClass", "top"),
                    new LdapAttribute("objectClass", "organizationalUnit","uidObject"),
                    new LdapAttribute("ou", "groups"));
            Long tmpid = Long.parseLong(bookDao.nextId());
            if(tmpid.longValue()!=0l)
            {
                newEntry.addAttribute(new LdapAttribute("uid",tmpid.toString()));
            }else
            {
                return ret;
            }
            //添加gourps条目
            if (bookDao.addEntry(newEntry)) {
                newEntry.setDn(String.format("ou=people,%s", corp.getDn()));
                newEntry.removeAttribute("ou");
                newEntry.addAttribute(new LdapAttribute("ou", "people"));
                tmpid = Long.parseLong(bookDao.nextId());
                if(tmpid.longValue()!=0l)
                {
                    newEntry.addAttribute(new LdapAttribute("uid",tmpid.toString()));
                    //添加people条目
                    if (bookDao.addEntry(newEntry)) {
                        admin.setCorpId(corp.getCorpId());
                        admin.setRole("admin");
                        Long nextid =Long.parseLong(bookDao.nextId());
                        if(nextid.longValue()!=0l)
                        {
                            admin.setUid(nextid);
                            admin.setPassword(ToolUntil.EncodePwd(admin.getPassword()));
                            if(admin.getName()!=null)
                            {
                                admin.setPingyin(ToolUntil.getPingYin(admin.getName()));
                            }
                            LdapEntry adminEntry = new LdapEntry();
                            DefaultLdapEntryMapper<UserEntry> mapper = new DefaultLdapEntryMapper<UserEntry>();
                            mapper.map(admin, adminEntry);
                            //添加企业管理员条目
                            if (bookDao.addEntry(adminEntry)) {
                                ret = true;
                            }
                        }
                        if(!ret)
                        {
                            //添加企业管理员失败，回滚
                            bookDao.delEntry(String.format("ou=people,%s", corp.getDn()));
                            bookDao.delEntry(String.format("ou=groups,%s", corp.getDn()));
                            logger.error("add corp admin failed.");
                        }
                    }
                }
                if(!ret) {
                    //添加people条目失败，回滚
                    bookDao.delEntry(String.format("ou=groups,%s", corp.getDn()));
                    logger.error("add corp people failed.");
                }
            }

        }catch (Exception e)
        {

            e.printStackTrace();
            logger.error(e.toString());
            logger.error(e.getMessage());
        }

        logger.info("addCorpConext Leave");
        return ret;
    }

    /**
     * 删除企业
     *
     * @param corpid
     * @return
     */
    public ErrorCode delCorp(Long corpid) {
        logger.info("delCorp Enter");
        ErrorCode error = null;
        LdapEntry entry = getEntry(corpid);
        String dn = entry.getDn();
        if (!dn.isEmpty()) {
//            error = bookDao.delEntryRecursive(dn,null) ? ErrorCode.OK : ErrorCode.INTELERR;
            error = bookDao.delEntryEx(dn,corpid)==ResultCode.SUCCESS ? ErrorCode.OK : ErrorCode.INTELERR;
        } else {
            error = ErrorCode.NOTFOUD;
        }
        logger.info("delCorp Leave");
        return error;
    }

    /**
     * 通过id查寻Entry
     * @param uid
     * @return
     */
    public LdapEntry getEntry(Long uid)
    {
        logger.info("getEntry Enter");
        String filter = String.format("uid=%d",uid);
        Map<String, Object> data = bookDao.Search(bookDao.getBaseDn(),new SearchFilter(filter),null,SearchScope.SUBTREE,null,1,1,false );
        if(data!=null)
        {
            List<LdapEntry> entrise = (List<LdapEntry>) data.get("recodes");
            if (entrise != null && entrise.size() == 1) {

                logger.info("getEntry Leave");
                return entrise.get(0);
            }
        }
        logger.info("getEntry Leave");
        return null;
    }

    /**
     * 返回部门全名
     * @param uid
     * @param hasdomain
     * @return
     */
    public String getDeptFullName(Long uid,boolean hasdomain)
    {
        logger.info("getDeptFullName Enter");
        StringBuilder sb =new StringBuilder();
        LdapEntry entry = getEntry(uid);
        if(entry==null) {
            logger.info("getDeptFullName Leave");
            return null;
        }
        String dn = entry.getDn();
        String item[] = dn.split(",");
        for (int i=item.length-1;i>=0;i--)
        {
            if(item[i].startsWith("o") && hasdomain==true )
            {
                sb.append(String.format("%s",item[i].substring(2)));
            }else if(item[i].startsWith("cn"))
            {
                sb.append(String.format("/%s",item[i].substring(3)));
            }
        }
        String fullname = sb.toString();
        if(fullname.startsWith("/"))
        {
            fullname = fullname.substring(1);
        }
        logger.info("getDeptFullName Leave");
        return fullname;
    }

    /**
     * 更新企业信息
     * @return
     */
    public ErrorCode updateCorp(CorpEntry corp)
    {
        logger.info("updateCorp Enter");
        ErrorCode error=ErrorCode.OK;
        //检查新的域名和名称是否已存在
        FilterItem filter = new FilterItem();
        filter.setOp("|");
        filter.getFilters().add(filter.new Item("uid",corp.getCorpId().toString(),"equal"));
        filter.getFilters().add(filter.new Item("description",corp.getName(),"equal"));
        filter.getFilters().add(filter.new Item("o",corp.getDomain(),"equal"));
        Map<String,Object> data = queryCorp(filter,null,1,2);
        CorpEntry oldcorp=null;
        if(data != null)
        {
            if((Long)data.get("total")>1)
            {
                List<CorpEntry> lcorp = (List<CorpEntry>)data.get("recodes");
                if((corp.getCorpId()!=null && lcorp.get(0).getCorpId()!=corp.getCorpId()))
                {
                    //新的域名或名称已存在
                    error = ErrorCode.DUPLICATE;
                }
            }else if((Long)data.get("total")<1)
            {
                //要更新的企业不存在
                error = ErrorCode.NOTFOUD;
            }else
            {
                List<CorpEntry> lcorp = (List<CorpEntry>)data.get("recodes");
                oldcorp = lcorp.get(0);
                if(oldcorp.getCorpId().longValue()!=corp.getCorpId().longValue())
                {
                    //要更新的企业不存在
                    error = ErrorCode.NOTFOUD;
                }
            }
        }else
        {
            //访问ldap出错
            error = ErrorCode.INTELERR;
        }

        if(error==ErrorCode.OK)
        {
            DefaultLdapEntryMapper<CorpEntry> mapper = new DefaultLdapEntryMapper<CorpEntry>();
            corp.setDn(oldcorp.getDn());
            if(corp.getName()!=null)
            {
                corp.setPingyin(ToolUntil.getPingYin(corp.getName()));
            }
            LdapEntry newEntry = new LdapEntry();
            mapper.map(corp,newEntry);
            List<AttributeModification> attrs = new ArrayList<AttributeModification>();

            String newdn=null;
            for (LdapAttribute attr: newEntry.getAttributes()
                    ) {
                if(attr.getName().equals("uid")
                        || attr.getName().equals("objectClass"))
                {
                    continue;
                }
                //修改域名，要更改dn
                if(attr.getName().equals("o") && !attr.getStringValue().equals(oldcorp.getDomain()))
                {
                    newdn = String.format("o=%s,%s",corp.getDomain(),bookDao.getBaseDn());
                }
                attrs.add(new AttributeModification(AttributeModificationType.REPLACE,attr));
            }

//            error =  bookDao.updateEntry(corp.getDn(),newdn,attrs.toArray(new AttributeModification[0]))? ErrorCode.OK:ErrorCode.INTELERR;
            ResultCode code = bookDao.updateEntry(corp.getDn(),newdn,attrs.toArray(new AttributeModification[0]));
            switch (code)
            {
                case SUCCESS:
                    error = ErrorCode.OK;
                    break;
                case OPERATIONS_ERROR:
                    error = ErrorCode.INTELERR;
                    break;
                default:
                    error = ErrorCode.INTELERR;
                    break;
            }
        }
        logger.info("updateCorp Leave");
        return error;
    }

//    //更新groupdn的member属性
//    private ErrorCode updateGroupMemeber(String userdn,String groupdn,AttributeModificationType op)
//    {
//        return bookDao.updateEntry(groupdn,null,
//                new AttributeModification[]{
//                        new AttributeModification(op,new LdapAttribute("member",userdn))
//                })? ErrorCode.OK:ErrorCode.INTELERR;
//    }

//    //更新groupdn的member属性
//    private ErrorCode updateGroupMemeberEx(String userdn,List<String> groupdns,AttributeModificationType op)
//    {
//        logger.info("updateGroupMemeberEx enter");
//        ErrorCode error = ErrorCode.OK;
//        List<String> tmp = new ArrayList<String>();
//        LdapAttribute attr = new LdapAttribute("member",userdn);
//        AttributeModification [] mods = new AttributeModification[]{new AttributeModification(op,attr)};
//        AttributeModificationType rop =null;
//        if(op==AttributeModificationType.ADD)
//        {
//            rop = AttributeModificationType.REMOVE;
//        }else if(op == AttributeModificationType.REMOVE)
//        {
//            rop = AttributeModificationType.ADD;
//        }else
//        {
//            error = ErrorCode.INVALIDARG;
//        }
//        if(error == ErrorCode.OK)
//        {
//            int length = groupdns.size();
//            int i=0;
//            for (;i<length-1;i++
//                    ) {
//                String groupdn = groupdns.get(i);
//                if(groupdn!=null && !groupdn.isEmpty())
//                {
//                    error = bookDao.updateEntry(groupdn,null,mods)?ErrorCode.OK:ErrorCode.INTELERR;
//                    if(error!=ErrorCode.OK)
//                    {
//                        break;
//                    }
//                }
//            }
//
//            //回滚
//            if(error!=ErrorCode.OK)
//            {
//                AttributeModification [] rmods = new AttributeModification[]{new AttributeModification(rop,attr)};
//                for (int j=0;j<i;j++)
//                {
//                    String groupdn = groupdns.get(i);
//                    if(groupdn!=null && !groupdn.isEmpty())
//                    {
//                        error = bookDao.updateEntry(groupdn,null,rmods)?ErrorCode.OK:ErrorCode.INTELERR;
//                    }
//                }
//            }
//        }
//        return error;
//    }

    /**
     * 添加用户
     * @param user
     * @return
     */
    public ErrorCode addUser(UserEntry user)
    {
        logger.info("addUser Enter");

        ErrorCode error = ErrorCode.OK;
        try {
            FilterItem filter = new FilterItem();
            filter.setOp("|");
            filter.getFilters().add(filter.new Item("labeledURI",user.getUserId(),"equal"));
            filter.getFilters().add(filter.new Item("mail",user.getEmail(),"equal"));
            filter.getFilters().add(filter.new Item("mobile",user.getMobile(),"equal"));
            Map<String,Object> data = queryUser(filter,null,1,1,null);
            if(data!=null)
            {
                if((Long)data.get("total")>0)
                {
                    error = ErrorCode.DUPLICATE;
                }else{
                    Long nextid = Long.parseLong(bookDao.nextId());
                    if(nextid.longValue()!=0l)
                    {
                        user.setUid(nextid);
                        //拼接用户dn.
                        String basedn = user.getDn();
                        user.setDn(String.format("labeledURI=%s,ou=people,%s",user.getUserId(),user.getDn()));
                        LdapEntry newEntry = new LdapEntry();
                        DefaultLdapEntryMapper<UserEntry> mapper = new DefaultLdapEntryMapper<UserEntry>();
                        mapper.map(user,newEntry);
                        error = bookDao.addEntry(newEntry)?ErrorCode.OK : ErrorCode.INTELERR;
                        //将用户添加到部门的membert属性
//                        if(error == ErrorCode.OK && user.getDepts()!=null)
//                        {
//                            for (NodeFullName dept:user.getDepts()
//                                    ) {
//                                updateGroupMemeber(user.getDn(),ToolUntil.fullName2dn(dept.getName(),basedn),AttributeModificationType.ADD);
//                            }
//                        }
                    }else
                    {
                        error = ErrorCode.INTELERR;
                    }
                }
            }else
            {
                error = ErrorCode.INTELERR;
            }

        }catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
            error = ErrorCode.INTELERR;
        }

        logger.info("addUser Enter");
        return error;
    }

    /**
     * 删除用户
     * @param uid
     * @return
     */
    public ErrorCode delUser(Long uid)
    {
        logger.info("delUser Enter");
        ErrorCode error = ErrorCode.OK;
        LdapEntry entry = getEntry(uid);
        DefaultLdapEntryMapper<UserEntry> mapper = new DefaultLdapEntryMapper<UserEntry>();
        UserEntry user = new UserEntry();
        if(entry!=null)
        {
            mapper.map(entry,user);
//            error = bookDao.delEntry(entry.getDn())?ErrorCode.OK:ErrorCode.INTELERR;
            error = bookDao.delEntryEx(entry.getDn(),uid) == ResultCode.SUCCESS?ErrorCode.OK:ErrorCode.INTELERR;
//            if(error==ErrorCode.OK)
//            {
//                //将用户的dn从其所属部门的member属性中移除
//                String corpdn = ToolUntil.dn2Corpdn(user.getDn());
//                if(user.getDeptIds()!=null)
//                {
//                    for (String id:user.getDeptIds()
//                         ) {
//                        String deptdn = getDnFromUid(Long.parseLong(id));
//                        error = updateGroupMemeber(user.getDn(),deptdn,AttributeModificationType.REMOVE);
//                        if(error != ErrorCode.OK)
//                        {
//                            logger.warn("del user failed");
//                            //需要回滚，待实现
//                            break;
//                        }
//                    }
//                }
//            }
        }else
        {
            error = ErrorCode.NOTFOUD;
        }
        logger.info("delUser Leave");
        return error;
    }

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    public ErrorCode updateUser(UserEntry user)
    {
        logger.info("updateUser Enter");
        ErrorCode error=ErrorCode.OK;
        boolean needUpdateDept = false;
        //检查登入名，邮箱地址，手机号是否已经存在
        FilterItem filter = new FilterItem();
        filter.setOp("|");
        filter.getFilters().add(filter.new Item("uid",user.getUid().toString(),"equal"));
        filter.getFilters().add(filter.new Item("labeledURI",user.getUserId(),"equal"));
        filter.getFilters().add(filter.new Item("mail",user.getEmail(),"equal"));
        filter.getFilters().add(filter.new Item("mobile",user.getMobile(),"equal"));
        Map<String,Object> data = queryUser(filter,null,1,2,"");
        UserEntry oldentry=null;
        if(data !=null)
        {
            if((Long)data.get("total")>1)
            {
                //登入名或邮箱地址或手机号已存在
                error = ErrorCode.DUPLICATE;
            }else if((Long)data.get("total")<1)
            {
                //要更新的用户不存在
                error = ErrorCode.NOTFOUD;
            }else
            {
                List<UserEntry> users = (List<UserEntry>)data.get("recodes");
                oldentry = users.get(0);
                if(oldentry.getUid().longValue()!=user.getUid().longValue())
                {
                    //要更新的用户不存在
                    error = ErrorCode.NOTFOUD;
                }
            }
        }else
        {
            //访问ldap异常
            error = ErrorCode.INTELERR;
        }

        if(error == ErrorCode.OK)
        {
            //更新名字的同时更新名字拼音
            if(user.getName()!=null)
            {
                if(oldentry.getName()==null || user.getName().toString()!=oldentry.getName().toString())
                {
                    user.setPingyin(ToolUntil.getPingYin(user.getName()));
                }
            }
            LdapEntry newEntry = new LdapEntry();
            DefaultLdapEntryMapper<UserEntry> mapper = new DefaultLdapEntryMapper<UserEntry>();
            mapper.map(user,newEntry);
            String newdn=null;
            List<AttributeModification> list = new ArrayList<AttributeModification>();
            for (LdapAttribute attr: newEntry.getAttributes()
                    ) {
                if(    attr.getName().equals("uid")
                        || attr.getName().equals("objectClass")
                        || attr.getName().equals("o"))
                {
                    continue;
                }
                //修改登入名，要更改dn
                if(attr.getName().equals("labeledURI") && !attr.getStringValue().equals(oldentry.getUserId()))
                {
                    String prefix = "labeledURI=";
                    String basedn = oldentry.getDn().substring(oldentry.getUserId().length()+prefix.length()-1);
                    newdn = String.format("%s%s,%s",prefix,attr.getStringValue(),basedn);
                    user.setDn(newdn);
                    needUpdateDept=true;
                }else
                {
                    user.setDn(oldentry.getDn());
                }

                if(attr.getName().equals("displayName"))
                {
                    //更新为空值，将原有的数据删除
                    if(attr.getStringValue().isEmpty())
                    {
                        if(oldentry.getName()!=null && !oldentry.getName().isEmpty())
                        {
                            list.add(new AttributeModification(AttributeModificationType.REMOVE,new LdapAttribute("displayName",oldentry.getName())));
                        }
                        continue;
                    }
                }
                if(attr.getName().equals("givenName"))
                {
                    //更新为空值，将原有的数据删除
                    if(attr.getStringValue().isEmpty())
                    {
                        if(oldentry.getPingyin()!=null && !oldentry.getPingyin().isEmpty())
                        {
                            list.add(new AttributeModification(AttributeModificationType.REMOVE,new LdapAttribute("givenName",oldentry.getPingyin())));
                        }
                        continue;
                    }
                }
                if(attr.getName().equals("mobile"))
                {
                    //更新为空值，将原有的数据删除
                    if(attr.getStringValue().isEmpty())
                    {
                        if(oldentry.getMobile()!=null && !oldentry.getMobile().isEmpty())
                        {
                            list.add(new AttributeModification(AttributeModificationType.REMOVE,new LdapAttribute("mobile",oldentry.getMobile())));
                        }
                        continue;
                    }
                }
                if(attr.getName().equals("departmentNumber"))
                {
                    needUpdateDept = true;
                }
                list.add(new AttributeModification(AttributeModificationType.REPLACE,attr));
            }
//            error = bookDao.updateEntry(oldentry.getDn(),user.getDn(),list.toArray(new AttributeModification[0]))?ErrorCode.OK:ErrorCode.INTELERR;
            ResultCode code = bookDao.updateEntry(oldentry.getDn(),user.getDn(),list.toArray(new AttributeModification[0]));
            switch (code)
            {
                case SUCCESS:
                    error = ErrorCode.OK;
                    break;
                default:
                    error = ErrorCode.INTELERR;
                    break;
            }

            //更新用户所在部门的membert属性
//            if(error==ErrorCode.OK && needUpdateDept)
//            {
//                String olddn = oldentry.getDn();
//                String corpdn = ToolUntil.dn2Corpdn(olddn);
//                //将该用户原来的dn从原来所在部门的member属性中移除
//                if(oldentry.getDeptIds()!=null)
//                {
//                    for (String id:oldentry.getDeptIds()
//                            ) {
//                        String deptdn = getDnFromUid(Long.parseLong(id));
//                        error = updateGroupMemeber(oldentry.getDn(),deptdn,AttributeModificationType.REMOVE);
//                        if(error!=ErrorCode.OK)
//                        {
//                            logger.info("update user department faile");
//                            //需要回滚，待实现
//                            break;
//                        }
//                    }
//                }
//                //将该用户新的dn添加到新的部门的member属性中
//                if(error==ErrorCode.OK && user.getDeptIds()!=null)
//                {
//                    for (String id:user.getDeptIds()
//                            ) {
//                        String deptdn = getDnFromUid(Long.parseLong(id));
//                        error = updateGroupMemeber(user.getDn(),deptdn,AttributeModificationType.ADD);
//                        if(error!=ErrorCode.OK)
//                        {
//                            logger.info("update user department faile");
//                            //需要回滚，待实现
//                            break;
//                        }
//                    }
//                }
//            }
        }
        logger.info("updateUser Leave");
        return error;
    }

    /**
     * 添加部门
     * @param group
     * @return
     */
    public ErrorCode addGroup(GroupEntry group)
    {
        logger.info("addGroup Enter");

        ErrorCode error = ErrorCode.OK;
        String parentdn="";
        try{
            //根据父部门id获取父部门dn
            if(group.getParentId().longValue()==0)
            {
                parentdn = String.format("ou=groups,%s",getEntry(group.getCorpId()).getDn());
            }else
            {
                parentdn = getEntry(group.getParentId()).getDn();
            }
            //检查名称是否重复
            if(group.getName()!=null && !group.getName().isEmpty())
            {
                FilterItem filter = new FilterItem();
                filter.setOp("|");
                filter.getFilters().add(filter.new Item("cn",group.getName(),"equal"));
                Map<String,Object> data = queryGroup(parentdn,filter,null,SearchScope.ONELEVEL,1,1);
                if(data!=null)
                {
                    if((Long)data.get("total")>0)
                    {
                        error = ErrorCode.DUPLICATE;
                    }
                }else
                {
                    error = ErrorCode.INTELERR;
                }

            }else{
                error = ErrorCode.INVALIDARG;
                logger.info("group name is empty");
            }

            if(error == ErrorCode.OK) {
                Long nextid =Long.parseLong(bookDao.nextId());
                if(nextid.longValue()!=0l) {
                    group.setDeptId(nextid);
                }else {
                    error = ErrorCode.INTELERR;
                }
            }
            if(error == ErrorCode.OK){
                group.setDn(String.format("cn=%s,%s",group.getName(),parentdn));
                group.setPingyin(ToolUntil.getPingYin(group.getName()));

                //member为必要属性
                List<String> ldn = new ArrayList<String>();
                ldn.add("");
                group.setMember(ldn);

//                if(group.getMember()==null)
//                {
//                    ldn.add("");
//                    group.setMember(ldn);
//                }else
//                {
//                    for (String e:group.getMember()
//                            ) {
//                        LdapEntry tmp = getEntry(Long.parseLong(e));
//                        if(tmp!=null)
//                        {
//                            ldn.add(tmp.getDn());
//                        }
//                    }
//                }
                LdapEntry newEntry = new LdapEntry();
                DefaultLdapEntryMapper<GroupEntry> mapper = new DefaultLdapEntryMapper<GroupEntry>();
                mapper.map(group,newEntry);

                error = bookDao.addEntry(newEntry)?ErrorCode.OK : ErrorCode.INTELERR;
            }
        }catch (Exception e)
        {
            error = ErrorCode.INTELERR;
            e.printStackTrace();
            logger.error(e.getMessage());
        }

        logger.info("addGroup Enter");
        return error;
    }

    /**
     * 更新部门
     * @param group
     * @param op
     * @return
     */
    public ErrorCode updateGroup(GroupEntry group , String op)
    {
        logger.info("updateGroup Enter");
        ErrorCode error = ErrorCode.OK;
        String basedn = null;
        String newdn = null;
        boolean modifydn = false;
        boolean modifyname=false;
        boolean modifmember=false;
        //更新所属父部门需要更新dn
        if(group.getParentId()!=null)
        {
            LdapEntry parent = null;
            if(group.getParentId()==0l)
            {
                parent  = getEntry(group.getCorpId());
                if(parent!=null)
                {
                    basedn = String.format("ou=groups,%s",parent.getDn());
                    modifydn = true;
                }
            }else
            {
                parent = getEntry(group.getParentId());
                if(parent!=null)
                {
                    basedn = parent.getDn();
                    modifydn = true;
                }
            }

        }
        LdapEntry oldEntry = getEntry(group.getDeptId());
        DefaultLdapEntryMapper<GroupEntry> mapper = new DefaultLdapEntryMapper<GroupEntry>();
        GroupEntry oldGroup = new GroupEntry();
        mapper.map(oldEntry,oldGroup);
        //更新组名需要更新dn
        String name=group.getName();
        if(name!=null && !oldGroup.getName().equals(name)) {
            modifydn = true;
            modifyname = true;
        }
        //拼接新的dn
        if(modifydn) {
            if(basedn==null) {
                String olddn= oldEntry.getDn();
                basedn = olddn.substring(olddn.indexOf(',')+1);
            }
            if(name==null || name.isEmpty()) {
                name = oldGroup.getName();
            }
            newdn = String.format("cn=%s,%s",name,basedn);
            group.setDn(newdn);
        }else
        {
            group.setDn(oldGroup.getDn());
        }
        //检查新的部门名是否已存在
        if(modifyname)
        {
            FilterItem filter = new FilterItem();
            filter.setOp("&");
            filter.getFilters().add(filter.new Item("cn",group.getName(),"equal"));
            Map<String,Object> data = queryGroup(basedn,filter,null,SearchScope.ONELEVEL,1,0);
            if(data!=null)
            {
                if((Long)data.get("total")>0)
                {
                    error = ErrorCode.DUPLICATE;
                }
            }else{
                error = ErrorCode.INTELERR;
            }

        }

        if(error == ErrorCode.OK)
        {
            //更新部门名的同时需要更新名称拼音
            if(modifyname)
            {
                group.setPingyin(ToolUntil.getPingYin(group.getName()));
            }

            //将member字段id数组转成dn数组
//            List<String> dns = new ArrayList<String>();
//            if(group.getMember()!=null)
//            {
//                for (String e:group.getMember()
//                        ) {
//                    LdapEntry tmp = getEntry(Long.parseLong(e));
//                    if(tmp!=null)
//                    {
//                        dns.add(tmp.getDn());
//                    }
//                }
//            }
//            if(dns.size()>0)
//            {
//                modifmember = true;
//                group.setMember(dns);
//            }
            if(group.getMember()!=null && group.getMember().size()>0)
            {
                modifmember = true;
            }

            LdapEntry newEntry = new LdapEntry();
            mapper.map(group,newEntry);

            List<AttributeModification> attrs = new ArrayList<AttributeModification>();
//            if(modifmember)
//            {
//                if(op.equals("add"))
//                {
//                    attrs.add(new AttributeModification(AttributeModificationType.ADD,newEntry.getAttribute("member")));
//                }else if(op.equals("remove"))
//                {
//                    attrs.add(new AttributeModification(AttributeModificationType.REMOVE,newEntry.getAttribute("member")));
//                }else
//                {
//                    attrs.add(new AttributeModification(AttributeModificationType.REPLACE,newEntry.getAttribute("member")));
//                }
//            }
            if(modifyname)
            {
                attrs.add(new AttributeModification(AttributeModificationType.REPLACE,newEntry.getAttribute("cn")));
                attrs.add(new AttributeModification(AttributeModificationType.REPLACE,newEntry.getAttribute("description")));
            }
            if(attrs.size()>0 ) {
                error =  bookDao.updateEntry(oldGroup.getDn(),group.getDn(),attrs.toArray(new AttributeModification[0]))==ResultCode.SUCCESS? ErrorCode.OK:ErrorCode.INTELERR;
            }

                //更新过的部门成员的departmentNumber需要修改
            if(error==ErrorCode.OK && modifmember)
            {

                //将新的部门名称添加到(移除出)用户的departmentNumber
                AttributeModification mod = new AttributeModification(op.equals("remove")?AttributeModificationType.REMOVE:AttributeModificationType.ADD,
                        new LdapAttribute("departmentNumber",group.getDeptId().toString()));
                List<String> retmembers= new ArrayList<String>();
                AttributeModification[] mods = new AttributeModification[]{mod};
                for (String uid:group.getMember()
                        ) {
                    String dn = getDnFromUid(Long.parseLong(uid));
                    ResultCode code = bookDao.updateEntry(dn,null,mods);
                    switch (code)
                    {
                        case SUCCESS:
                            retmembers.add(uid);
                            break;
                        case ATTRIBUTE_OR_VALUE_EXISTS:
                            error = ErrorCode.DEPTDUPLUSER;
                            break;
                        case NO_SUCH_ATTRIBUTE:
                            error = ErrorCode.DEPTDUPLUSER;
                            break;
                        default:
                            error = ErrorCode.INTELERR;
                            break;
                    }
                    if(error == ErrorCode.INTELERR)
                    {
                        break;
                    }
                }
                group.setMember(retmembers);

                if(error!=ErrorCode.INTELERR && op.equals("replace"))
                {
                    //将部门id从被替换的用户的departmentNumber中移除
                    if(oldGroup.getMember()!=null)
                    {
                        AttributeModification tmpmod = new AttributeModification(AttributeModificationType.REMOVE,
                                new LdapAttribute("departmentNumber",group.getDeptId().toString()));
                        AttributeModification[] tmpmods = new AttributeModification[]{tmpmod};
                        for (String uid: oldGroup.getMember())
                        {
                            String dn = getDnFromUid(Long.parseLong(uid));
                            if(dn!=null && !dn.isEmpty())
                            {
                                if(ResultCode.SUCCESS!=bookDao.updateEntry(dn,null,tmpmods));
                            }
                        }

                    }
                }


            }

        }
        logger.info("updateGroup Leave");
        return error;
    }

    /**
     * 删除部门及其子部门，其下用户从部门中移除
     * @param groupid
     * @return
     */
    public ErrorCode delGroup(Long groupid)
    {
        logger.info("delGroup Enter");
        ErrorCode error = null;
        LdapEntry entry = getEntry(groupid);
        if(entry!=null)
        {
            String dn = entry.getDn();
            if (!dn.isEmpty()) {
                //error = bookDao.delEntryRecursive(dn,this.callBack) ? ErrorCode.OK : ErrorCode.INTELERR;
                bookDao.delUserGroupRef(dn);
                error = bookDao.delEntryEx(dn,groupid) == ResultCode.SUCCESS? ErrorCode.OK : ErrorCode.INTELERR;
            } else {
                error = ErrorCode.NOTFOUD;
            }
        }else {
            error = ErrorCode.NOTFOUD;
        }

        logger.info("delGroup Leave");
        return error;
    }


    /**
     * 查询企业
     * 默认按域名排序
     * @param filter
     * @param sortkeys
     * @param page
     * @param pagesize
     * @return
     */
    public Map<String,Object> queryCorp(FilterItem filter,SortKey sortkeys[],int page,int pagesize)
    {
        logger.info("queryCorp Enter");
        if(sortkeys==null || sortkeys.length==0)
        {
            //默认按域名排序
            sortkeys = new SortKey[]{new SortKey("o")};
        }
        FilterItem f = new FilterItem();
        f.setOp("&");
        f.getFilters().add(f.new Item("objectClass","organization","equal"));
        f.getFilters().add(filter);
        Map<String,Object> tmp = bookDao.Search(bookDao.getBaseDn(),new SearchFilter(f.toString()),sortkeys,SearchScope.ONELEVEL,null,page,pagesize,true);
        logger.info("queryCorp Leave");
        return tmp;
    }

    /**
     * 查询部门成员
     * @return
     */
    public Map<String ,Object> queryGroupMembers(Long groupid,FilterItem filter,SortKey sortkeys[],SearchScope scope,int page,int pagesize)
    {
        logger.info("queryGroup Enter");
        LdapEntry entry = getEntry(groupid);
        DefaultLdapEntryMapper<GroupEntry> mapper = new DefaultLdapEntryMapper<GroupEntry>();
        GroupEntry group = new GroupEntry();
        mapper.map(entry,group);
        Map<String,Object> data=new HashMap<String, Object>();
        long total = 0l;
        List<UserEntry> users = new ArrayList<UserEntry>();
        if(group.getMember()!=null)
        {
            for(Iterator<String> it = group.getMember().iterator(); it.hasNext();){
                String s = it.next();
                if(s.isEmpty()){
                    it.remove();
                }
            }
            total = group.getMember().size();
            DefaultLdapEntryMapper<UserEntry> mapperu = new DefaultLdapEntryMapper<UserEntry>();
            List<String> members = group.getMember();
            int start = (page-1)*pagesize;
            int end = start+pagesize-1;
            for (int i=start;i<=end&&i<members.size();i++)
            {
                LdapEntry tmp = bookDao.getEntry(members.get(i));
                UserEntry user = new UserEntry();
                mapperu.map(tmp,user);
                users.add(user);
            }
        }
        data.put("total",total);
        data.put("recodes",users);

        return data;
    }

    /**
     * 查询部门成员
     * @return
     */
    public Map<String ,Object> queryGroupMembersEx(Long groupid,FilterItem filter,SortKey sortkeys[],SearchScope scope,int page,int pagesize)
    {
        logger.info("queryGroupMembersEx Enter");
        Map<String,Object> data = null;
        FilterItem tmpfilter = new FilterItem();
        tmpfilter.setOp("&");
        tmpfilter.getFilters().add(tmpfilter.new Item("departmentNumber",groupid.toString(),"equal"));
        data = queryUser(tmpfilter,sortkeys,page,pagesize,null);
        logger.info("queryGroupMembersEx Leave");
        return data;
    }


    /**
     * 查询部门所有成员(包括子孙部门中的成员)
     */
    public Map<String,Object> queryGroupsAllMemebers(Long corpid,Long groupid,SortKey sortKey[],int page,int pagesize)
    {
        logger.info("queryGroupsAllMemebers Enter");
        Map<String,Object> data = queryGroupEx(corpid,groupid,null,SearchScope.SUBTREE,1,0);
        List<GroupEntry> groups = (List<GroupEntry>)data.get("recodes");
        Map<String,Object> users = new HashMap<String, Object>();
        if(groups!=null && groups.size()>0)
        {
            FilterItem tmpfilter = new FilterItem();
            tmpfilter.setOp("|");
            for (GroupEntry g:groups
                 ) {
                if(g!=null)
                {
                    tmpfilter.getFilters().add(tmpfilter.new Item("departmentNumber",g.getDeptId().toString(),"equal"));
                }
            }
            users = queryUser(tmpfilter,sortKey,page,pagesize,null);
        }
        logger.info("queryGroupsAllMemebers Leave");
        return users;
    }

    /**
     * 查询部门
     * @param baseDn
     * @param filter
     * @param sortkeys
     * @param scope
     * @param page
     * @param pagesize
     * @return
     */
    public Map<String ,Object> queryGroup(String baseDn,FilterItem filter,SortKey sortkeys[],SearchScope scope,int page,int pagesize)
    {
        logger.info("queryGroup Enter");
        if(sortkeys==null || sortkeys.length==0)
        {
            //默认按组名拼音排序
            sortkeys = new SortKey[]{new SortKey("description")};
        }
        FilterItem f = new FilterItem();
        f.setOp("&");
        f.getFilters().add(f.new Item("objectClass","groupOfNames","equal"));
        if(filter!=null)
        {
            f.getFilters().add(filter);
        }
        Map<String,Object> data=null;
        data = bookDao.Search(baseDn,new SearchFilter(f.toString()),sortkeys,scope,null,page,pagesize,true);

        if(data!=null)
        {
            List<GroupEntry> groups = (List<GroupEntry>)data.get("recodes");
            for (GroupEntry g:groups
                 ) {
                Map<String,Object> tmp =  queryGroupMembersEx(g.getDeptId(),null,null,null,page,pagesize);
                if(tmp!=null)
                {
                    g.setUcount((Long)tmp.get("total"));
                }else
                {
                    g.setUcount(0l);
                }
            }
        }
        logger.info("queryGroup Leave");
        return data;
    }

    /**
     * 用id查询部门
     * @param corpid
     * @param groupid
     * @param sortkeys
     * @param page
     * @param pagesize
     * @return
     */
    public Map<String,Object> queryGroupEx(Long corpid,Long groupid,SortKey sortkeys[],SearchScope scope,int page,int pagesize)
    {
        logger.info("queryGroupEx Enter");
        //groupid等于0时查询部门
        Long id = groupid==0l? corpid:groupid;
        LdapEntry entry = getEntry(id);
        Map<String,Object> data = null;
        if(entry!=null)
        {
            String dn = entry.getDn();
            //根部门位于"ou=groups,..."条目
            if(groupid==0l)
            {
                dn = String.format("ou=groups,%s",dn);
            }
            data = queryGroup(dn,null,sortkeys,scope,page,pagesize);
        }
        logger.info("queryGroupEx Leave");
        return data;
    }

    public Map<String ,Object> queryUser(FilterItem filter, SortKey sortkeys[], int page, int pagesize, String domain )
    {
        logger.info("queryUser Enter");
        if(sortkeys==null || sortkeys.length==0)
        {
            //默认按名字拼音排序
//            sortkeys = new SortKey[]{new SortKey("labeledURI"),
//                    new SortKey("givenName")};
            sortkeys = new SortKey[]{new SortKey("labeledURI")};
        }
        FilterItem f = new FilterItem();
        f.setOp("&");
        f.getFilters().add(f.new Item("objectClass","inetOrgPerson","equal"));
        f.getFilters().add(filter);
        Map<String,Object> tmp=null;
        if(domain == null || domain.equals(""))
        {
            tmp = bookDao.Search(bookDao.getBaseDn(),new SearchFilter(f.toString()),sortkeys, SearchScope.SUBTREE,null,page,pagesize,true);
        }else
        {
            tmp = bookDao.Search(String.format("ou=people,o=%s,%s",domain,bookDao.getBaseDn()),new SearchFilter(f.toString()),sortkeys, SearchScope.ONELEVEL,null,page,pagesize,true);
        }

        logger.info("queryUser Leave");
        return tmp;
    }

    public Long getGroupIDFromDn(String dn)
    {

        LdapEntry entry = bookDao.getEntry(dn);
        DefaultLdapEntryMapper<GroupEntry> mapper = new DefaultLdapEntryMapper<GroupEntry>();
        GroupEntry group = new GroupEntry();
        mapper.map(entry,group);
        return group.getDeptId();
    }

    /**
     * 获取指定条目的uid
     * @param dn
     * @return
     */
    public Long getUidFormDn(String dn)
    {
        LdapEntry entry = bookDao.getEntry(dn);
        if(entry!=null)
        {
            String uid = entry.getAttribute("uid").getStringValue();
            return Long.parseLong(uid);
        }
        return null;
    }

    /**
     * 使用id获取条目的dn
     * @param uid
     * @return
     */
    public String getDnFromUid(Long uid)
    {
        LdapEntry entry = getEntry(uid);
        if(entry!=null)
        {
            return entry.getDn();
        }
        return null;
    }

    public boolean Auth(Long uid,String oldps)
    {
        logger.info("Auth Enter");
        boolean ret =false;
        LdapEntry entry = getEntry(uid);
        if(entry!=null) {
            String ldappw = new String(entry.getAttribute("userPassword").getBinaryValue());
            if (ldappw != null) {
                ret = ToolUntil.verifyMD5(ldappw, oldps);
            }
        }
        logger.info("Auth Leave");
        return ret;
    }

    public Object getEntry(String dn)
    {
        LdapEntry entry = bookDao.getEntry(dn);
        if(entry!=null)
        {
            return bookDao.RecodeDao(entry);
        }
        return null;
    }


}
