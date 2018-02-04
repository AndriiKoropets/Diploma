package com.koropets.diploma.chess;

import com.koropets.diploma.chess.process.service.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChessPsychologyApp implements CommandLineRunner{

    @Autowired
    private Process process;

    public static void main(String[] args) {
        SpringApplication.run(ChessPsychologyApp.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        System.out.println("Hello world");
        process.process();
    }
}
