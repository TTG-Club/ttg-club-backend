package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.bestiary.SenseApi;
import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.creature.SavingThrow;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BestiaryServiceImplTest {

    private final BestiaryServiceImpl service = new BestiaryServiceImpl(null, null, null, null, null, null);

    @Test
    void mapsSpeedWithoutNumericTruncation() {
        BeastDetailRequest request = requestWithSpeed(
                NameValueApi.builder().value(120).build(),
                NameValueApi.builder().name("летая").value(300).build());
        Creature creature = new Creature();

        ReflectionTestUtils.invokeMethod(service, "mapSpeed", creature, request);

        assertEquals(120, creature.getSpeed());
        assertEquals(Short.valueOf((short) 300), creature.getFlySpeed());
    }

    @Test
    void rejectsBaseSpeedOutsideByteRange() {
        BeastDetailRequest request = requestWithSpeed(NameValueApi.builder().value(128).build());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "mapSpeed", new Creature(), request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void rejectsAdditionalSpeedOutsideShortRange() {
        BeastDetailRequest request = requestWithSpeed(
                NameValueApi.builder().value(30).build(),
                NameValueApi.builder().name("летая").value(Short.MAX_VALUE + 1).build());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "mapSpeed", new Creature(), request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void parsesPassivePerceptionPrefix() {
        BeastDetailRequest request = requestWithPassivePerception("15 (+5)");

        byte passivePerception = ReflectionTestUtils.invokeMethod(service, "parsePassivePerception", request);

        assertEquals(15, passivePerception);
    }

    @Test
    void handlesLongNonNumericPassivePerceptionInLinearTime() {
        BeastDetailRequest request = requestWithPassivePerception(String.join("", Collections.nCopies(100_000, ",")));

        byte passivePerception = ReflectionTestUtils.invokeMethod(service, "parsePassivePerception", request);

        assertEquals(10, passivePerception);
    }

    @Test
    void removesDuplicateSavingThrowsByAbility() {
        List<NameValueApi> request = Arrays.asList(
                NameValueApi.builder().key(AbilityType.STRENGTH).value(3).build(),
                NameValueApi.builder().key(AbilityType.DEXTERITY).value(4).build(),
                NameValueApi.builder().key(AbilityType.STRENGTH).value(5).build());

        List<SavingThrow> savingThrows = ReflectionTestUtils.invokeMethod(service, "mapSavingThrows", request);

        assertEquals(2, savingThrows.size());
        assertEquals(AbilityType.STRENGTH, savingThrows.get(0).getAbility());
        assertEquals(5, savingThrows.get(0).getBonus());
        assertEquals(AbilityType.DEXTERITY, savingThrows.get(1).getAbility());
    }

    private BeastDetailRequest requestWithSpeed(NameValueApi... speed) {
        BeastDetailRequest request = new BeastDetailRequest();
        request.setSpeed(speed.length == 1 ? Collections.singletonList(speed[0]) : Arrays.asList(speed));
        return request;
    }

    private BeastDetailRequest requestWithPassivePerception(String passivePerception) {
        SenseApi senses = new SenseApi();
        senses.setPassivePerception(passivePerception);
        BeastDetailRequest request = new BeastDetailRequest();
        request.setSenses(senses);
        return request;
    }
}
