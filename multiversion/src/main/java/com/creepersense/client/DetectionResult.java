package com.creepersense.client;

import java.util.List;

public record DetectionResult(float maxIntensity, List<DetectedCreeper> creepers) {}

