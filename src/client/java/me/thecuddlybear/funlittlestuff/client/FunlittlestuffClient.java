package me.thecuddlybear.funlittlestuff.client;

import me.thecuddlybear.funlittlestuff.Funlittlestuff;
import me.thecuddlybear.funlittlestuff.client.entity.ShroomieEntityModel;
import me.thecuddlybear.funlittlestuff.client.entity.ShroomieEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class FunlittlestuffClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(Funlittlestuff.SHROOMIE, ShroomieEntityRenderer::new);
    }
}
