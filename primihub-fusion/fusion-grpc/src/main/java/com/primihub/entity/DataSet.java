package com.primihub.entity;

import java.util.List;

public class DataSet {
    private String id;
    private String accessInfo;
    private String driver;
    private String address;
    private String vibility;
    private String available = "Available";
    private Integer holder = 0;
    private String fields;

    public DataSet(String id, String accessInfo, String driver, String address, String vibility) {
        this.id = id;
        this.accessInfo = accessInfo;
        this.driver = driver;
        this.address = address;
        this.vibility = vibility;
    }

    public DataSet(String id, String accessInfo, String driver, String address, String vibility, Integer holder) {
        this.id = id;
        this.accessInfo = accessInfo;
        this.driver = driver;
        this.address = address;
        this.vibility = vibility;
        this.holder = holder;
    }

    public DataSet() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccessInfo() {
        return accessInfo;
    }

    public void setAccessInfo(String accessInfo) {
        this.accessInfo = accessInfo;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVibility() {
        return vibility;
    }

    public void setVibility(String vibility) {
        this.vibility = vibility;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public Integer getHolder() {
        return holder;
    }

    public void setHolder(Integer holder) {
        this.holder = holder;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "DatSetEntity{" +
                "id='" + id + '\'' +
                ", accessInfo='" + accessInfo + '\'' +
                ", driver='" + driver + '\'' +
                ", address='" + address + '\'' +
                ", vibility='" + vibility + '\'' +
                ", available='" + available + '\'' +
                ", holder=" + holder +
                '}';
    }
}
