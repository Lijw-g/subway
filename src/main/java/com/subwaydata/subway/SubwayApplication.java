package com.subwaydata.subway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @author: Lijiwen
 * Description:
 * @createDate
 **/
@SpringBootApplication(scanBasePackages="com.subwaydata.subway")
@ServletComponentScan
public class SubwayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubwayApplication.class, args);
    }

}
