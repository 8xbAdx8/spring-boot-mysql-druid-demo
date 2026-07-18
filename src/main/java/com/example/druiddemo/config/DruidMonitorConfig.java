package com.example.druiddemo.config;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DruidMonitorProperties.class)
public class DruidMonitorConfig {

    @Bean
    public ServletRegistrationBean<StatViewServlet> statViewServlet(DruidMonitorProperties properties) {
        DruidMonitorProperties.StatViewServletProperties config = properties.getStatViewServlet();
        ServletRegistrationBean<StatViewServlet> bean =
                new ServletRegistrationBean<>(new StatViewServlet(), config.getUrlPattern());

        bean.setEnabled(config.isEnabled());
        bean.addInitParameter("loginUsername", config.getLoginUsername());
        bean.addInitParameter("loginPassword", config.getLoginPassword());
        bean.addInitParameter("resetEnable", config.getResetEnable());
        bean.addInitParameter("allow", config.getAllow());
        bean.addInitParameter("deny", config.getDeny());
        return bean;
    }

    @Bean
    public FilterRegistrationBean<WebStatFilter> webStatFilter(DruidMonitorProperties properties) {
        DruidMonitorProperties.WebStatFilterProperties config = properties.getWebStatFilter();
        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>(new WebStatFilter());

        bean.setEnabled(config.isEnabled());
        bean.addUrlPatterns(config.getUrlPattern());
        bean.addInitParameter("exclusions", config.getExclusions());
        bean.addInitParameter("sessionStatEnable", config.getSessionStatEnable());
        bean.addInitParameter("sessionStatMaxCount", config.getSessionStatMaxCount());
        bean.addInitParameter("principalSessionName", config.getPrincipalSessionName());
        bean.addInitParameter("principalCookieName", config.getPrincipalCookieName());
        bean.addInitParameter("profileEnable", config.getProfileEnable());
        return bean;
    }
}
