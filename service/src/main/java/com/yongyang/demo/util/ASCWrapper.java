package com.yongyang.demo.util;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.tika.io.IOUtils;

@RequiredArgsConstructor
public class ASCWrapper {
    private final String ascPath;

    public ASCWrapper(){
        this("asc");
    }

    @SneakyThrows
    public byte[] compile(String path){
        String cmd = ascPath + " " + path +  " --optimize -b";
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        byte[] error = IOUtils.toByteArray(p.getErrorStream());
        if(error != null && error.length > 0){
            throw new RuntimeException(new String(error));
        }
        return IOUtils.toByteArray(p.getInputStream());
    }
}
