package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.DataPsi;
import com.primihub.biz.entity.data.po.DataPsiTask;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.service.data.DataPsiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * 隐私求交
 */
@RequestMapping("psi")
@RestController
@Slf4j
public class PsiController {

    @Autowired
    private DataPsiService dataPsiService;

    /**
     * 创建并运行隐私求交任务
     * @param userId
     * @param req
     * @return
     */
    @PostMapping("saveDataPsi")
    public BaseResultEntity saveDataPsi(@RequestHeader("userId") Long userId,
                                        DataPsiReq req){
        if (userId<=0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        if (req.getOwnOrganId()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"ownOrganId");
        }
        if (StringUtils.isBlank(req.getOwnResourceId())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"ownResourceId");
        }
        if (StringUtils.isBlank(req.getOwnKeyword())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"ownKeyword");
        }
        if (req.getOtherOrganId()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"otherOrganId");
        }
        if (req.getOtherResourceId()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"otherResourceId");
        }
        if (StringUtils.isBlank(req.getOtherKeyword())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"otherKeyword");
        }
        if (StringUtils.isBlank(req.getResultName())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resultName");
        }
        if (StringUtils.isBlank(req.getResultOrganIds())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resultOrganIds");
        }
        if (req.getPsiTag()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"psiTag");
        }
        if (req.getPsiTag() == 2){
            if (StringUtils.isBlank(req.getTeeOrganId())){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"teeOrganId");
            }
        }
        return dataPsiService.saveDataPsi(req,userId);
    }

    /**
     * 根据隐私求交(psi)ID修改结果名称
     * @param req
     * @return
     */
    @PostMapping("updateDataPsiResultName")
    public BaseResultEntity updateDataPsiResultName(DataPsiReq req){
        if (req.getId()==null||req.getId()==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"id");
        }
        if (StringUtils.isBlank(req.getResultName())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resultName");
        }
        return dataPsiService.updateDataPsiResultName(req);
    }

    /**
     * 查询本地资源列表
     * @param req
     * @param organId
     * @return
     */
    @GetMapping("getPsiResourceList")
    public BaseResultEntity getPsiResourceList(DataResourceReq req, Long organId){
        return dataPsiService.getPsiResourceList(req);
    }

    /**
     * 多方位查询资源支持中心节点查询
     * @param req
     * @param organId
     * @param resourceName
     * @return
     */
    @GetMapping("getPsiResourceAllocationList")
    public BaseResultEntity getPsiResourceDataList(PageReq req,
                                                   String organId,
                                                   String resourceName){
        return dataPsiService.getPsiResourceAllocationList(req,organId,resourceName);
    }

    /**
     * 查询隐私求交任务列表
     * @param req
     * @return
     */
    @GetMapping("getPsiTaskList")
    public BaseResultEntity getPsiTaskList(DataPsiQueryReq req){
        return  dataPsiService.getPsiTaskList(req);
    }


    @GetMapping("getOrganPsiTask")
    public BaseResultEntity getOrganPsiTask(@RequestHeader("userId") Long userId,
                                            String resultName,
                                            PageReq req){
        if (userId<=0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        return dataPsiService.getOrganPsiTask(userId,resultName,req);
    }

    /**
     * 查询隐私求交任务详情
     * @param taskId
     * @return
     */
    @GetMapping("getPsiTaskDetails")
    public BaseResultEntity getPsiTaskDetails(Long taskId){
        if (taskId==null||taskId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return  dataPsiService.getPsiTaskDetails(taskId);
    }

    /**
     * 下载隐私求交结果文件
     * @param response
     * @param taskId
     * @throws Exception
     */
    @GetMapping("downloadPsiTask")
    public void downloadPsiTask(HttpServletResponse response,Long taskId) throws Exception{
        if (taskId==null||taskId==0L) {
            return;
        }
        DataPsiTask task = dataPsiService.selectPsiTaskById(taskId);
        DataPsi dataPsi = dataPsiService.selectPsiById(task.getPsiId());
        File file = new File(task.getFilePath());
        if (file!=null&&file.exists()){
            String fileName = dataPsi.getResultName()+".csv";
            FileInputStream inputStream = new FileInputStream(file);
            response.setHeader("content-Type","application/vnd.ms-excel");
            response.setHeader("content-disposition", "attachment; fileName=" + new String(fileName.getBytes("UTF-8"),"iso-8859-1"));
            ServletOutputStream outputStream = response.getOutputStream();
            int len = 0;
            byte[] data = new byte[1024];
            while ((len = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, len);
            }
            outputStream.close();
            inputStream.close();
        }else {
            String content = "no data";
            String fileName = null;
            if (dataPsi!=null){
                if (task.getFileContent()!=null){
                    content = task.getFileContent();
                }
                fileName = dataPsi.getResultName()+".csv";
            }else {
                fileName = UUID.randomUUID().toString()+".csv";
            }
            OutputStream outputStream = null;
            //将字符串转化为文件
            byte[] currentLogByte = content.getBytes();
            try {
                // 告诉浏览器用什么软件可以打开此文件
                response.setHeader("content-Type","application/vnd.ms-excel");
                // 下载文件的默认名称
                response.setHeader("Content-disposition","attachment;filename="+ new String(fileName.getBytes("UTF-8"),"iso-8859-1"));
                response.setCharacterEncoding("UTF-8");
                outputStream = response.getOutputStream();
                outputStream.write(currentLogByte);
                outputStream.close();
                outputStream.flush();
            }catch (Exception e) {
                log.info("downloadPsiTask -- fileName:{} -- fileContent:{} -- e:{}",fileName,content,e.getMessage());
            }
        }
    }

    /**
     * 删除隐私求交任务
     * @param taskId
     * @return
     */
    @GetMapping("delPsiTask")
    public BaseResultEntity delPsiTask(Long taskId){
        if (taskId==null||taskId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return  dataPsiService.delPsiTask(taskId);
    }

    /**
     * 取消隐私求交任务
     * @param taskId
     * @return
     */
    @GetMapping("cancelPsiTask")
    public BaseResultEntity cancelPsiTask(Long taskId){
        if (taskId==null||taskId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return  dataPsiService.cancelPsiTask(taskId);
    }

    /**
     * 重启隐私求交任务
     * @param taskId
     * @return
     */
    @GetMapping("retryPsiTask")
    public BaseResultEntity retryPsiTask(Long taskId){
        if (taskId==null||taskId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return  dataPsiService.retryPsiTask(taskId);
    }


}
