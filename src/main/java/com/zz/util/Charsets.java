package com.zz.util;

import sun.nio.cs.StandardCharsets;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;

public class Charsets {
    public static final Charset UTF_8;
    public static final Charset GBK;
    static{
        CharsetProvider standardProvider = new StandardCharsets();
        UTF_8 = standardProvider.charsetForName("UTF-8");
        GBK = standardProvider.charsetForName("GBK");
    }
}
