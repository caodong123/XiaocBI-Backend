package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * excel 转 csv
 */
@Slf4j
public class ExcelUtils {

    public static String excelToCsv(MultipartFile multipartFile)  {

        //换成上传文件multipartFile
        File file = null;
        /*try {
            file = ResourceUtils.getFile("classpath:test_excel.xlsx");
        } catch (FileNotFoundException e) {
            log.error("excel转换错误");
        }*/
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("excel转换失败");
        }
        //处理list
        //拼接结果
        //头部 第一行
        StringBuilder res = new StringBuilder();
        if(CollUtil.isEmpty(list)){
            return null;
        }
        LinkedHashMap<Integer, String> head = (LinkedHashMap<Integer, String>) list.get(0);
        //去除null情况
        List<String> headList = head.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        for (int i = 0; i < headList.size(); i++) {
            res.append(headList.get(i)).append(" ");
        }
        res.append("\n");
       // System.out.println(res);
        //下部数据
        for (int i = 1; i < list.size(); i++) {
            LinkedHashMap<Integer, String> datas = (LinkedHashMap<Integer, String>) list.get(i);
            //去除null情况
            List<String> dataList = datas.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            for (int j = 0; j < dataList.size(); j++) {
                res.append(dataList.get(j)).append(" ");
            }
            res.append("\n");
        }

       // System.out.println(list);
        System.out.println(res);
        return res.toString();
    }

    public static void main(String[] args) {
        excelToCsv(null);
    }

}
