package com.macro.mall.seckill.controller;

import com.macro.mall.seckill.util.DistributedRedisLock;
import com.macro.mall.seckill.util.RedisUtil;
import com.netflix.ribbon.proxy.annotation.Hystrix;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @Author: liuxiang
 * @Date: 2020/5/2
 * @Description:
 */
@Controller
@Api(tags = "SeckillController", description = "秒杀管理")
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private RedisUtil redisUtil;

   /* @Autowired
    private RedissonClient redissonClient;*/

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @ApiOperation("获取秒杀商品")
    @RequestMapping(value = "/seckill", method = RequestMethod.GET)
    @ResponseBody
    public String kill() throws InterruptedException {
        /**
         * 直接请求，10000次请求，库存还有剩余，线程不安全的
         */
      /*  int stock = (int) redisUtil.get("seckill");
        if (stock > 0) {
            redisUtil.decr("seckill", 1);
            System.out.println("当前库存数量" + stock + ",某用户抢购成功，当前抢购数：" + (10000 - stock));
        }*/
        /**
         * CountDownLatch,每次设置100个人只能抢到一个，库存数量正确，1000个请求线程安全,10000个请求线程不安全
         */
        final int totalThread = 10;
        CountDownLatch countDownLatch = new CountDownLatch(totalThread);
        Semaphore semaphore = new Semaphore(10);
        int stock = (int) redisUtil.get("seckill_seckill");
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("100个人开始抢1个");
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        if (stock > 0) {
            redisUtil.decr("seckill_seckill", 1);
            System.out.println("当前库存数量" + stock + ",某用户抢购成功，当前抢购数：" + (10000 - stock));
        }
        semaphore.release();
        executorService.shutdown();
        /**
         * Semaphore控制访问人数每次只能进来10个，库存数量正确,前几个线程不安全
         */
       /* final int clientCount = 10;
        final int totalRequestCount = 1000;
        Semaphore semaphore = new Semaphore(clientCount);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < totalRequestCount; i++) {
            int stock = (int) redisUtil.get("seckill_seckill");
            executorService.execute(() -> {
                try {
                    semaphore.acquire();
                    if (stock > 0) {
                        redisUtil.decr("seckill_seckill", 1);
                    }
                    System.out.println("当前库存数量" + stock + ",某用户抢购成功，当前抢购数：" + (10000 - stock));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
            });
        }
        executorService.shutdown();*/
        return "1";
    }

    /**
     * 线程安全
     *
     * @return
     */
    @ApiOperation("Redission获取秒杀商品")
    @RequestMapping(value = "/redissonSeckill", method = RequestMethod.GET)
    @ResponseBody
    @Hystrix
    public String redissonSeckill() {
        String key = "seckill_seckill";
        /*RSemaphore semaphore = redissonClient.getSemaphore(key);
        boolean seckill = semaphore.tryAcquire();*/
        int stock = (int) redisUtil.get(key);
        //加锁
        boolean seckill = DistributedRedisLock.acquire(key);
        //执行具体业务逻辑
        if (seckill) {
            redisUtil.decr(key, 1);
            System.out.println("当前库存数量" + stock + ",某用户抢购成功，当前抢购数：" + (10000 - stock));
        } else {
            System.out.println("当前库存数量" + stock + ",某用户抢购失败");
        }
        //释放锁
        DistributedRedisLock.release(key);
        return "1";
    }

    /**
     * 利用jedis事务可以保证线程安全
     *
     * @return
     */
    @ApiOperation("jedis获取秒杀商品")
    @RequestMapping(value = "/jedisSeckill", method = RequestMethod.GET)
    @ResponseBody
    public String jedisSeckill() {
       /* Jedis jedis = new Jedis("localhost",
                6379);
        String key = "seckill";
        jedis.watch(key);
        int stock = Integer.parseInt(jedis.get(key))
        if (stock > 0) {
            redis.clients.jedis.Transaction multi = jedis.multi();
            multi.incrBy(key, -1);
            List<Object> exec = multi.exec();
            if (exec != null && exec.size() > 0) {
                System.out.println("当前库存数量" + stock + ",某用户抢购成功，当前抢购数：" + (10000 - stock));
            } else {
                System.out.println("当前库存数量" + stock + ",某用户抢购失败");
            }
        }*/
        return "1";
    }

}