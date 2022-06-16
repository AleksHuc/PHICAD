package phi2cad;

/**
 * The class implements object that presents a single network traffic flow and its direction.
 */
public class FlowMessage {
    private String direction;
    private String[] flow;
    private String anomaly;

    /**
     * The constructor creates new FlowMessage object from given parameters.
     * @param flow String[] that presents the values of a single traffic flow.
     * @param direction String that presents the direction of a single traffic flow.
     */
    public FlowMessage(String[] flow, String direction, String anomaly) {
        this.flow = flow;
        this.direction = direction;
        this.anomaly = anomaly;
    }

    /**
     * The method returns the String that presents the direction of the flow.
     * @return String that presents flow direction.
     */
    public String getDirection() {
        return direction;
    }

    /**
     * The method returns the String array the presents the values of a single flow.
     * @return String[] that presents flow values.
     */
    public String[] getFlow() {
        return flow;
    }

    public String getAnomaly() {
        return anomaly;
    }
}
