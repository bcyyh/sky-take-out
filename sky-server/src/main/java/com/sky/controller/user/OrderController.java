package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")  //防止重名所以进行重命名
/*@RestController = @Controller + @ResponseBody
@Controller：将当前类注册为 Spring MVC 的控制器组件，交给 Spring 容器管理。
@ResponseBody：将方法的返回值直接写入 HTTP 响应体中*/
@RequestMapping("/user/order")
@Api(tags = "C端-订单接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 查询历史订单
     */
    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单")
    //Data中嵌套列表
    public Result<PageResult> page(int page, int pageSize, Integer status) {
        log.info("查询历史订单：{}", status);
        //将查询出的历史菜单进行返回 data下有total 和 records 所以用Pageresult返回
        PageResult pageResult = orderService.pageQuery4User(page, pageSize, status);
        //需要返回数据 所以用PageResult
        return Result.success(pageResult);  //total 和 records
    }
    /**
     * 查询订单详情
     */
    @GetMapping("/orderDetail/{id}") //路径参数要写完整、传递的参数需要进行绑定
    @ApiOperation("查询订单详情")
    public Result<Object> orderDetail(@PathVariable("id") Long id){
        log.info("查询订单详情，订单id为：{}", id);
        //Data中有数据 一般返回VO数据
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * POST 往往用于创建一个新的资源（比如：下新订单）。
     * PUT 用于修改已有的资源
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    //路径参数 需要加@PathVariable
    public Result cancel(@PathVariable("id") Long id) throws Exception {
        log.info("取消订单，订单id为：{}", id);
        orderService.userCancelById(id);
        return Result.success();
    }

    /**
     * 再来一单
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable("id") Long id) {
        log.info("再来一单，订单id为：{}", id);
        orderService.repetition(id);
        return Result.success();
    }

}
