package com.hospital.appointmentservice.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotUtil {

    private static final DateTimeFormatter TIME_FORMATTER_12H = 
        DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter TIME_FORMATTER_24H = 
        DateTimeFormatter.ofPattern("HH:mm");
    private static final int SLOT_DURATION_MINUTES = 30;

    public static List<String> generateAvailableSlots(LocalTime availableFrom, 
                                                       LocalTime availableTo) {
        List<String> slots = new ArrayList<>();
        LocalTime current = availableFrom;

        while (current.isBefore(availableTo)) {
            slots.add(current.format(TIME_FORMATTER_12H));
            current = current.plusMinutes(SLOT_DURATION_MINUTES);
        }

        return slots;
    }

    public static LocalTime parseTimeString(String timeString) {
        try {
            return LocalTime.parse(timeString, TIME_FORMATTER_12H);
        } catch (Exception e1) {
            try {
                return LocalTime.parse(timeString, TIME_FORMATTER_24H);
            } catch (Exception e2) {
                try {
                    return LocalTime.parse(timeString);
                } catch (Exception e3) {
                    throw new IllegalArgumentException(
                        "Unable to parse time string: " + timeString +
                        ". Expected format: HH:mm or hh:mm a");
                }
            }
        }
    }

    public static String formatTime(LocalTime time) {
        return time.format(TIME_FORMATTER_12H);
    }
}
