package com.yongyang.demo;

import org.springframework.core.env.Environment;

public class Constants {
    private static String entryPoint;

    public static String getEntryPoint() {
        return entryPoint;
    }

    public static void init(Environment environment) {
        String e = environment.getProperty("demo-config.entry-point");
        if (e == null || e.isEmpty())
            e = "http://localhost:8080";

        while (e.endsWith("/")) {
            e = e.substring(0, e.length() - 1);
        }
        Constants.entryPoint = e;
    }
}
