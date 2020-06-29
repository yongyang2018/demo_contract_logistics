package com.yongyang.demo.controller;

import com.yongyang.demo.DemoConfig;
import com.yongyang.demo.Start;
import lombok.RequiredArgsConstructor;
import org.apache.tika.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StaticInterceptor implements HandlerInterceptor {
    private final DemoConfig demoConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Path path = Paths.get(request.getServletPath());

        for (Map.Entry<String, String> redirect : demoConfig.getRedirects()) {
            if (request.getServletPath().equals(redirect.getKey())) {
                response.sendRedirect(redirect.getValue());
                return false;
            }
        }

        for (Map.Entry<String, String> entry : demoConfig.getResources()) {
            if (path.startsWith(entry.getKey())) {
                Path r = Paths.get(entry.getKey()).relativize(path);
                Path p = Paths.get(entry.getValue(), r.toString());
                p = Paths.get("/").relativize(p);
                String s = p.toString().replaceAll("\\\\", "/");
                if(!s.startsWith("/"))
                    s = "/" + s;
                response.setContentType(Start.TIKA.detect(request.getServletPath()));
                IOUtils.copy(new FileInputStream(s), response.getOutputStream());
                response.getOutputStream().close();
                return false;
            }
        }

        for (Map.Entry<String, String> entry : demoConfig.getProxy()) {
            if (request.getServletPath().equals(entry.getKey())) {
                response.setContentType(Start.TIKA.detect(request.getServletPath()));
                IOUtils.copy(new FileInputStream(entry.getValue()), response.getOutputStream());
                response.getOutputStream().close();
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

}
