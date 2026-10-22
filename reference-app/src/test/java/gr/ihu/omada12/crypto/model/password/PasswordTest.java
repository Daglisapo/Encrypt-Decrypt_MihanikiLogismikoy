package gr.ihu.omada12.crypto.model.password;

import gr.ihu.omada12.crypto.model.cipher.CryptoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** VLT-01, LAB-07, LAB-08 */
class PasswordTest {
    private final PasswordStrengthMeter meter = new PasswordStrengthMeter();

    @Test
    void commonAndShortPasswordsAreWeak() {
        assertEquals(0, meter.evaluate("123456").getScore());
        assertEquals(0, meter.evaluate("password").getScore());
        assertEquals(0, meter.evaluate("").getScore());
        assertTrue(meter.evaluate("password").getHintKeys().contains("pw.hint.common"));
    }

    @Test
    void longMixedPasswordIsStrongAndTakesCenturies() {
        PasswordStrength s = meter.evaluate("T7#qL!x9@vR2$mZp");
        assertTrue(s.getScore() >= 3, "score=" + s.getScore());
        assertEquals(CrackTime.Unit.CENTURIES, s.getCrackTime().getUnit());
    }

    @Test
    void repetitionsAndSequencesCountLess() {
        assertTrue(meter.evaluate("aaaaaaaaaaaa").getEntropyBits() < meter.evaluate("qhzmxkrwbtvn").getEntropyBits());
        assertTrue(meter.evaluate("abcdefghijkl").getEntropyBits() < meter.evaluate("qhzmxkrwbtvn").getEntropyBits());
    }

    @Test
    void crackTimePicksReadableUnit() {
        assertEquals(CrackTime.Unit.INSTANT, new CrackTime(0.2).getUnit());
        assertEquals(CrackTime.Unit.MINUTES, new CrackTime(150).getUnit());
        assertEquals(2.5, new CrackTime(150).getAmount(), 1e-9);
        assertEquals(CrackTime.Unit.DAYS, new CrackTime(3 * 86_400).getUnit());
    }

    @Test
    void generatorRespectsPolicy() throws CryptoException {
        PasswordGenerator gen = new PasswordGenerator();
        String pw = gen.generate(new PasswordPolicy(20, true, true, true, true));
        assertEquals(20, pw.length());
        assertTrue(pw.chars().anyMatch(Character::isLowerCase));
        assertTrue(pw.chars().anyMatch(Character::isUpperCase));
        assertTrue(pw.chars().anyMatch(Character::isDigit));
        assertTrue(pw.chars().anyMatch(c -> PasswordGenerator.SYMBOLS.indexOf(c) >= 0));

        String digitsOnly = gen.generate(new PasswordPolicy(12, false, false, true, false));
        assertTrue(digitsOnly.chars().allMatch(Character::isDigit));
    }

    @Test
    void generatorRejectsBadPolicy() {
        PasswordGenerator gen = new PasswordGenerator();
        assertThrows(CryptoException.class, () -> gen.generate(new PasswordPolicy(16, false, false, false, false)));
        assertThrows(CryptoException.class, () -> gen.generate(new PasswordPolicy(4, true, true, true, true)));
    }
}
