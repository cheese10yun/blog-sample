package com.example.tobyspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//@SpringBootApplication
@Configuration
@ComponentScan
public class TobyspringApplication {

//    @Bean
//    public HelloController helloController(HelloService helloService){
//        return new HelloController(helloService);
//    }
//
//    @Bean
//    public HelloService helloService(){
//        return new SimpleHelloService();
//    }

    @Bean
    public ServletWebServerFactory servletWebServerFactory(){
        return new TomcatServletWebServerFactory();
    }

    @Bean
    public DispatcherServlet dispatcherServlet(){
        return new DispatcherServlet();
    }

    public static void main(String[] args) {
        final AnnotationConfigWebApplicationContext  applicationContext =  new AnnotationConfigWebApplicationContext() {
            @Override
            protected void onRefresh() {
                super.onRefresh();

                final ServletWebServerFactory serverFactory = this.getBean(ServletWebServerFactory.class);
                final DispatcherServlet dispatcherServlet = this.getBean(DispatcherServlet.class);
                dispatcherServlet.setApplicationContext(this);


                final WebServer webServer = serverFactory.getWebServer(servletContext -> {
                    servletContext.addServlet("dispatcherServlet", dispatcherServlet
                    ).addMapping("/*");
                });

                webServer.start();
            }
        };
//        applicationContext.registerBean(HelloController.class);
//        applicationContext.registerBean(SimpleHelloService.class);
        applicationContext.register(TobyspringApplication.class);
        applicationContext.refresh();


//        final WebServer webServer = serverFactory.getWebServer(servletContext -> {
////            final HelloController helloController = new HelloController();
//            servletContext.addServlet("frontcontroller", new HttpServlet() {
//                @Override
//                protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//                    if (req.getRequestURI().equals("/hello") && req.getMethod().equals(HttpMethod.GET.name())) {
//                        final String name = req.getParameter("name");
//
//                        final HelloController helloController = applicationContext.getBean(HelloController.class);
//
//                        final String ret = helloController.hello(name);
//                        resp.setStatus(HttpStatus.OK.value());
//                        resp.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
//                        resp.getWriter().println(ret);
//                    } else if (req.getRequestURI().equals("/user")) {
//                        //
//                    } else {
//                        resp.setStatus(HttpStatus.NOT_FOUND.value());
//                    }
//
//
//                }
//            }).addMapping("/hello");
//        });

    }
}
