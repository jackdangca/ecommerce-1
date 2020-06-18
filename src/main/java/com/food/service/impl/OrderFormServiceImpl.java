package com.food.service.impl;

import com.food.exception.UnexpectedException;
import com.food.mappers.DeliveryAddressMapper;
import com.food.mappers.OrderFormMapper;
import com.food.mappers.OrderItemMapper;
import com.food.model.DeliveryAddress;
import com.food.model.OrderForm;
import com.food.model.OrderFormExample;
import com.food.model.OrderItem;
import com.food.model.constants.OrderConstants;
import com.food.model.vo.*;
import com.food.service.ICustomerService;
import com.food.service.IMerchantService;
import com.food.service.IOrderFormService;
import com.food.service.IProductService;
import com.food.utils.CheckUpdateUtil;
import com.food.utils.IDUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.food.model.constants.OrderConstants.DINING_METHOD;
import static com.food.model.constants.OrderConstants.PAYMENT_METHODS;

@Service
public class OrderFormServiceImpl implements IOrderFormService {


    private OrderFormMapper orderFormMapper;
    private OrderItemMapper orderItemMapper;
    private DeliveryAddressMapper deliveryAddressMapper;
    private IProductService productService;
    @Autowired
    private IMerchantService merchantService;
    @Autowired
    private ICustomerService customerService;


    public  OrderFormServiceImpl(OrderFormMapper orderFormMapper, OrderItemMapper orderItemMapper, DeliveryAddressMapper deliveryAddressMapper, IProductService productService){
        this.orderFormMapper = orderFormMapper;
        this.orderItemMapper = orderItemMapper;
        this.deliveryAddressMapper = deliveryAddressMapper;
        this.productService = productService;
    }

    OrderItemVO convertToItemVO(OrderItem item){
        OrderItemVO vo=new OrderItemVO();
        BeanUtils.copyProperties(item,vo);
        return vo;
    }


    @Transactional
    @Override
    public OrderResultVO createOrder(UserClientOrderVO vo) {
        if(vo.getOrderItems().size() == 0){
            throw new UnexpectedException("订单商品列表为空！");
        }

        if(!PAYMENT_METHODS.contains(vo.getPaymentMethod()))
        {
            throw new UnexpectedException("无法识别的支付方式！");
        }
        if(!DINING_METHOD.contains(vo.getDiningMethod()))
        {
            throw new UnexpectedException("无法识别的订餐方式！");
        }

        MerchantVO merchant = merchantService.findMerchantById(vo.getMerchant_id());
        CustomerVO existingCustomer = customerService.findUserByPhoneOrId(vo.getCustomer());
        if(existingCustomer == null){
            vo.getCustomer().setId(null);
            vo.getCustomer().setAuto_generated(true);
            existingCustomer = customerService.addUser(vo.getCustomer());
        }
        Integer customerId = existingCustomer.getId();
        DeliveryAddress address =new DeliveryAddress();
        address.setAddress(vo.getAddress());
        address.setName(vo.getCustomer().getName());
        address.setPhone(vo.getCustomer().getPhone());
        address.setUser_id(customerId);
        address.setPostcode(null);
        deliveryAddressMapper.insert(address);
        List<OrderItemVO> orderItems = vo.getOrderItems();
        OrderForm order = new OrderForm();
        order.setBuyer(vo.getCustomer().getName());
        order.setComment(vo.getComment());
        //TODO 加 expire time， 有用户功能的时候
        //        order.setExpired_time(Instant.now().);

        long orderCode = orderFormMapper.selectOrderCode();

        BigDecimal totalPrice =new BigDecimal(0);
        int quantity=0;
        for (OrderItemVO item : orderItems) {
            ProductVO product = productService.getProductById(item.getProduct_id());
            BigDecimal subTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setSub_total(subTotal);
            item.setUniprice(product.getPrice());
            item.setImg(product.getImgs().get(0).getUrl());
            item.setProduct_desc(product.getDescription());
            item.setProduct_name(product.getName());
            quantity += item.getQuantity();
            totalPrice = totalPrice.add(subTotal);
        }

        String orderNumber = IDUtils.getOrderId(orderCode);
        order.setPayment_method(vo.getPaymentMethod());
        order.setDining_method(vo.getDiningMethod());
        order.setDelivery_address_id(address.getId());
        order.setOrder_code(orderCode+"");
        order.setOrder_number(orderNumber);
        order.setCreate_time(new Date());
        order.setUpdate_time(order.getCreate_time());
        order.setPhone(vo.getCustomer().getPhone());
        order.setStatus(OrderConstants.STATUS_UNPAID);
        order.setUser_id(customerId);
        order.setMerchant_id(merchant.getId());
        order.setTotal_price(totalPrice);
        order.setTotal_count(quantity);
        int result = orderFormMapper.insert(order);
        if(result != 1)
            throw new UnexpectedException("订单创建异常! 10001");
        Integer orderId = order.getId();
        for (OrderItemVO item : orderItems) {
            item.setOrder_id(orderId);
            OrderItem orderItem = new OrderItem();
            BeanUtils.copyProperties(item,orderItem);
            int insert = orderItemMapper.insert(orderItem);
            item.setId(orderItem.getId());
            if(insert != 1){
                throw new UnexpectedException("订单创建异常! 10002");
            }
        }


        //

        DeliveryAddressVO addressVO =new DeliveryAddressVO();
        BeanUtils.copyProperties(address,addressVO);

        OrderResultVO orderResultVO = new OrderResultVO();
        BeanUtils.copyProperties(order,orderResultVO);
        orderResultVO.setOrderItems(orderItems);
        orderResultVO.setCustomer(existingCustomer);
        orderResultVO.setAddress(addressVO);
        orderResultVO.setMerchant(merchant);
        return orderResultVO;
    }

