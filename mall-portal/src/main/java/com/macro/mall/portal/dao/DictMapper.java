package com.macro.mall.portal.dao;

import com.macro.mall.model.DictDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DictMapper {

    List<DictDO> selectAll();
}
