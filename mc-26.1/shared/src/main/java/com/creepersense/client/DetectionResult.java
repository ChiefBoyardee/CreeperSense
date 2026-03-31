package com.creepersense.client;

import java.util.List;

/**
 * Shared result from detection.
 *
 * @param maxIntensity strongest threat intensity (0–1), used for global HUD strength
 * @param creepers     per-creeper results (angle/intensity), filtered to rear-arc and within range
 */
public record DetectionResult(float maxIntensity, List<DetectedCreeper> creepers) {}


