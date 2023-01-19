package com.drawinggame.backend.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/")
public class IndexController {

    public static class Guess {
        public final String type;
        public final String url;

        public Guess(String type, String url) {
            this.type = type;
            this.url = url;
        }

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }
    }

    private final List<Guess> guessList;
    private final Random random = new Random();

    public IndexController() {
        final File file = new File("src/main/resources/static/images");

        guessList = new ArrayList<>(file.list().length);

        for (String fileName : file.list()) {
            final String[] split = fileName.split("_");
            final String name = split[0];
            final String url = "http://192.168.0.101:8080/images/" + fileName;

            guessList.add(new Guess(name, url));
        }
    }

    @GetMapping("generate-guess")
    public Guess generateGuess() {
        final int index = random.nextInt(guessList.size());
        return guessList.get(index);
    }
}
