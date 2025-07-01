package com.example.webhooksite.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

@Service
public class EndpointNameGenerator {
    private final String[] firstWords;
    private final String[] secondWords;
    private final Random random = new Random();

    public EndpointNameGenerator(@Value("classpath:words.properties") Resource wordList) throws IOException {
        Properties props = new Properties();
        props.load(wordList.getInputStream());
        
        firstWords = props.getProperty("first").split(",");
        secondWords = props.getProperty("second").split(",");
    }

    public String generateEndpointName() {
        String first = firstWords[random.nextInt(firstWords.length)];
        String second = secondWords[random.nextInt(secondWords.length)];
        return first + "-" + second;
    }
}
