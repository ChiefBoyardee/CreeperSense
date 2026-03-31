package com.creepersense;

/**
 * Detection and HUD defaults (single place so Fabric/NeoForge builds stay aligned).
 */
public final class Tuning {

    /** Horizontal distance (blocks) to scan for creepers. */
    public static final double SEARCH_RADIUS = 14.0;

    /**
     * Normalized horizontal dot product (player look XZ vs vector to creeper XZ) must be at or below this to count as
     * "behind". -cos(70°) ≈ -0.34 limits to a ~140° rear arc (stricter than full 180° back hemisphere).
     */
    public static final double BEHIND_DOT_THRESHOLD = -0.3420201433256687;

    /** Horizontal distance at or below which the distance factor is treated as fully "close". */
    public static final double CLOSE_DISTANCE = 2.5;

    /** Display intensity lerps toward the target by this fraction each client tick (higher = snappier). */
    public static final float DISPLAY_SMOOTHING = 0.22f;

    /** Peak HUD opacity multiplier when intensity is 1. */
    public static final float HUD_MAX_ALPHA = 0.36f;

    /** Extra intensity from creeper swelling (0–1), scaled before adding to distance-based intensity. */
    public static final float SWELL_INTENSITY_SCALE = 0.45f;

    /**
     * Vertical falloff for threats: creepers far above/below you are much less actionable (different floors/caves).
     * We fade intensity from 100% at {@link #VERTICAL_FADE_START} down to 0% at {@link #VERTICAL_FADE_END}.
     */
    public static final double VERTICAL_FADE_START = 3.0;
    public static final double VERTICAL_FADE_END = 10.0;

    /** Arc radius for the chevron indicator (pixels). */
    public static final int HUD_ARC_RADIUS_MIN_PX = 60;

    /** Arc radius as a fraction of screen height (used if larger than min). */
    public static final float HUD_ARC_RADIUS_SCREEN_HEIGHT_FRACTION = 0.28f;

    /** Downward offset for the arc center relative to the reticle (fraction of screen height). */
    public static final float HUD_ARC_CENTER_Y_OFFSET_SCREEN_HEIGHT_FRACTION = 0.10f;

    private Tuning() {}
}
