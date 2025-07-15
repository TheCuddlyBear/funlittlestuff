package me.thecuddlybear.funlittlestuff.ai.brain.task;

import com.mojang.datafixers.util.Pair;
import me.thecuddlybear.funlittlestuff.ai.ModMemoryTypes;
import me.thecuddlybear.funlittlestuff.ai.brain.TeleportTarget;
import me.thecuddlybear.funlittlestuff.util.MemoryList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;
import java.util.function.Function;

public class FollowOwnerTask extends ExtendedBehaviour<TameableEntity> {

    private static final MemoryList MEMORIES = MemoryList.create(1)
            .registered(
                    ModMemoryTypes.TELEPORT_TARGET,
                    MemoryModuleType.LOOK_TARGET,
                    MemoryModuleType.WALK_TARGET
            );

    private LivingEntity owner;
    private int updateCountdownTicks;
    protected Function<LivingEntity, Float> speedModifier = (entity) -> 1.0F;
    protected UniformIntProvider range = UniformIntProvider.create(5, 10);

    public FollowOwnerTask speedModifier(float modifier) {
        return this.speedModifier(entity -> modifier);
    }

    public FollowOwnerTask speedModifier(Function<LivingEntity, Float> function) {
        this.speedModifier = function;

        return this;
    }

    public FollowOwnerTask range(int min, int max) {
        this.range = UniformIntProvider.create(min, max);

        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected boolean shouldRun(ServerWorld world, TameableEntity entity) {
        LivingEntity owner = entity.getOwner();
        if(owner == null) {
            return false;
        }
        if(owner.isSpectator()) {
            return false;
        }
        if(entity.isSitting()) {
            return false;
        }
        if(entity.squaredDistanceTo(owner) < this.range.getMin() * this.range.getMin()) {
            return false;
        }
        /*if(entity.getRecipientUuid() != null) {
            return false;
        }*/
        this.owner = owner;
        return super.shouldRun(world, entity);
    }

    @Override
    protected boolean shouldKeepRunning(TameableEntity entity) {
        if(entity.isSitting() || this.owner == null) {
            return false;
        }
        
        double distance = entity.squaredDistanceTo(this.owner);
        return distance > (this.range.getMin() * this.range.getMin());
    }

    @Override
    protected void tick(TameableEntity entity) {
        Brain<?> brain = entity.getBrain();
        BrainUtil.setMemory(brain, MemoryModuleType.LOOK_TARGET, new EntityLookTarget(this.owner, true));
        
        if(--this.updateCountdownTicks <= 0) {
            this.updateCountdownTicks = 10;
            if(!entity.isLeashed() && !entity.hasVehicle()) {
                double distance = entity.squaredDistanceTo(this.owner);
                
                if(distance >= 144.0) { // 12 blocks squared
                    BrainUtil.setMemory(brain, ModMemoryTypes.TELEPORT_TARGET, new TeleportTarget(this.owner));
                    BrainUtil.clearMemory(brain, MemoryModuleType.WALK_TARGET);
                } else {
                    BrainUtil.clearMemory(brain, ModMemoryTypes.TELEPORT_TARGET);
                    BrainUtil.setMemory(brain, MemoryModuleType.WALK_TARGET, 
                        new WalkTarget(this.owner, this.speedModifier.apply(entity), 2));
                }
            }
        }
    }
}