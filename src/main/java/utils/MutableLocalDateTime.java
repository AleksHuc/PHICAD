package utils;

import java.time.LocalDateTime;

/**
 * The class implements the object that presents a mutable LocalDateTime.
 */
public class MutableLocalDateTime {

    private LocalDateTime localDateTime;

    /**
     * The constructor creates new MutableLocalDateTime object.
     * @param localDateTime LocalDateTime object that presents the new timestamp of MutableLocalDateTime.
     */
    public MutableLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    /**
     * The method returns the current value of MutableLocalDateTime.
     * @return LocalDateTime object that presents the current timestamp of MutableLocalDateTime.
     */
    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    /**
     * The method sets the current value of MutableLocalDateTime.
     * @param localDateTime LocalDateTime object that presents the new timestamp of MutableLocalDateTime.
     */
    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }
}
