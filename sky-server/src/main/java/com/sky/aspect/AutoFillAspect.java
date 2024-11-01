package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {


    /**
     * 定义切入点，指定对哪些类的哪些方法进行拦截
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    /*定义切点表达式，指定要拦截哪些包下面的哪些类，这些类下面的哪些方法，以及什么样的参数的方法会被拦截
      还指定返回值是怎么样的方法才会被拦截
      但是这样的拦截粒度太大了，这是我们还要在切点表达式里面规定只有添加了AutoFill注解的方法才会被拦截
      因为有一些查询方法它是不需要进行自动填充的
     */
    public void autoFillPointCut() {
    }


    /*
        切点（Pointcut）：切入点，用来定义哪些方法会被拦截。比如 autoFillPointCut() 方法中的表达式就是一个切入点定义。
        连接点（JoinPoint）：每一个被拦截到的方法执行点都被称为连接点。连接点可以理解为所有可能被拦截的“执行点”。
        切面（Aspect）：切面就是包含了切点和通知的类。在这个类中，AutoFillAspect 就是一个切面类。
     */



    /**
    前置通知，在通知中进行公共字段的赋值
     */
    @Before("autoFillPointCut()")    //指定它所对应的切点表达式，表示要对哪些方法进行增强，它拦截到的方法就会告诉给连接点
    public void autoFill(JoinPoint joinPoint) {    //通过连接点就可以知道什么方法被拦截到了，及其参数是什么样的
        log.info("开始进行公共字段的自动填充...");

        //获取当前被拦截的方法的数据库操作类型
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();   //获得方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);    //获得方法上的注解对象
        OperationType operationType = autoFill.value();     //获得数据库操作类型

        //获取当前被拦截方法大参数（实体对象）
        Object[] args = joinPoint.getArgs();
        if (args==null || args.length==0) {
            return;
        }
        Object entity = args[0];

        //准备赋值数据
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();

        //根据不同的操作类型，为对应的属性赋值
        if (operationType==OperationType.INSERT) {
            //插入类型，为四个字段赋值
            try {

                //获取实体所对应的方法
                Method setCreateTime = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, id);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, id);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else if (operationType==OperationType.UPDATE) {
            //更新类型，为两个字段赋值
            try {
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, id);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}
