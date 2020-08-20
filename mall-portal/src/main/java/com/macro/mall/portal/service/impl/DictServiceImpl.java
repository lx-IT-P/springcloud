package com.macro.mall.portal.service.impl;

import cn.hutool.core.lang.Dict;
import com.macro.mall.model.DictDO;
import com.macro.mall.portal.dao.DictMapper;
import com.macro.mall.portal.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: liuxiang
 * @Date: 2020/4/30
 * @Description:
 */
@Service
public class DictServiceImpl implements DictService {
    @Autowired
    private static DictMapper dictMapper;

    @Override
    public List<DictDO> list() {
        return dictMapper.selectAll();
    }

    public static HashMap dictValue(){
        List<DictDO> list = dictMapper.selectAll();
        HashMap<Integer, String> objectObjectHashMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            objectObjectHashMap.put(list.get(i).getParamCode(),list.get(i).getParamValue());
        }
     return objectObjectHashMap;
     }

}
