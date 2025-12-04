package com.documentpreview.shared.ddd;

import java.util.Objects;

/**
 * Base class for all value objects in the domain model.
 * Value objects are immutable and equality is based on their attributes.
 */
public abstract class ValueObject {
    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    /**
     * Helper method to compare two objects for equality, handling nulls.
     * 
     * @param a First object
     * @param b Second object
     * @return true if both objects are equal, false otherwise
     */
    protected static boolean equals(Object a, Object b) {
        return Objects.equals(a, b);
    }

    /**
     * Helper method to generate a hash code from multiple attributes.
     * 
     * @param attributes The attributes to include in the hash code
     * @return The generated hash code
     */
    protected static int hashCode(Object... attributes) {
        return Objects.hash(attributes);
    }
}