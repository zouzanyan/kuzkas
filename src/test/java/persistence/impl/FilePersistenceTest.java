package persistence.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FilePersistenceTest {

    @Test
    void save() {
        new FilePersistence().save();
    }
}