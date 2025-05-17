package com.example.bakalarkax.Clothes;

import android.util.Log;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClothingUsageTracker {
    private final List<String> wornDates;

    public ClothingUsageTracker(List<String> wornDatesRaw) {
        this.wornDates = new ArrayList<>();
        for (String date : wornDatesRaw) {
            this.wornDates.add(date.trim());
        }
    }



    public List<Boolean> getLastNDaysUsage(int days) {
        List<Boolean> usage = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < days; i++) {
            LocalDate day = today.minusDays(i);
            String formattedDay = day.format(formatter);
            usage.add(wornDates.contains(formattedDay));
        }

        return usage;
    }

    public List<String> getLastNDaysLabelsWithDates(int days) {
        List<String> labels = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("EEE");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.");

        for (int i = 0; i < days; i++) {
            LocalDate day = today.minusDays(i);
            String label = dateFormat.format(day) + "\n" + labelFormat.format(day);
            labels.add(label);
        }

        return labels;
    }
}

