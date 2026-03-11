package com.sky.controller.admin;


import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController //身份声明 这个类是一个 Web 控制器，专门负责处理 HTTP 请求
@RequestMapping("/admin/order")  //地址导航写在类上面（提取公共路径）
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;
    /**
     * 订单搜索
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    //此处为URL 拼接参数 直接用DTO
    //前端传来的为JSON 格式 需要用 @RequestBody
    //前端传来路径参数 用 @PathVariable
    public Result<PageResult> search(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索：{}", ordersPageQueryDTO);
        //此处需要进行容器的注入 否则无法获取数据
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 订单数量统计
     */

    @GetMapping("/statistics")
    @ApiOperation("订单数量统计")
    public Result statistics(){

        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        //返回的为VO 即data数据
        return Result.success(orderStatisticsVO);
    }
}
