package com.macro.mall.grade.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.grade.domain.SucessResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Set;

/**
 * @Author: liuxiang
 * @Date: 2020/6/4
 * @Description: 闯关控制器
 */
@RestController
@Api(tags = "WordController", description = "闯关单词")
@RequestMapping("/word")
public class WordController {
    private static String[] word = null;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @ApiOperation("给用户添加单词库")
    @RequestMapping(value = "/putWord", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult putWord(@RequestParam String userId) {
        String userPerfix = "user:word:" + userId;
        //绑定hashKey
        BoundHashOperations<String, Object, Object> stringObjectObjectBoundHashOperations = stringRedisTemplate.boundHashOps(userPerfix);
        //添加单词库
        for (int i = 0; i < 100; i++) {
            stringObjectObjectBoundHashOperations.put(String.valueOf(i), "十全十美");
        }
        return CommonResult.success("ok");
    }

    @ApiOperation("获取本关单词")
    @RequestMapping(value = "/getWord", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult getWord(@NotNull @RequestParam String userId, @NotNull @RequestParam String[] id) {
        //获取用户闯关单词
        ArrayList<String> strings1 = getUserWord(userId, id);
        //将成语id保存起来
        word = id;
        //显示过关的排名
        ZSetOperations<String, String> stringStringZSetOperations = stringRedisTemplate.opsForZSet();
        //获取闯关的等级
        stringStringZSetOperations.incrementScore("userScore", userId, id.length);
        return strings1 == null ? CommonResult.failed("该用户未注册") : CommonResult.success(strings1);
    }

    /**
     * 获取用户闯关单词
     *
     * @param userId
     * @param id
     * @return
     */
    private ArrayList<String> getUserWord(@NotNull @RequestParam String userId, @NotNull @RequestParam @NotNull String[] id) {
        String userPerfix = "user:word:" + userId;
        BoundHashOperations<String, Object, Object> stringObjectObjectBoundHashOperations = stringRedisTemplate.boundHashOps(userPerfix);
        ArrayList<String> strings1 = new ArrayList<>();
        for (String i : id) {
            Object o1 = stringObjectObjectBoundHashOperations.get(i);
            strings1.add((String) o1);
        }
        return strings1;
    }

    @ApiOperation("闯关成功")
    @RequestMapping(value = "/success", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<SucessResult> success(@NotNull @RequestParam String userId) {
        String userPerfix = "user:word:" + userId;
        BoundHashOperations<String, Object, Object> stringObjectObjectBoundHashOperations = stringRedisTemplate.boundHashOps(userPerfix);
        //展示已成功的单词
        ArrayList<String> strings1 = new ArrayList<>();
        for (String i : word) {
            Object o1 = stringObjectObjectBoundHashOperations.get(i);
            strings1.add((String) o1);
        }
        SucessResult sucessResult = new SucessResult();
        sucessResult.setStrings1(strings1);
        //删除已闯关成功的单词
        stringObjectObjectBoundHashOperations.delete(word);
        //获取闯关排名
        Double userScore = stringRedisTemplate.opsForZSet().score("userScore", userId);
        if (userScore == null) {
            Set<String> userScore1 = stringRedisTemplate.opsForZSet().reverseRange("userScore", 0, -1);
            sucessResult.setUserScore1(userScore1);
            return CommonResult.success(sucessResult);
        }
        Set<String> userScore1 = stringRedisTemplate.opsForZSet().reverseRange("userScore", 0, userScore.longValue());
        sucessResult.setUserScore1(userScore1);
        //清除数组中的内容
        word = null;
        return CommonResult.success(sucessResult);
    }

}
