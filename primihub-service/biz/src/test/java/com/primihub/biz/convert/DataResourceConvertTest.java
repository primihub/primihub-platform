package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataResourceFieldReq;
import com.primihub.biz.entity.data.req.DataResourceReq;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.sys.po.SysFile;
import com.primihub.biz.entity.sys.po.SysUser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class DataResourceConvertTest {

    @Test
    void dataResourceTagPoConvertVo_shouldMap() {
        DataResourceTag tag = new DataResourceTag();
        tag.setTagId(1L);
        tag.setTagName("testTag");

        ResourceTagVo result = DataResourceConvert.dataResourceTagPoConvertVo(tag);

        assertThat(result.getTagId()).isEqualTo(1L);
        assertThat(result.getTagName()).isEqualTo("testTag");
    }

    @Test
    void dataResourceReqConvertPo_shouldMapWithFile() {
        DataResourceReq req = new DataResourceReq();
        req.setResourceId(1L);
        req.setResourceName("res");
        req.setResourceDesc("desc");
        req.setResourceAuthType(1);
        req.setResourceSource(1);
        req.setPublicOrganId("organ1");

        SysFile sysFile = new SysFile();
        sysFile.setFileId(100L);
        sysFile.setFileSize(1024L);
        sysFile.setFileSuffix(".csv");
        sysFile.setFileUrl("/path/to/file");

        DataResource result = DataResourceConvert.dataResourceReqConvertPo(req, 42L, 1L, sysFile);

        assertThat(result.getResourceId()).isEqualTo(1L);
        assertThat(result.getResourceName()).isEqualTo("res");
        assertThat(result.getUserId()).isEqualTo(42L);
        assertThat(result.getOrganId()).isEqualTo(1L);
        assertThat(result.getFileId()).isEqualTo(100L);
        assertThat(result.getFileSize()).isEqualTo(1024);
        assertThat(result.getFileSuffix()).isEqualTo(".csv");
        assertThat(result.getUrl()).isEqualTo("/path/to/file");
    }

    @Test
    void dataResourceReqConvertPo_shouldMapWithoutFile() {
        DataResourceReq req = new DataResourceReq();
        req.setResourceId(1L);
        req.setResourceName("res");

        DataResource result = DataResourceConvert.dataResourceReqConvertPo(req, 42L, 1L);

        assertThat(result.getFileId()).isZero();
        assertThat(result.getFileSize()).isZero();
        assertThat(result.getFileSuffix()).isEmpty();
        assertThat(result.getFileColumns()).isZero();
        assertThat(result.getFileRows()).isZero();
        assertThat(result.getDbId()).isZero();
        assertThat(result.getUrl()).isEmpty();
        assertThat(result.getResourceState()).isZero();
    }

    @Test
    void dataResourcePoConvertVo_shouldMap() {
        DataResource po = new DataResource();
        po.setResourceId(1L);
        po.setResourceName("res");
        po.setResourceDesc("desc");
        po.setResourceAuthType(1);
        po.setResourceSource(1);
        po.setFileHandleField("col1,col2");
        po.setResourceState(0);
        po.setResourceHashCode("abc123");

        DataResourceVo result = DataResourceConvert.dataResourcePoConvertVo(po);

        assertThat(result.getResourceId()).isEqualTo(1L);
        assertThat(result.getResourceName()).isEqualTo("res");
        assertThat(result.getFileHandleField()).containsExactly("col1", "col2");
        assertThat(result.getResourceState()).isZero();
        assertThat(result.getResourceHashCode()).isEqualTo("abc123");
    }

    @Test
    void dataResourcePoConvertVo_shouldHandleBlankHandleField() {
        DataResource po = new DataResource();
        po.setFileHandleField(null);

        DataResourceVo result = DataResourceConvert.dataResourcePoConvertVo(po);

        assertThat(result.getFileHandleField()).isEmpty();
    }

    @Test
    void dataResourcePoConvertVo_shouldMapWithOrganInfo() {
        DataResource po = new DataResource();
        po.setResourceName("res");

        DataResourceVo result = DataResourceConvert.dataResourcePoConvertVo(po, "fusion1", "OrganName");

        assertThat(result.getOrganFusionId()).isEqualTo("fusion1");
        assertThat(result.getOrganName()).isEqualTo("OrganName");
    }

    @Test
    void dataFileFieldPoConvertVo_shouldMapFlags() {
        DataFileField field = new DataFileField();
        field.setFieldId(1L);
        field.setFieldName("age");
        field.setFieldAs("年龄");
        field.setFieldType(2);
        field.setFieldDesc("年龄字段");
        field.setRelevance(1);
        field.setGrouping(0);
        field.setProtectionStatus(1);

        DataFileFieldVo result = DataResourceConvert.DataFileFieldPoConvertVo(field);

        assertThat(result.getFieldId()).isEqualTo(1L);
        assertThat(result.getFieldName()).isEqualTo("age");
        assertThat(result.getFieldAs()).isEqualTo("年龄");
        assertThat(result.getRelevance()).isTrue();
        assertThat(result.getGrouping()).isFalse();
        assertThat(result.getProtectionStatus()).isTrue();
    }

    @Test
    void dataFileFieldReqConvertPo_shouldMap() {
        DataResourceFieldReq req = new DataResourceFieldReq();
        req.setFieldName("name");
        req.setFieldAs("姓名");
        req.setFieldDesc("desc");
        req.setRelevance(1);
        req.setGrouping(0);
        req.setProtectionStatus(1);

        DataFileField result = DataResourceConvert.DataFileFieldReqConvertPo(req, null);

        assertThat(result.getFieldName()).isEqualTo("name");
        assertThat(result.getRelevance()).isEqualTo(1);
        assertThat(result.getGrouping()).isZero();
        assertThat(result.getProtectionStatus()).isEqualTo(1);
    }

    @Test
    void dataDerivationResourcePoConvertDataVo_shouldMap() {
        DataDerivationResourceVo src = new DataDerivationResourceVo();
        src.setId(1L);
        src.setResourceName("derived");

        DataDerivationResourceDataVo result = DataResourceConvert.dataDerivationResourcePoConvertDataVo(src);

        assertThat(result.getResourceId()).isEqualTo(1L);
        assertThat(result.getResourceName()).isEqualTo("derived");
    }

    @Test
    void getMapValue_shouldReturnDefault_whenMissing() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        String result = DataResourceConvert.getMapValue(map, "key", "def");

        assertThat(result).isEqualTo("def");
    }

    @Test
    void getMapValue_shouldReturnValue_whenPresent() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("key", "val");

        String result = DataResourceConvert.getMapValue(map, "key", "def");

        assertThat(result).isEqualTo("val");
    }

    @Test
    void resourceConvertSelectVo_shouldHandleNulls() {
        DataResource dr = new DataResource();
        dr.setResourceFusionId("f1");
        dr.setResourceName("res");

        ModelSelectResourceVo result = DataResourceConvert.resourceConvertSelectVo(dr);

        assertThat(result.getResourceId()).isEqualTo("f1");
        assertThat(result.getResourceContainsY()).isZero();
        assertThat(result.getResourceYRowsCount()).isZero();
        assertThat(result.getResourceYRatio()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void editResourceReqConvertPo_shouldUpdateNonNullFields() {
        DataResourceReq req = new DataResourceReq();
        req.setResourceName("new name");
        req.setResourceDesc("new desc");
        req.setResourceAuthType(2);
        req.setResourceSource(3);

        DataResource po = new DataResource();
        po.setResourceName("old name");

        DataResourceConvert.editResourceReqConvertPo(req, po);

        assertThat(po.getResourceName()).isEqualTo("new name");
        assertThat(po.getResourceDesc()).isEqualTo("new desc");
        assertThat(po.getResourceAuthType()).isEqualTo(2);
        assertThat(po.getResourceSource()).isEqualTo(3);
    }

    @Test
    void editResourceReqConvertPo_shouldNotUpdateBlankFields() {
        DataResourceReq req = new DataResourceReq();
        req.setResourceName("");

        DataResource po = new DataResource();
        po.setResourceName("keep");

        DataResourceConvert.editResourceReqConvertPo(req, po);

        assertThat(po.getResourceName()).isEqualTo("keep");
    }
}
