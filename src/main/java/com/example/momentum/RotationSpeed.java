
package com.example.momentum;

/**
 * FAST  = aggressive rotation: always jump to the current top-N each month.
 * SLOW  = stickier rotation: keep existing holdings as long as they remain reasonably strong.
 */
public enum RotationSpeed {
    FAST,
    SLOW
}
