package com.foresightworks.projectmanagementsystemservice;

import com.foresightworks.projectmanagementsystemservice.data.AppData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
@Slf4j
public class ProjectManagementSystemServiceApplication implements CommandLineRunner {

    @Autowired
    private AppData appData;

    public static void main(String[] args) {
        SpringApplication.run(ProjectManagementSystemServiceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            appData.loadData();
            Thread.currentThread().join();
        } catch (InterruptedException exc) {
            log.error(String.format("EXCEPTION: %s : %s", exc.getMessage(), Arrays.toString(exc.getStackTrace())));
        }
    }
}
