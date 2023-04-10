package com.cleanroommc.groovyscript.api;

import org.jetbrains.annotations.NotNull;

/**
 * An object that has an amount.
 * Can use '*' in groovy to set amount.
 */
public interface IResourceStack {

    int getAmount();

    void setAmount(int amount);

    default @NotNull IResourceStack withAmount(int amount) {
        setAmount(amount);
        return this;
    }

    /**
     * enables groovy to use '*' operator
     */
    default IResourceStack multiply(@NotNull Number n) {
        return withAmount(n.intValue());
    }
}
