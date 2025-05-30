package com.example.tourify_system_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
public class TourifySystemBeApplication {

	public static void main(String[] args) {
		// Đặt múi giờ mặc định là Asia/Ho_Chi_Minh
		TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Ho_Chi_Minh")));

		SpringApplication.run(TourifySystemBeApplication.class, args);
	}
}
