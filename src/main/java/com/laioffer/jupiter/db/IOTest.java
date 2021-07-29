package com.laioffer.jupiter.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class IOTest {

    public void test(int a, int b) throws MySQLException{
        if (a < b) {
            throw new MySQLException("invalid input");
        }
    }

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        String propFileName = "config.properties";
        InputStream inputStream = IOTest.class.getClassLoader().getResourceAsStream(propFileName);
        props.load(inputStream);
        System.out.println(props);

        IOTest ioTest = new IOTest();
        try {
            ioTest.test(1, 2);
        } catch (MySQLException e) {
            System.out.println(e.getMessage());
        }
    }


}
