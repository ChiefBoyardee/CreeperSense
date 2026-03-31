package com.creepersense.client;

/**
 * One creeper detected in the rear arc.
 *
 * @param entityId          creeper entity id (client-side)
 * @param intensity         0–1 threat strength
 * @param angleFromBehindRad signed radians, 0 = behind, + = behind-left, - = behind-right
 */
public record DetectedCreeper(int entityId, float intensity, float angleFromBehindRad) {}

