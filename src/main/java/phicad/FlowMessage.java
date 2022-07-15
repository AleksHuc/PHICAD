package phicad;

import java.time.LocalDateTime;

/**
 * The class implements object that presents a single network traffic flow, more specifically its attributes, direction and timestamp.
 */
public class FlowMessage {
    private String direction;
    private String[] flow;
    private LocalDateTime timestamp;

    /**
     * The constructor creates new FlowMessage object from given parameters.
     * @param flow String[] array that presents the attributes of a single network flow.
     * @param direction String object that presents the direction of a single network flow.
     * @param timestamp LocalDateTime object that presents the timestamp of a single network flow.
     */
    public FlowMessage(String[] flow, String direction, LocalDateTime timestamp) {
        this.flow = flow;
        this.direction = direction;
        this.timestamp = timestamp;
    }

    /**
     * The method returns the String that presents the direction of the flow.
     * @return String object that presents the direction of a single network flow.
     */
    public String getDirection() {
        return direction;
    }

    /**
     * The method returns the String array the presents the attributes of a single network flow.
     * @return String[] array that presents the attributes of a single network flow.
     */
    public String[] getFlow() {
        return flow;
    }

    /**
     *
     * @return LocalDateTime object that presents the timestamp of a single network flow.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
