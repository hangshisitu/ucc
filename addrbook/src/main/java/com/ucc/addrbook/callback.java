package com.ucc.addrbook;

import org.ldaptive.LdapEntry;

/**
 * Created by Administrator on 2016/5/10.
 */
public interface callback {
   boolean preDel(Object obj,LdapEntry entry);
}
