package com.ucc.addrbook;

/**
 * Created by Administrator on 2016/3/14.
 */
public class NodeFullName implements Comparable<NodeFullName>{

    private Long id;
    private String name;

    public NodeFullName(Long id,String fullname)
    {
        this.id=id;
        this.name = fullname;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compareTo(NodeFullName arg0) {
        return this.getId().compareTo(arg0.getId());
    }
}
