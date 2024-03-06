package com.yupi.springbootinit.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.FileConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.manager.RedisLimiterManager;
import com.yupi.springbootinit.model.dto.chart.*;

import com.yupi.springbootinit.model.dto.file.UploadFileRequest;
import com.yupi.springbootinit.model.dto.post.PostQueryRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.Post;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.enums.FileUploadBizEnum;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.AIUtil;
import com.yupi.springbootinit.utils.ExcelUtils;
import com.yupi.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);


        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }


    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }


    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */

    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws Exception {

        //校验
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //校验文件
        //校验文件后缀
        String filename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(filename); //后缀
        final List<String> validSuffix = Arrays.asList("png", "jpg", "svg", "jpeg", "xlsx");
        ThrowUtils.throwIf(!validSuffix.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        //校验文件大小
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > 1024 * 1024, ErrorCode.PARAMS_ERROR, "文件大小过大，超过1M");

        //校验合法性
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "参数过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");

        //获取登录用户
        User loginUser = userService.getLoginUser(request);

        //限流
        redisLimiterManager.doLimiter("genChartByAi" + loginUser.getId().toString());


        //拼接请求
        //用户输入
        StringBuilder userInput = new StringBuilder();
        //目标
        userInput.append("分析需求：").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        //原始数据
        //处理excel
        String data = ExcelUtils.excelToCsv(multipartFile);
        userInput.append("原始数据：").append("\n");
        userInput.append(data);
        //请求
        //模型id
//        final Long modelId=1762821784564355073L;
//        String response = aiManager.doChat(modelId, userInput.toString());
        AIUtil aiUtil = new AIUtil();
        String response = aiUtil.doChat(userInput.toString());

        //需要保存的信息
        Chart chart = new Chart();
        chart.setUserId(loginUser.getId());
        chart.setChartType(chartType);
        chart.setGoal(goal);
        chart.setName(name);

        //提取结果
        String[] splits = new String[0];
        if (!response.contains("【【【【【")) {
            handleChartUpdateError(chart.getId(), "ai生成出现错误");
        }
        splits = response.split("【【【【【");


        String temp = splits[1].trim();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < temp.length(); i++) {
            if (temp.charAt(i) != '\'') res.append(temp.charAt(i));
            else res.append("\"");
        }
        String chartCode = null;
        String analyseResult = null;
        if (!res.toString().contains("】】】】")) {
            handleChartUpdateError(chart.getId(), "ai生成出现错误");
        }
        chartCode = res.toString().split("】】】】")[0];
        analyseResult = res.toString().split("】】】】")[1];


        //保存到数据库中

        chart.setGenResult(analyseResult);
        chart.setGenChart(chartCode);
        chart.setChartData(data);
        chart.setStatus("succeed");


        boolean saved = chartService.save(chart);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "图表信息保存失败");

        //封装到返回类中
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(chartCode);
        biResponse.setGenResult(analyseResult);
        biResponse.setId(chart.getId());

        return ResultUtils.success(biResponse);
    }


    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws Exception {

        //校验
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //校验文件
        //校验文件后缀
        String filename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(filename); //后缀
        final List<String> validSuffix = Arrays.asList("png", "jpg", "svg", "jpeg", "xlsx");
        ThrowUtils.throwIf(!validSuffix.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        //校验文件大小
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > 1024 * 1024, ErrorCode.PARAMS_ERROR, "文件大小过大，超过1M");

        //校验合法性
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "参数过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");

        //获取登录用户
        User loginUser = userService.getLoginUser(request);

        //限流
        redisLimiterManager.doLimiter("genChartByAi" + loginUser.getId().toString());


        //拼接请求
        //用户输入
        StringBuilder userInput = new StringBuilder();
        //目标
        userInput.append("分析需求：").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        //原始数据
        //处理excel
        String data = ExcelUtils.excelToCsv(multipartFile);
        userInput.append("原始数据：").append("\n");
        userInput.append(data);
        //请求
        //模型id
//        final Long modelId=1762821784564355073L;

        //先把图表保存到数据库中
        //保存到数据库中
        Chart chart = new Chart();
        chart.setUserId(loginUser.getId());
        chart.setChartType(chartType);
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(data);
        chart.setStatus("wait");
        boolean saved = chartService.save(chart);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "图表信息保存失败");

        //提交一个任务
        CompletableFuture.runAsync(() -> {
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            if (!b) {
                handleChartUpdateError(chart.getId(), "更新图表执行中信息失败");
                return;
            }
            //调用AI
            String response = null;
            try {
//                response = aiManager.doChat(modelId, userInput.toString());
                AIUtil aiUtil = new AIUtil();
                response = aiUtil.doChat(userInput.toString());
            } catch (Exception e) {
                handleChartUpdateError(chart.getId(), "Ai生成错误");
            }

            //提取结果
            String[] splits = new String[0];
            if (!response.contains("【【【【【")) {
                handleChartUpdateError(chart.getId(), "ai生成出现错误");
            }
            splits = response.split("【【【【【");


            String temp = splits[1].trim();
            StringBuilder res = new StringBuilder();
            for (int i = 0; i < temp.length(); i++) {
                if (temp.charAt(i) != '\'') res.append(temp.charAt(i));
                else res.append("\"");
            }
            String chartCode = null;
            String analyseResult = null;
            if (!res.toString().contains("】】】】")) {
                handleChartUpdateError(chart.getId(), "ai生成出现错误");

            }

            chartCode = res.toString().split("】】】】")[0];
            analyseResult = res.toString().split("】】】】")[1];

            //再更新一次
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setStatus("succeed");
            updateChartResult.setGenChart(chartCode);
            updateChartResult.setGenResult(analyseResult);
            boolean b1 = chartService.updateById(updateChartResult);
            if (!b1) {
                handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
            }

        }, threadPoolExecutor);

        //封装到返回类中
        BiResponse biResponse = new BiResponse();
        biResponse.setId(chart.getId());

        return ResultUtils.success(biResponse);
    }

    private void handleChartUpdateError(long chartId, String message) {
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setStatus("failed");
        chart.setExecMessage(message);
        boolean updateById = chartService.updateById(chart);
        if (!updateById) {
            log.info("更新图表失败状态失败,id:" + chartId);
        }


    }


}
