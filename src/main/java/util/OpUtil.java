package util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OpUtil {
    public static void main(String[] args) {
        Path upload_files = Paths.get("upload_files");
        Path test = upload_files.resolve("a.txt").normalize();
        System.out.println(test.toAbsolutePath());
    }
}
