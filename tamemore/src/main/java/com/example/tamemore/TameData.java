package com.example.tamemore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

/** Per-entity taming state stored as a NeoForge data attachment on ocelots and foxes. */
public record TameData(Optional<UUID> owner, boolean sitting) {
    public static final TameData EMPTY = new TameData(Optional.empty(), false);

    public static final MapCodec<TameData> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(TameData::owner),
            Codec.BOOL.optionalFieldOf("sitting", false).forGetter(TameData::sitting)
    ).apply(inst, TameData::new));

    public boolean isTamed() {
        return owner.isPresent();
    }

    public boolean isOwnedBy(Entity entity) {
        return owner.isPresent() && owner.get().equals(entity.getUUID());
    }

    public TameData withSitting(boolean sitting) {
        return new TameData(owner, sitting);
    }
}
