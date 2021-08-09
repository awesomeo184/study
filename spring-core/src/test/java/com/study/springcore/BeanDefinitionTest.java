package com.study.springcore;

import com.study.springcore.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class BeanDefinitionTest {

    @Test
    void beanDefinition() {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(
            AppConfig.class);
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);
            System.out.println(beanDefinition.getFactoryMethodName());
        }
    }
}
