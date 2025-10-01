package com.kuruneru.latereminders.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
@EnableAutoConfiguration(exclude = {QuartzAutoConfiguration.class})
public class TestConfig {
    // This configuration excludes Quartz for test runs
}