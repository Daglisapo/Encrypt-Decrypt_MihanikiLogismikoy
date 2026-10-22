package gr.ihu.omada12.crypto.model.cipher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** ENC-01, NFR-11: κάθε όνομα της λίστας δίνει τον σωστό αλγόριθμο. */
class CipherFactoryTest {

    @Test
    void everyListedAlgorithmCanBeCreated() {
        for (String name : CipherFactory.availableAlgorithms()) {
            assertEquals(name, CipherFactory.create(name).getName());
        }
    }

    @Test
    void unknownAlgorithmIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> CipherFactory.create("ROT13"));
    }
}
