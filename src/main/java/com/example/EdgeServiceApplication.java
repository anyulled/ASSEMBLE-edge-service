package com.example;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

//@EnableBinding(Source.class)
@EnableZuulProxy
@SpringBootApplication
public class EdgeServiceApplication {
    @LoadBalanced
    @Bean
    RestOperations restOperations() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(EdgeServiceApplication.class, args);
    }
}

@RefreshScope
@RestController
class QuoteController {
    private final RestOperations restOperations;

    @Value("${quote}")
    private String defaultQuote;

//    private final Source source;
//
//    public QuoteController(RestOperations restOperations, Source source) {
//        this.restOperations = restOperations;
//        this.source = source;
//    }

    // Replace with constructor above when enabling Spring Cloud Stream + RabbitMQ
    public QuoteController(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    @HystrixCommand(fallbackMethod = "getDefaultQuote")
    @GetMapping("/quotorama")
    public Quote getRandomQuote() {
        return this.restOperations.getForObject("http://quote-service/random", Quote.class);
    }

    public Quote getDefaultQuote() {
        return new Quote(this.defaultQuote, "You & me both!");
    }

//    @PostMapping("/newquote")
//    public void addQuote(@RequestBody Quote quote) {
//        this.source.output().send(MessageBuilder.withPayload(quote).build());
//    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
class Quote {
    private Long id;
    private String text, source;

    public Quote(String text, String source) {
        this.text = text;
        this.source = source;
    }
}