package me.thecuddlybear.funlittlestuff.client.entity;

import me.thecuddlybear.funlittlestuff.entity.ShroomieEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ShroomieEntityRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<ShroomieEntity, R> {

    public ShroomieEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new ShroomieEntityModel());
    }
}
