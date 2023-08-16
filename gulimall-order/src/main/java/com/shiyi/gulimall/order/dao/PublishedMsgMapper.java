package com.shiyi.gulimall.order.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shiyi.gulimall.order.entity.PublishedMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * <p>
 * 发布消息表 Mapper 接口
 * </p>
 *
 * @author shiyi
 * @since 2023-07-17
 */
@Mapper
public interface PublishedMsgMapper extends BaseMapper<PublishedMsg> {

     List<PublishedMsg> findMessageByLtStatus(@Param("status") int status);

     List<PublishedMsg> findMessageByStatusAndDelayTime(@Param("status") int status);
}
