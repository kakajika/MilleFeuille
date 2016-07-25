package com.labo.kaji.millefeuille;

/**
 * @author kakajika
 * @since 15/08/08.
 * Interface for applying item's shade level.
 */
public interface Shadeable {

    /**
     * Apply shade level.
     * @param shadowLevel 0.0f to 1.0f
     */
    void setShadeLevel(float shadowLevel);

    /**
     * Get applied shadow level.
     * @return Shadow level
     */
    float getShadeLevel();

}
