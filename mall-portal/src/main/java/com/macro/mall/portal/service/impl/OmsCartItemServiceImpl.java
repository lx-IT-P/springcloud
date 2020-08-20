package com.macro.mall.portal.service.impl;

import com.macro.mall.common.advice.CacheException;
import com.macro.mall.common.enums.ExceptionEnum;
import com.macro.mall.common.exception.Assert;
import com.macro.mall.common.exception.Asserts;
import com.macro.mall.common.utils.JsonUtils;
import com.macro.mall.mapper.OmsCartItemMapper;
import com.macro.mall.model.OmsCartItem;
import com.macro.mall.model.OmsCartItemExample;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.dao.PortalProductDao;
import com.macro.mall.portal.domain.CartProduct;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.service.OmsCartItemService;
import com.macro.mall.portal.service.OmsPromotionService;
import com.macro.mall.portal.service.UmsMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车管理Service实现类
 * Created by macro on 2018/8/2.
 */
@Service
public class OmsCartItemServiceImpl implements OmsCartItemService {
    private static Logger LOGGER = LoggerFactory.getLogger(OmsCartItemServiceImpl.class);
    @Autowired
    private OmsCartItemMapper cartItemMapper;
    @Autowired
    private PortalProductDao productDao;
    @Autowired
    private OmsPromotionService promotionService;
    @Autowired
    private UmsMemberService memberService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final String KEY_PREFIX = "ly:cart:uid:";
    private static final String CART_KEY_PREFIX = "cart:id:";
    private static final String QUANTITY = "cart:quantity:id:";

    @CacheException
    @Override
    public int add(OmsCartItem cartItem) {
        Assert.notNull(cartItem.getId(), ExceptionEnum.PARAM_EMPTY);
        //获取会员信息
        UmsMember currentMember = memberService.getCurrentMember();
        cartItem.setMemberId(currentMember.getId());
        cartItem.setMemberNickname(currentMember.getNickname());
        cartItem.setDeleteStatus(0);
        Assert.notNull(currentMember, ExceptionEnum.MEMBER_NOT_FOUND);
        //购物车的key
        String key = KEY_PREFIX + currentMember.getId();
        //购物车数量的key
        String quantityKey = QUANTITY + cartItem.getId();
        //获取hash操作对象
        BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(key);
        // 查询是否存在
        String cartKey = CART_KEY_PREFIX + cartItem.getId();
        Integer num = cartItem.getQuantity();
        if (hashOps.hasKey(cartKey)) {
            // 存在，获取购物车数据
            String json = hashOps.get(cartKey).toString();
            cartItem = JsonUtils.parse(json, OmsCartItem.class);
            // 修改购物车数量
            //cartItem.setQuantity(cartItem.getQuantity() + num);
            stringRedisTemplate.opsForValue().increment(quantityKey,1L);
            LOGGER.info("此商品存在，并更新redis数量{}",Integer.parseInt(stringRedisTemplate.opsForValue().get(quantityKey)));
            return Integer.parseInt(stringRedisTemplate.opsForValue().get(quantityKey));
        }
        // 写回redis
        cartItem.setCreateDate(new Date());
        stringRedisTemplate.opsForValue().set(quantityKey, String.valueOf(1));
        hashOps.put(cartKey, JsonUtils.serialize(cartItem));
        LOGGER.info("此商品已存入redis购物车");
        return Integer.parseInt(stringRedisTemplate.opsForValue().get(quantityKey));
    }

    /**
     * 根据会员id,商品id和规格获取购物车中商品
     */
    private OmsCartItem getCartItem(OmsCartItem cartItem) {
        OmsCartItemExample example = new OmsCartItemExample();
        OmsCartItemExample.Criteria criteria = example.createCriteria().andMemberIdEqualTo(cartItem.getMemberId())
                .andProductIdEqualTo(cartItem.getProductId()).andDeleteStatusEqualTo(0);
        if (!StringUtils.isEmpty(cartItem.getProductSkuId())) {
            criteria.andProductSkuIdEqualTo(cartItem.getProductSkuId());
        }
        List<OmsCartItem> cartItemList = cartItemMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(cartItemList)) {
            return cartItemList.get(0);
        }
        return null;
    }

    @CacheException
    @Override
    public List<OmsCartItem> list(Long memberId) {
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andDeleteStatusEqualTo(0).andMemberIdEqualTo(memberId);
        //购物车的key
        String key = KEY_PREFIX + memberId;
        if (!this.stringRedisTemplate.hasKey(key)) {
            // 不存在，直接返回
            return null;
        }
        BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(key);
        List<Object> carts = hashOps.values();
        // 判断是否有数据
        if (CollectionUtils.isEmpty(carts)) {
            return null;
        }
        // 查询购物车数据
        return carts.stream().map(o -> JsonUtils.parse(o.toString(), OmsCartItem.class)).collect(Collectors.toList());
    }

    @Override
    public List<CartPromotionItem> listPromotion(Long memberId) {
        List<OmsCartItem> cartItemList = list(memberId);
        List<CartPromotionItem> cartPromotionItemList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cartItemList)) {
            cartPromotionItemList = promotionService.calcCartPromotion(cartItemList);
        }
        return cartPromotionItemList;
    }

    @Override
    public int updateQuantity(Long id, Long memberId, Integer quantity) {
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setQuantity(quantity);
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andDeleteStatusEqualTo(0)
                .andIdEqualTo(id).andMemberIdEqualTo(memberId);
        return cartItemMapper.updateByExampleSelective(cartItem, example);
    }

    @CacheException
    @Override
    public int delete(Long memberId, List<Long> ids) {
        //获取会员信息
        UmsMember currentMember = memberService.getCurrentMember();
        Assert.notNull(currentMember, ExceptionEnum.MEMBER_NOT_FOUND);
        //购物车的key
        String key = KEY_PREFIX + currentMember.getId();
        for (Long id : ids) {
            // 查询是否存在
            String cartKey = CART_KEY_PREFIX + id;
            // 获取hash操作对象
            BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(key);
            if (!hashOps.hasKey(cartKey)) {
                // 不存在，直接返回
                return 0;
            }
            hashOps.delete(cartKey);
            return 1;
        }
        return 0;
    }

    @Override
    public CartProduct getCartProduct(Long productId) {
        return productDao.getCartProduct(productId);
    }

    @Override
    public int updateAttr(OmsCartItem cartItem) {
        //删除原购物车信息
        OmsCartItem updateCart = new OmsCartItem();
        updateCart.setId(cartItem.getId());
        updateCart.setModifyDate(new Date());
        updateCart.setDeleteStatus(1);
        cartItemMapper.updateByPrimaryKeySelective(updateCart);
        cartItem.setId(null);
        add(cartItem);
        return 1;
    }

    @Override
    public int clear(Long memberId) {
        OmsCartItem record = new OmsCartItem();
        record.setDeleteStatus(1);
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andMemberIdEqualTo(memberId);
        return cartItemMapper.updateByExampleSelective(record, example);
    }
}
