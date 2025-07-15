package me.thecuddlybear.funlittlestuff.ai;

import com.mojang.serialization.Codec;
import me.thecuddlybear.funlittlestuff.Funlittlestuff;
import me.thecuddlybear.funlittlestuff.ai.brain.TeleportTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.Supplier;

public class ModMemoryTypes {
    public static void init(){}

    public static final MemoryModuleType<TeleportTarget> TELEPORT_TARGET = register("teleport_target");

    private static <U> MemoryModuleType<U> register(String id, Codec<U> codec) {
        return Registry.register(Registries.MEMORY_MODULE_TYPE, Identifier.of(Funlittlestuff.MOD_ID, id), new MemoryModuleType<>(Optional.of(codec)));
    }

    private static <U> MemoryModuleType<U> register(String id) {
        return Registry.register(Registries.MEMORY_MODULE_TYPE, Identifier.of(Funlittlestuff.MOD_ID, id), new MemoryModuleType<>(Optional.empty()));
    }

}
