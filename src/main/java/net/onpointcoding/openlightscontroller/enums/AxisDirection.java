package net.onpointcoding.openlightscontroller.enums;

public enum AxisDirection {
    X,
    Y,
    Z,
    None;

    public static AxisDirection getEnum(String value) {
        for (AxisDirection axis : AxisDirection.values())
            if (axis.name().equals(value)) return axis;
        return AxisDirection.None;
    }
}
