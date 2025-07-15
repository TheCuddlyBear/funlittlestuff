package me.thecuddlybear.funlittlestuff.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.thecuddlybear.funlittlestuff.Funlittlestuff;
import me.thecuddlybear.funlittlestuff.ai.ModMemoryTypes;
import me.thecuddlybear.funlittlestuff.ai.brain.task.FollowOwnerTask;
import me.thecuddlybear.funlittlestuff.ai.brain.task.TeleportToTargetTask;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.BreedWithPartner;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowParent;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.InWaterSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyAdultSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtil;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ShroomieEntity extends TameableEntity implements GeoEntity, SmartBrainOwner<ShroomieEntity> {
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.shroomie.idle");
    protected static final RawAnimation SIT_ANIM = RawAnimation.begin().thenLoop("animation.shroomie.sitting");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public static final Predicate<LivingEntity> FOLLOW_TAMED_PREDICATE;
    public static final int RED_TYPE = 0;
    public static final int BLUE_TYPE = 1;
    public static final int GREEN_TYPE = 2;
    public static final int PURPLE_TYPE = 3;
    public static final int PINK_TYPE = 4;
    public static final Map<Integer, Identifier> TEXTURES;
    private static final TrackedData<Integer> TYPE;

    public ShroomieEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.setTamed(false, false);
    }

    // AI
    @Override
    public List<? extends ExtendedSensor<? extends ShroomieEntity>> getSensors() {
        return ObjectArrayList.of(
          new NearbyLivingEntitySensor<>(),
          new NearbyPlayersSensor<>(),
          new NearbyAdultSensor<>(),
          new InWaterSensor<>()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public BrainActivityGroup<? extends ShroomieEntity> getCoreTasks() {
        return new BrainActivityGroup<ShroomieEntity>(Activity.CORE)
                .priority(0)
                .behaviours(
                        new FloatToSurfaceOfFluid<>().riseChance(0.5f),
                        new TeleportToTargetTask(),
                        new FollowOwnerTask(),
                        new LookAtTarget<>().runFor(entity -> entity.getRandom().nextBetween(45, 90)),
                        new MoveToWalkTarget<>()
                                .startCondition(entity -> !BrainUtil.hasMemory(entity, ModMemoryTypes.TELEPORT_TARGET))
                                .stopIf(entity -> BrainUtil.hasMemory(entity, ModMemoryTypes.TELEPORT_TARGET))
                );
    }

    @Override
    @SuppressWarnings("unchecked")
    public BrainActivityGroup<? extends ShroomieEntity> getIdleTasks() {
        return new BrainActivityGroup<ShroomieEntity>(Activity.IDLE)
                .priority(10)
                .behaviours(
                        new BreedWithPartner<>(),
                        new FollowParent<>(),
                        // SET entity
                        new SetRandomLookTarget<>()
                                .lookTime(entity -> entity.getRandom().nextBetween(150, 250)),
                        new OneRandomBehaviour<>(
                                Pair.of(
                                        new SetRandomWalkTarget<ShroomieEntity>()
                                                .speedModifier(1.0F)
                                                .setRadius(24, 12),
                                        4
                                ),
                                Pair.of(
                                        new Idle<ShroomieEntity>().runFor(entity -> entity.getRandom().nextBetween(100, 300)),
                                        3
                                )
                        ).startCondition(entity -> !BrainUtil.hasMemory(entity, MemoryModuleType.WALK_TARGET))
                );
    }


    @Override
    public List<Activity> getActivityPriorities() {
        return ObjectArrayList.of(
                Activity.CORE,
                Activity.IDLE
        );
    }

    @Override
    protected Brain.Profile<?> createBrainProfile() {
        return new SmartBrainProvider<>(this);
    }

    public int getShroomieType() {
        return this.dataTracker.get(TYPE);
    }

    public void setShroomieType(int type) {
        if (type < 0 || type >= 5){
            type = this.random.nextInt(5);
        }

        this.dataTracker.set(TYPE, type);
    }

    public Identifier getTexture() {
        return TEXTURES.getOrDefault(this.getShroomieType(), TEXTURES.get(0));
    }

    static {
        FOLLOW_TAMED_PREDICATE = (entity) -> {
            EntityType<?> entityType = entity.getType();
            return entityType == EntityType.SHEEP || entityType == EntityType.MOOSHROOM;
        };
        TYPE = DataTracker.registerData(ShroomieEntity.class, TrackedDataHandlerRegistry.INTEGER);
        TEXTURES = new HashMap<>();

        TEXTURES.put(0, Identifier.of(Funlittlestuff.MOD_ID, "textures/entity/shroomie/shroomie_red.png"));
        TEXTURES.put(1, Identifier.of(Funlittlestuff.MOD_ID, "textures/entity/shroomie/shroomie_blue.png"));
        TEXTURES.put(2, Identifier.of(Funlittlestuff.MOD_ID, "textures/entity/shroomie/shroomie_green.png"));
        TEXTURES.put(3, Identifier.of(Funlittlestuff.MOD_ID, "textures/entity/shroomie/shroomie_purple.png"));
        TEXTURES.put(4, Identifier.of(Funlittlestuff.MOD_ID, "textures/entity/shroomie/shroomie_pink.png"));

    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData);
        if(world.getMoonSize() > 0.9f) {
            this.setShroomieType(this.random.nextInt(5));
        }else {
            this.setShroomieType(this.random.nextInt(4));
        }
        return entityData;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("ShroomieType", this.getShroomieType());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setShroomieType(view.getInt("ShroomieType",0));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TYPE, 0);
    }

    public static DefaultAttributeContainer.Builder createShroomieAttributes() {
        return TameableEntity.createMobAttributes().add(EntityAttributes.MOVEMENT_SPEED, 0.3D).add(EntityAttributes.MAX_HEALTH, 8.0D).add(EntityAttributes.ATTACK_DAMAGE, 2.0d);
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0D, (double)(0.6F * this.getStandingEyeHeight()), (double)(this.getWidth() * 0.4F));
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BAT_DEATH;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_BAT_HURT;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_HAPPY_GHAST_AMBIENT;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false; //TODO: Breeding
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        ShroomieEntity shroomieEntity = (ShroomieEntity)Funlittlestuff.SHROOMIE.create(world, SpawnReason.BREEDING);
        LivingEntity owner = this.getOwner();
        if (owner != null) {
            shroomieEntity.setOwner(owner);
            shroomieEntity.setTamed(true, true);
        }

        if (entity instanceof ShroomieEntity) {
            if (this.random.nextBoolean()) {
                shroomieEntity.setShroomieType(this.getShroomieType());
            } else {
                shroomieEntity.setShroomieType(((ShroomieEntity)entity).getShroomieType());
            }
        }

        return shroomieEntity;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
        if(this.getWorld().isClient){
            boolean bl = this.isOwner(player) || this.isTamed() || itemStack.isOf(Items.RED_MUSHROOM) || itemStack.isOf(Items.BROWN_MUSHROOM) && !this.isTamed();
            return bl ? ActionResult.CONSUME : ActionResult.PASS;
        }else {
            if(this.isTamed()){
                if(this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()){
                    if (!player.getAbilities().creativeMode) {
                        itemStack.decrement(1);
                    }

                    this.heal(1);
                    this.emitGameEvent(GameEvent.ENTITY_INTERACT);
                    return ActionResult.SUCCESS;
                }else if(this.isSitting()){
                    this.setSitting(false);
                    this.setInSittingPose(false);
                    return ActionResult.SUCCESS;
                }else {
                    this.setSitting(true);
                    this.setInSittingPose(true);
                    return ActionResult.SUCCESS;
                }
            } else if(itemStack.isOf(Items.RED_MUSHROOM) || itemStack.isOf(Items.BROWN_MUSHROOM)) {
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }

                if (this.random.nextInt(3) == 0) {
                    this.setOwner(player);
                    this.navigation.stop();
                    this.setTarget((LivingEntity)null);
                    this.setTamed(true, true);
                    this.setSitting(true);
                    this.getWorld().sendEntityStatus(this, (byte)7);
                } else {
                    this.getWorld().sendEntityStatus(this, (byte)6);
                }

                return ActionResult.SUCCESS;
            }
            return super.interactMob(player, hand);
        }
    }

    @Override
    public void setTamed(boolean tamed, boolean updateAttributes) {
        super.setTamed(tamed, updateAttributes);
        if (tamed) {
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(20.0D);
            this.setHealth(20.0F);
        } else {
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(8.0D);
        }

        this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
    }

    protected <E extends ShroomieEntity> PlayState predicate(final AnimationTest<E> animTest) {
        if(this.isInSittingPose()){
            return animTest.setAndContinue(SIT_ANIM);
        }
        if(animTest.isMoving()){
            return animTest.setAndContinue(IDLE_ANIM);
        }

        return animTest.setAndContinue(IDLE_ANIM);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<ShroomieEntity>("Predicate", 5, this::predicate));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        this.tickBrain(this);
        if(this.getServer() == null) {
            return;
        }

        if(!this.isTamed()) {
            return;
        }
    }
}