package com.creepersense.client;

import com.creepersense.Tuning;

public final class ThreatState {
    private float targetIntensity;
    private float displayIntensity;

    private static final int MAX_TRACKED = 12;
    private final int[] ids = new int[MAX_TRACKED];
    private final float[] targetAngle = new float[MAX_TRACKED];
    private final float[] displayAngle = new float[MAX_TRACKED];
    private final float[] targetPerIntensity = new float[MAX_TRACKED];
    private final float[] displayPerIntensity = new float[MAX_TRACKED];
    private int count = 0;

    public void setTargetIntensity(float targetIntensity) {
        this.targetIntensity = Math.min(1f, Math.max(0f, targetIntensity));
    }

    public void setTargets(java.util.List<DetectedCreeper> creepers) {
        for (int i = 0; i < count; i++) targetPerIntensity[i] = 0f;

        for (DetectedCreeper c : creepers) {
            int idx = indexOfId(c.entityId());
            if (idx < 0) {
                idx = (count >= MAX_TRACKED) ? indexOfWeakest() : count++;
                ids[idx] = c.entityId();
                displayAngle[idx] = wrapToPi(c.angleFromBehindRad());
                targetAngle[idx] = displayAngle[idx];
                displayPerIntensity[idx] = 0f;
            }

            targetAngle[idx] = wrapToPi(c.angleFromBehindRad());
            targetPerIntensity[idx] = Math.min(1f, Math.max(0f, c.intensity()));
        }
    }

    public void smoothTowardTarget() {
        float t = Tuning.DISPLAY_SMOOTHING;
        displayIntensity += (targetIntensity - displayIntensity) * t;
        if (Math.abs(displayIntensity - targetIntensity) < 0.002f) displayIntensity = targetIntensity;

        int write = 0;
        for (int read = 0; read < count; read++) {
            float targetI = targetPerIntensity[read];
            float di = displayPerIntensity[read] + (targetI - displayPerIntensity[read]) * t;
            displayPerIntensity[read] = di;

            float delta = wrapToPi(targetAngle[read] - displayAngle[read]);
            displayAngle[read] = wrapToPi(displayAngle[read] + delta * t);

            boolean keep = di > 0.02f || targetI > 0.02f;
            if (keep) {
                if (write != read) {
                    ids[write] = ids[read];
                    targetAngle[write] = targetAngle[read];
                    displayAngle[write] = displayAngle[read];
                    targetPerIntensity[write] = targetPerIntensity[read];
                    displayPerIntensity[write] = displayPerIntensity[read];
                }
                write++;
            }
        }
        count = write;
    }

    public float displayIntensity() {
        return displayIntensity;
    }

    public int trackedCount() {
        return count;
    }

    public float angleAt(int idx) {
        return displayAngle[idx];
    }

    public float intensityAt(int idx) {
        return displayPerIntensity[idx];
    }

    private int indexOfId(int id) {
        for (int i = 0; i < count; i++) if (ids[i] == id) return i;
        return -1;
    }

    private int indexOfWeakest() {
        int wi = 0;
        float wv = displayPerIntensity[0];
        for (int i = 1; i < count; i++) {
            float v = displayPerIntensity[i];
            if (v < wv) {
                wv = v;
                wi = i;
            }
        }
        return wi;
    }

    private static float wrapToPi(float a) {
        float twoPi = (float) (Math.PI * 2.0);
        a %= twoPi;
        if (a <= -Math.PI) a += twoPi;
        if (a > Math.PI) a -= twoPi;
        return a;
    }
}

