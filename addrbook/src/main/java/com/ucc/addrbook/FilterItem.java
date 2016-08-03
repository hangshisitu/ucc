package com.ucc.addrbook;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/3/23.
 */
public class FilterItem {

    public class Item{
        private String key="";
        private String value="";
        private String matchrule="like";

        public Item(String key,String value,String matchrule)
        {
            this.key=key;
            this.value=value;
            this.matchrule=matchrule;
        }

        public String toString()
        {
            if(value==null || key==null || value.equals("") || key.equals(""))
            {
                return "";
            }
            if(matchrule.equals("like"))
            {
                return String.format("(%s=*%s*)",key,value);
            }else
            {
                return String.format("(%s=%s)",key,value);
            }
        }
    }

    private List<Object> filters=new ArrayList<Object>();
    private String op="|";

    public FilterItem()
    {

    }
    public List<Object> getFilters() {
        return filters;
    }

    public void setFilters(List<Object> filters) {
        this.filters = filters;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public FilterItem(List<Object> filters, String op)
    {
        this.filters=filters;
        this.op=op;
    }

    public String toString()
    {
        String filter="("+op;
        for (Object f:filters
             ) {
            if(f!=null)
            {
                filter +=f.toString();
            }

        }
        filter +=")";
        if(filter.length()<=3)
        {
            return "";
        }
        return filter;
    }
}
