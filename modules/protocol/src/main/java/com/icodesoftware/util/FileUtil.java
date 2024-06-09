package com.icodesoftware.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


public class FileUtil {

    public static boolean deleteDirectory(String dir){
        Path directory = Paths.get(dir);
        if (!Files.exists(directory)) return true;
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    Files.delete(path);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException {
                    Files.delete(directory);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        }catch (Exception ex){
            //ignore
            return false;
        }
    }

    public static File createFileIfNotExisted(String path){
        try {
            Path _path = Paths.get(path);
            if(!Files.exists(_path)) Files.createDirectories(_path);
            return _path.toFile();
        }catch (Exception ex){
            throw new RuntimeException("cannot create file ["+path+"]");
        }
    }
}
