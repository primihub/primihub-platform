package com.primihub.entity;

public class DataSet {
    private String id;
    private String accessInfo;
    private String driver;
    private String address;
    private String vibility;
    private String available = "Available";
    private Integer holder = 0;

    public DataSet(String id, String accessInfo, String driver, String address, String vibility) {
        this.id = id;
        this.accessInfo = accessInfo;
        this.driver = driver;
        this.address = address;
        this.vibility = vibility;
        this.available = available;
    }

    public DataSet(String id, String accessInfo, String driver, String address, String vibility, Integer holder) {
        this.id = id;
        this.accessInfo = accessInfo;
        this.driver = driver;
        this.address = address;
        this.vibility = vibility;
        this.available = available;
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
