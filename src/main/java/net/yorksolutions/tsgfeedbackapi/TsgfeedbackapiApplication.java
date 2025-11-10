package net.yorksolutions.tsgfeedbackapi;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TsgfeedbackapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TsgfeedbackapiApplication.class, args);
    }

//    @Bean
//    public NewTopic newTopic() {
//        return new NewTopic("testTopic1", 1, (short) 1);
//    }
}
