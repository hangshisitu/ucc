package com.ucc.addrbook;

import org.ldaptive.*;
import org.ldaptive.beans.reflect.DefaultLdapEntryMapper;
import org.ldaptive.control.SortKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/5/10.
 */
//@Service("UpdateUserDept")
public class UpdateUserDept implements callback {
    public UpdateUserDept() {
    }

    //    @Autowired
//    private BookDao bookDao;
    public  boolean preDel(Object bookDao,LdapEntry entry)
    {
        boolean ret = true;
        if(entry!=null)
        {
            DefaultLdapEntryMapper<GroupEntry> mapper = new DefaultLdapEntryMapper<GroupEntry>();
            GroupEntry group = new GroupEntry();
            mapper.map(entry,group);
            BookDao bs = (BookDao)bookDao;
            String grpid = group.getDeptId().toString();

            SortKey sortkeys[] = new SortKey[]{new SortKey("labeledURI")};

            FilterItem f = new FilterItem();
            f.setOp("&");
            f.getFilters().add(f.new Item("objectClass","inetOrgPerson","equal"));
            f.getFilters().add(f.new Item("departmentNumber",grpid,"equal"));
            Map<String,Object> tmp=null;
            tmp = bs.Search(bs.getBaseDn(),new SearchFilter(f.toString()),sortkeys, SearchScope.SUBTREE,null,1,0,true);
            if(tmp!=null)
            {
                List<UserEntry> users = (List<UserEntry> )tmp.get("recodes");
                if(users!=null)
                {
                    for (UserEntry user:users
                            ) {
                        if(user!=null && !user.getDn().isEmpty())
                        {
                            LdapAttribute attr = new LdapAttribute("departmentNumber",grpid);
                            if(ResultCode.SUCCESS!=bs.updateEntry(user.getDn(),null,new AttributeModification[]{new AttributeModification(AttributeModificationType.REMOVE,attr)}))
                            {
                                ret = false;
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }
}
