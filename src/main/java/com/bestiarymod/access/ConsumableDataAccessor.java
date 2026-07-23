package com.bestiarymod.access;

import java.util.Set;

public interface ConsumableDataAccessor {
    Set<String> getConsumedItems();
    void setConsumedItems(Set<String> items);
    default boolean hasConsumed(String key) {
        return getConsumedItems().contains(key);
    }
    default void markConsumed(String key) {
        Set<String> items = getConsumedItems();
        items.add(key);
        setConsumedItems(items);
    }
    default void removeConsumed(String key) {
        Set<String> items = getConsumedItems();
        items.remove(key);
        setConsumedItems(items);
    }
}
