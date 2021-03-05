package com.nexysquare.ddoyac.model;

import io.realm.RealmObject;

public class PillRealm extends RealmObject {

    private String name;
    private String descriptor;
    private int matched;

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public int getMatched() {
        return matched;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMatched(int matched) {
        this.matched = matched;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }
}
