package test.example2.model;

import java.time.LocalDate;

/** Classe simple (pas une entity) pour montrer le binding de LocalDate, enum, int, String. */
public class Event {
    private String title;
    private int attendees;
    private LocalDate date;
    private Level level;

    public Event() {
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getAttendees() { return attendees; }
    public void setAttendees(int attendees) { this.attendees = attendees; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }
}
