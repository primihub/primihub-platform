package com.primihub.biz.entity.sys.po;


public class DataSet {
    private String id;
    private String accessInfo;
    private String driver;
    private String address;
    private String visibility;
    private String available = "Available";
    private Integer holder = 0;
    private String fields;

    public DataSet(String id, String accessInfo, String driver, String address, String visibility) {
        this.id = id;
        this.accessInfo = accessInfo;
        this.driver = driver;
        this.address = address;
        this.visibility = visibility;
    }

    public DataSet(String id, String accessInfo, String driver, String address, String visibility, Integer holder) {
        this.id = id;
        this.accessInfo = accessInfo;
        this.driver = driver;
        this.address = address;
        this.visibility = visibility;
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
        if (this.accessInfo==null){
            return "";
        }
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

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    @Override
    public String toString() {
        return "DatSetEntity{" +
                "id='" + id + '\'' +
                ", accessInfo='" + accessInfo + '\'' +
                ", driver='" + driver + '\'' +
                ", address='" + address + '\'' +
                ", visibility='" + visibility + '\'' +
                ", available='" + available + '\'' +
                ", holder=" + holder +
                '}';
    }
}
