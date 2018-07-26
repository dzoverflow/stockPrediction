package com.ckjava;

import com.ckjava.xutils.FileUtils;

public class TestDeleteDir extends FileUtils {
    public static void main(String[] args) {
        boolean flag = FileUtils.deleteDirectory("G:\\data\\20180627");
        System.out.println(flag);
    }
}
