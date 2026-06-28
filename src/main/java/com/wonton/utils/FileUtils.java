package com.wonton.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static Stream<String> open(String path) {
        try {
            Stream<String> lines = Files.lines(Path.of(path), StandardCharsets.UTF_8);
            return lines;
        } catch (IOException e) {
            throw new RuntimeException("无法打开文件" + path, e);
        }
    }

    public static String readSource(String path) {
        Stream<String> lines = open(path);
        return lines.collect(Collectors.joining("\n"));
    }
}
