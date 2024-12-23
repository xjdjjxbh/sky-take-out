package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
在实际运行时，Spring Boot 会扫描到 @Mapper 注解，并自动创建代理实例，以供依赖注入使用。
Spring Boot 配合 MyBatis，通过 @MapperScan 或 @Mapper 注解，自动扫描 @Mapper 标记的接口，然后为每个接口创建代理类。
这些代理类会将接口中的方法映射到对应的 SQL 语句或 XML 文件中的 SQL 配置。
因此，在服务或控制层可以直接注入这个接口，就像注入一个普通类一样，Spring Boot 会自动提供一个可用的代理实例。
 */
@Mapper
public interface UserMapper {

    /**
     * 根据openId查询用户信息
     * @param openId
     * @return
     */
    @Select("select * from user where openid = #{openId}")
    User getByOpenId(String openId);

    //插入完用户之后要返用户的id，因为在插入完之后会使用到用户的id。所以这里使用配置文件编写sql

    /**
     * 插入新用户
     * @param user
     */
    void insert(User user);

    /**
     * 根据用户id获取用户
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 按条件查询用户数量
     * @param conditionMap
     * @return
     */
    Integer countByMap(Map conditionMap);
//    Integer countByMap(HashMap<String, LocalDateTime> conditionMap);

    /**
     * 根据条件查询订单数量
     * @param conditionMap
     * @return
     */
    Integer countOrderNumberByMap(HashMap conditionMap);
}
