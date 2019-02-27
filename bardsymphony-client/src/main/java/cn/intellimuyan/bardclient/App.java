package cn.intellimuyan.bardclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@SpringBootConfiguration
@EnableAutoConfiguration
@Slf4j
public class App {

    @ComponentScan(basePackages = "cn.intellimuyan.bardclient.base")
    @Configuration
    static class CommonScanner {

    }

    @ComponentScan(basePackages = "cn.intellimuyan.bardclient.nettyclient")
    @Configuration
    static class NettyScanner {

    }

    @Profile("director")
    @ComponentScan(basePackages = "cn.intellimuyan.bardclient.director")
    @Configuration
    static class DirectorScanner {

    }

    @Profile("player")
    @ComponentScan(basePackages = "cn.intellimuyan.bardclient.player")
    @Configuration
    static class PlayerScanner {

    }

    public static void main(String[] args) {
        try {
            SpringApplicationBuilder builder = new SpringApplicationBuilder(App.class);
            builder.headless(false).web(WebApplicationType.NONE).run(args);
        } catch (RuntimeException e) {
            System.exit(-1);
        }

    }

}
