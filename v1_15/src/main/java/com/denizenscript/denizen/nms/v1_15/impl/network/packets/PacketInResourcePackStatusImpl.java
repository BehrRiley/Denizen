package com.denizenscript.denizen.nms.v1_15.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketInResourcePackStatus;
import net.minecraft.server.v1_15_R1.PacketPlayInResourcePackStatus;

public class PacketInResourcePackStatusImpl implements PacketInResourcePackStatus {

    private PacketPlayInResourcePackStatus internal;

    public PacketInResourcePackStatusImpl(PacketPlayInResourcePackStatus internal) {
        this.internal = internal;
    }

    @Override
    public String getStatus() {
        return internal.status.name();
    }
}
