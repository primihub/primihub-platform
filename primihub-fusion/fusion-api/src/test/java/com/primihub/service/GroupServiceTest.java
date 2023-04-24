package com.primihub.service;

import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.repository.FusionRepository;
import com.primihub.repository.GroupRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class GroupServiceTest {
    @Autowired
    private GroupService groupService;
    @MockBean
    private GroupRepository groupRepository;
    @MockBean
    private FusionRepository fusionRepository;

    @TestConfiguration
    static class EmployeeServiceImplTestContextConfiguration {

        @Bean
        public GroupService employeeService() {
            return new GroupService();
        }
    }

    @Test
    public void testCreateGroup() {
        BaseResultEntity<?> result = groupService.createGroup("groupID", "groupName");
        assertThat(result.getCode()).isEqualTo(BaseResultEnum.SUCCESS.getReturnCode());
        assertThat(result.getResult()).isInstanceOf(HashMap.class);
        assertThat((Map)result.getResult()).containsKey("group");
    }

    @Test
    public void testFindAllGroup() {
        BaseResultEntity<?> result = groupService.findAllGroup("groupID");
        assertThat(result.getCode()).isEqualTo(BaseResultEnum.SUCCESS.getReturnCode());
        assertThat(result.getResult()).isInstanceOf(HashMap.class);
        assertThat((Map)result.getResult()).containsKey("groupList");
    }

    @Test
    public void testFindOrganInGroup() {
        BaseResultEntity<?> result = groupService.findOrganInGroup(1L);
        assertThat(result.getCode()).isEqualTo(BaseResultEnum.SUCCESS.getReturnCode());
        assertThat(result.getResult()).isInstanceOf(HashMap.class);
        assertThat((Map)result.getResult()).containsKey("organList");
    }

    @Test
    public void testFindMyGroupOrgan() {
        BaseResultEntity<?> result = groupService.findMyGroupOrgan("groupID");
        assertThat(result.getCode()).isEqualTo(BaseResultEnum.SUCCESS.getReturnCode());
        assertThat(result.getResult()).isInstanceOf(ArrayList.class);
    }
}
