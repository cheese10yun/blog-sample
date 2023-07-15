package com.example.tobyspring;

import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class MyApplication {

     static void extracted(Class<?> applicationClass, String... args) {
        final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext() {
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
        applicationContext.register(applicationClass);
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