    @Transactional
    @Override
    public void updateOrder(OrderForm orderForm) {
        orderForm.setUpdate_time(new Date());
        int i = orderFormMapper.updateByPrimaryKey(orderForm);
        CheckUpdateUtil.test(i,"update order "+orderForm.getId()+" failed");
    }

    @Override
    public void updateAllOrders(Iterable<OrderForm> orderForms) {

    }

    @Override
    public void updateOrderSelective(OrderForm orderForm) {

    }


    @Override
    public void deleteOrderById(Integer id) {

    }

    @Override
    public Optional<OrderForm> findOrderById(Integer id) {
        return Optional.empty();
    }

    @Override
    public List<OrderForm> findOrderByIds(List<Integer> ids) {
        return null;
    }

    @Override
    public List<OrderForm> findAllOrder() {
        return null;
    }

    @Override
    public List<OrderForm> findAllOrdersByUserId(Integer userId) {
        return null;
    }

    @Override
    public Page<BusinessClientOrderResultVO> findAllOrdersByMerchantId( BusinessClientOrderQueryVO example, Page<BusinessClientOrderResultVO> page) {
        MerchantVO merchant = merchantService.findMerchantById(example.getMerchant_id());
        if(!"available".equals(merchant.getAvailability())){
            throw  new UnexpectedException("current merchant is not available");
        }
        OrderFormExample ofe=new OrderFormExample();
        OrderFormExample.Criteria criteria = ofe.createCriteria();
        criteria.andMerchant_idEqualTo(merchant.getId());
        if(example.getStart_create_time() !=null && example.getEnd_create_time() !=null){
            criteria.andCreate_timeBetween(example.getStart_create_time(),example.getEnd_create_time());
        }
        if(example.getStart_update_time() !=null && example.getEnd_update_time() !=null){
            criteria.andUpdate_timeBetween(example.getStart_update_time(),example.getEnd_update_time());
        }
        if(StringUtils.hasText(example.getStatus())){
            criteria.andStatusEqualTo(example.getStatus());
        }
        if(StringUtils.hasText(example.getBuyer())){
            criteria.andBuyerLike("%"+example.getBuyer().trim()+"%");
        }

         if(StringUtils.hasText(example.getOrder_number())){
            criteria.andOrder_numberLike("%"+example.getOrder_number().trim()+"%");
        }
         if(StringUtils.hasText(example.getDining_method())){
            criteria.andDining_methodEqualTo(example.getDining_method().trim());
        }




        long totalElements = orderFormMapper.countByExample(ofe);
        int offset =page.getCurrentPage()*page.getPageSize();
        List<OrderForm> orderForms = orderFormMapper.selectAll(ofe,page.getPageSize(),offset);
        List<BusinessClientOrderResultVO> items = orderForms.stream().map(of -> {
            BusinessClientOrderResultVO vo = new BusinessClientOrderResultVO();
            BeanUtils.copyProperties(of, vo);
            return vo;
        }).collect(Collectors.toList());
        return Page.buildResult(page, (int) totalElements, items);
    }

    @Override
    public long count(OrderForm example) {
        return 0;
    }

}
