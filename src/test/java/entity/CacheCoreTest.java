package entity;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CacheCoreTest {

    @Test
    void uploadFile() {

        CacheCore cacheCore = new CacheCore();
        byte[] bytes = "iloveyou".getBytes(StandardCharsets.UTF_8);
        cacheCore.uploadFile("uploadDir", "/../a.txt", bytes, 0);

    }
}