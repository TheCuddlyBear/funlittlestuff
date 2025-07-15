package me.thecuddlybear.funlittlestuff.client.entity;

import me.thecuddlybear.funlittlestuff.Funlittlestuff;
import me.thecuddlybear.funlittlestuff.entity.ShroomieEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ShroomieEntityModel extends GeoModel<ShroomieEntity> {

    public static final DataTicket<Identifier> TEXTURE = DataTicket.create("texture", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.of(Funlittlestuff.MOD_ID, "shroomie");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState geoRenderState) {
        return geoRenderState.getGeckolibData(TEXTURE);
    }

    @Override
    public void addAdditionalStateData(ShroomieEntity animatable, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, renderState);
        Identifier text = animatable.getTexture();
        renderState.addGeckolibData(TEXTURE, text);
    }

    @Override
    public Identifier getAnimationResource(ShroomieEntity shroomieEntity) {
        return Identifier.of(Funlittlestuff.MOD_ID, "shroomie");
    }


}
