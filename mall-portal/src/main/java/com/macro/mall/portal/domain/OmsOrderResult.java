package com.macro.mall.portal.domain;


import com.macro.mall.model.OmsOrder;

import java.util.List;

/**
 * @Author: liuxiang
 * @Date: 2020/4/30
 * @Description:
 */
public class OmsOrderResult {
    public List<OmsOrder> getOmsOrders() {
        return omsOrders;
    }

    public void setOmsOrders(List<OmsOrder> omsOrders) {
        this.omsOrders = omsOrders;
    }

    public OmsOrder getOmsOrderDetails() {
        return omsOrderDetails;
    }

    public void setOmsOrderDetails(OmsOrder omsOrderDetails) {
        this.omsOrderDetails = omsOrderDetails;
    }

    private List<OmsOrder> omsOrders;
    private OmsOrder omsOrderDetails;
}
