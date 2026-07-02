package com.vqsv.rebuild.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ImageAssetReport {
    private final List<Integer> originalIds;
    private final List<Integer> decodedIds;
    private final List<Integer> missingDecodedIds;
    private final List<Integer> missingOriginalIds;

    ImageAssetReport(List<Integer> originalIds, List<Integer> decodedIds) {
        this.originalIds = sortedCopy(originalIds);
        this.decodedIds = sortedCopy(decodedIds);
        this.missingDecodedIds = difference(this.originalIds, this.decodedIds);
        this.missingOriginalIds = difference(this.decodedIds, this.originalIds);
    }

    public List<Integer> originalIds() {
        return originalIds;
    }

    public List<Integer> decodedIds() {
        return decodedIds;
    }

    public List<Integer> missingDecodedIds() {
        return missingDecodedIds;
    }

    public List<Integer> missingOriginalIds() {
        return missingOriginalIds;
    }

    public int originalCount() {
        return originalIds.size();
    }

    public int decodedCount() {
        return decodedIds.size();
    }

    public boolean isCompletePairing() {
        return missingDecodedIds.isEmpty() && missingOriginalIds.isEmpty();
    }

    public String summary() {
        return "imgInventory=original:" + originalCount()
                + " decoded:" + decodedCount()
                + " missingDecoded:" + missingDecodedIds.size()
                + " missingOriginal:" + missingOriginalIds.size();
    }

    public String missingSummary(int limit) {
        return "imgMissing=decoded" + limitedList(missingDecodedIds, limit)
                + " original" + limitedList(missingOriginalIds, limit);
    }

    private static List<Integer> sortedCopy(List<Integer> values) {
        List<Integer> copy = new ArrayList<>(values);
        Collections.sort(copy);
        return List.copyOf(copy);
    }

    private static List<Integer> difference(List<Integer> left, List<Integer> right) {
        List<Integer> missing = new ArrayList<>();
        for (Integer value : left) {
            if (!right.contains(value)) {
                missing.add(value);
            }
        }
        return List.copyOf(missing);
    }

    private static String limitedList(List<Integer> values, int limit) {
        if (values.isEmpty()) {
            return "[]";
        }
        int end = Math.min(values.size(), Math.max(0, limit));
        String suffix = values.size() > end ? "...+" + (values.size() - end) : "";
        return values.subList(0, end).toString() + suffix;
    }
}
