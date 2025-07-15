package me.thecuddlybear.funlittlestuff;

import me.thecuddlybear.funlittlestuff.ai.ModMemoryTypes;
import me.thecuddlybear.funlittlestuff.entity.ShroomieEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class Funlittlestuff implements ModInitializer {

    public static final String MOD_ID = "funlittlestuff";

    public static final RegistryKey<EntityType<?>> SHROOMIE_KEY = RegistryKey.of(
            Registries.ENTITY_TYPE.getKey(),
            Identifier.of(MOD_ID, "shroomie")
    );

    //Entities TODO: temp location
    public static final EntityType<ShroomieEntity> SHROOMIE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "shroomie"),
            EntityType.Builder.create(ShroomieEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6f, 0.6f)
                    .build(SHROOMIE_KEY)
    );

    @Override
    public void onInitialize() {
        ModMemoryTypes.init();
        FabricDefaultAttributeRegistry.register(SHROOMIE, ShroomieEntity.createShroomieAttributes());
    }
}
