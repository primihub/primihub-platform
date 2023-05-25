package com.primihub.biz.entity.data.dto;

import lombok.Data;

@Data
public class ModelOutputPathDto {
    private String hostLookupTable;
    private String guestLookupTable;
    private String guestModelFileName;
    private String hostModelFileName;
    private String predictFileName;
    private String indicatorFileName;
    private String modelRunZipFilePath;
    private String taskPath;
    private String taskNNType;

    public ModelOutputPathDto() {
    }

    public ModelOutputPathDto(String path) {
        this.taskPath = path;
        this.hostLookupTable = path + "/hostLookupTable";
        this.guestLookupTable = path + "/guestLookupTable";
        this.guestModelFileName = path + "/modelFileName.guest";
        this.hostModelFileName = path + "/modelFileName.host";
        this.predictFileName = path + "/predictFileName.csv";
        this.indicatorFileName = path + "/indicatorFileName.json";
        this.modelRunZipFilePath = path + ".zip";
    }
}
