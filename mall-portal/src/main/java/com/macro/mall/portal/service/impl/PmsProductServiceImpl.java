package com.macro.mall.portal.service.impl;

import com.macro.mall.common.utils.JsonUtils;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.portal.dao.PmsProductDao;
import com.macro.mall.portal.service.PmsProductService;
import com.macro.mall.portal.util.RedisUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.ribbon.proxy.annotation.Hystrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 商品管理Service实现类
 * Created by macro on 2018/4/26.
 */
@Service
public class PmsProductServiceImpl implements PmsProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PmsProductServiceImpl.class);
    @Autowired
    private PmsProductDao pmsProductDao;
    @Autowired
    private RedisUtil redisUtil;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "sku:info";
    private static final String KEY = "sku:id:";
    //锁名称前缀
    public static final String LOCK_PREFIX = "redis:lock:id:";

    /**
     * 查询商品详情
     *
     * @param id
     * @return
     */
    @Override
    @HystrixCommand(fallbackMethod = "hiError")
    public PmsProduct list(Long id,String ip) {
        System.out.println("当前" + ip + "正在访问");
        //商品详情的key
        String key = KEY_PREFIX;
        String lockKey = LOCK_PREFIX + id;
        String infoKey = KEY + id;
        BoundHashOperations<String, Object, Object> stringObjectObjectBoundHashOperations = redisTemplate.boundHashOps(key);
        // 创建布隆过滤器对象，防止缓存击穿
       /* BloomFilter<Integer> filter = BloomFilter.create(
                Funnels.integerFunnel(),
                15000,
                0.01);
        //添加数据到布隆过滤器
        String ids = id.toString();
        filter.putAll(Integer.valueOf(ids));*/
        //查询缓存中是否存在
        if (stringObjectObjectBoundHashOperations.hasKey(key)) {
            //查询缓存
            return (PmsProduct) redisUtil.get(key);
        } else {
                System.out.println("ip为" + ip + "在缓存中没有查到，申请分布式锁" + lockKey);
                //设置分布式锁，防止缓存击穿
                String token = UUID.randomUUID().toString();
                //redisTemplate设置分布式锁
                //boolean lock = redisUtil.lock(lockKey);
                Jedis jedis = new Jedis("localhost", 6379);
                //防止删除别人的锁
                String lock = jedis.set(lockKey, token, "nx", "px", 10 * 1000);
                if (null != lock && "OK".equals(lock)) {
                    //设置成功，有权在锁有效时间内查询数据库
                    PmsProduct list = pmsProductDao.list(id);
                    //防止某用户抢不到锁
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (list != null) {
                        //放入redis
                        stringObjectObjectBoundHashOperations.put(infoKey, JsonUtils.serialize(list));
                        Random r = new Random();
                        //设置key的随机过期时间，防止缓存雪崩
                        stringObjectObjectBoundHashOperations.expire(r.nextInt(123), TimeUnit.SECONDS);
                        return list;
                    } else {
                        //防止缓存穿透，设置为null的key以及过期时间
                        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(key);
                        hashOperations.put(key, "");
                        hashOperations.expire(60 * 3, TimeUnit.SECONDS);
                    }
                    //判断是否是删除自己的锁
                    String localToken = jedis.get(lockKey);
                    if (null != localToken && token.equals(localToken)) {
                        String script = "if redis.call('get',KEYS[1] == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                        Long eval = (Long) jedis.eval(script, Integer.parseInt(lockKey),localToken);
                        if (eval != null && eval != 0) {
                            //使用lua脚本在高并发情况下，获取key的同时删除key
                            jedis.del(lockKey);
                        }
                        System.out.println("ip" + ip + "申请分布式锁成功，并已删除");
                    }
                    //redisTemplate释放分布式锁
                    //redisUtil.deleteLock(key);
                } else {
                    System.out.println("ip为" + ip + "申请分布式锁失败" + lockKey);
                    //设置自旋,重新访问
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //直接return,防止产生未执行线程
                    return list(id, ip);
                }
            }
            return null;
        }
    public String hiError(String name) {
        return "hi,"+name+",sorry,error!";
    }

}
