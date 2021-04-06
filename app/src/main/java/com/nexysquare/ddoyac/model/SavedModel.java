package com.nexysquare.ddoyac.model;

import java.util.Date;

public class SavedModel {

    private int id;
    private int priority;
    private String name;
    private Date created;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getCreated() {
        return created;
    }

    public int getPriority() {
        return priority;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
