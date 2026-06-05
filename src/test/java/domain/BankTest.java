package domain;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BankTest {

    private Bank bankWithBrick(int amount) {
        Map<ResourceType, Integer> resources = new EnumMap<>(ResourceType.class);
        resources.put(ResourceType.BRICK, amount);
        resources.put(ResourceType.LUMBER, 0);
        resources.put(ResourceType.ORE, 0);
        resources.put(ResourceType.GRAIN, 0);
        resources.put(ResourceType.WOOL, 0);
        return new Bank(resources);
    }

    @Test
    void Constructor_DefaultBank_Has19OfEachResource() {
        Bank bank = new Bank();
        for (ResourceType type : ResourceType.values()) {
            assertEquals(19, bank.getResourceCount(type));
        }
    }

    @Test
    void GetResourceCount_WithNullType_ThrowsIllegalArgumentException() {
        Bank bank = new Bank();
        assertThrows(IllegalArgumentException.class, () -> bank.getResourceCount(null));
    }

    @Test
    void GetResourceCount_WithValidType_ReturnsCurrentCount() {
        Bank bank = bankWithBrick(5);
        assertEquals(5, bank.getResourceCount(ResourceType.BRICK));
    }

    @Test
    void Deduct_WithNullType_ThrowsIllegalArgumentException() {
        Bank bank = new Bank();
        assertThrows(IllegalArgumentException.class, () -> bank.deduct(null, 1));
    }

    @Test
    void Deduct_WithNegativeAmount_ThrowsIllegalArgumentException() {
        Bank bank = new Bank();
        assertThrows(IllegalArgumentException.class, () -> bank.deduct(ResourceType.BRICK, -1));
    }

    @Test
    void Deduct_WithZeroAmount_NoChange() {
        Bank bank = bankWithBrick(5);
        bank.deduct(ResourceType.BRICK, 0);
        assertEquals(5, bank.getResourceCount(ResourceType.BRICK));
    }

    @Test
    void Deduct_ExactlyAvailableAmount_LeavesZero() {
        Bank bank = bankWithBrick(5);
        bank.deduct(ResourceType.BRICK, 5);
        assertEquals(0, bank.getResourceCount(ResourceType.BRICK));
    }

    @Test
    void Deduct_OneMoreThanAvailable_ThrowsIllegalArgumentException() {
        Bank bank = bankWithBrick(5);
        assertThrows(IllegalArgumentException.class, () -> bank.deduct(ResourceType.BRICK, 6));
    }

    @Test
    void PackagePrivateConstructor_WithMissingResourceType_ThrowsIllegalArgumentException() {
        Map<ResourceType, Integer> incomplete = new EnumMap<>(ResourceType.class);
        incomplete.put(ResourceType.BRICK, 5);
        assertThrows(IllegalArgumentException.class, () -> new Bank(incomplete));
    }

    @Test
    void Collect_WithAmountZero_NoChange() {
        Bank bank = bankWithBrick(5);
        bank.collect(ResourceType.BRICK, 0);
        assertEquals(5, bank.getResourceCount(ResourceType.BRICK));
    }

    @Test
    void Collect_WithAmountOne_NoExceptionThrown() {
        Bank bank = bankWithBrick(5);
        bank.collect(ResourceType.BRICK, 1);
        assertEquals(6, bank.getResourceCount(ResourceType.BRICK));
    }

    @Test
    void Collect_ExactlyBelowMax_AtNineteen() {
        Bank bank = bankWithBrick(18);
        bank.collect(ResourceType.BRICK, 1);
        assertEquals(19, bank.getResourceCount(ResourceType.BRICK));
    }

    @Test
    void Collect_OneMoreThanAvailableSpace_ThrowsIllegalArgumentException() {
        Bank bank = bankWithBrick(18);
        assertThrows(IllegalArgumentException.class, () -> bank.collect(ResourceType.BRICK, 2));
    }

    @Test
    void Collect_FromZeroToOne_NoExceptionThrown() {
        Bank bank = bankWithBrick(0);
        bank.collect(ResourceType.BRICK, 1);
        assertEquals(1, bank.getResourceCount(ResourceType.BRICK));
    }

    @Test
    void Collect_WithNullType_ThrowsIllegalArgumentException() {
        Bank bank = new Bank();
        assertThrows(IllegalArgumentException.class, () -> bank.collect(null, 1));
    }

    @Test
    void Collect_WithNegativeAmount_ThrowsIllegalArgumentException() {
        Bank bank = new Bank();
        assertThrows(IllegalArgumentException.class, () -> bank.collect(ResourceType.BRICK, -1));
    }

    @Test
    void Collect_WhenResultExactlyNineteen_NoExceptionThrown() {
        Bank bank = bankWithBrick(17);
        bank.collect(ResourceType.BRICK, 2);
        assertEquals(19, bank.getResourceCount(ResourceType.BRICK));
    }
}
