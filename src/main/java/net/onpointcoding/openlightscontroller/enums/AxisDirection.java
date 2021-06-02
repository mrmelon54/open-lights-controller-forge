package net.onpointcoding.openlightscontroller.enums;

import net.minecraft.util.IStringSerializable;

public enum AxisDirection implements IStringSerializable {
    X,
    Y,
    Z,
    None;

    public static AxisDirection getEnum(String value) {
        for (AxisDirection axis : AxisDirection.values())
            if (axis.name().equals(value)) return axis;
        return AxisDirection.None;
    }

    @Override
    public String getName() {
        return super.name().toLowerCase();
    }
}
