package me.thecuddlybear.funlittlestuff.ai.brain.task;

import com.mojang.datafixers.util.Pair;
import me.thecuddlybear.funlittlestuff.ai.ModMemoryTypes;
import me.thecuddlybear.funlittlestuff.util.MemoryList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtil;

import java.util.List;

public class TeleportToTargetTask extends ExtendedBehaviour<MobEntity> {

    private static final MemoryList MEMORIES = MemoryList.create(1)
            .present(ModMemoryTypes.TELEPORT_TARGET);

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected boolean shouldKeepRunning(MobEntity entity) {
        return BrainUtil.hasMemory(entity, ModMemoryTypes.TELEPORT_TARGET);
    }

    @Override
    protected void tick(MobEntity entity) {
        Brain<?> brain = entity.getBrain();
        if(this.tryTeleport(entity, brain)) {
            BrainUtil.clearMemory(brain, ModMemoryTypes.TELEPORT_TARGET);
        }
    }

    private boolean tryTeleport(MobEntity entity, Brain<?> brain) {
        if(!BrainUtil.hasMemory(entity, ModMemoryTypes.TELEPORT_TARGET)) {
            return false;
        }

        Entity target = BrainUtil.getMemory(brain, ModMemoryTypes.TELEPORT_TARGET).entity();
        BlockPos pos = target.getBlockPos();

        for(int i = 0; i < 10; i++) {
            int j = entity.getRandom().nextBetween(-3, 3);
            int k = entity.getRandom().nextBetween(-3, 3);
            if(Math.abs(j) >= 2 || Math.abs(k) >= 2) {
                int l = entity.getRandom().nextBetween(-1, 1);
                if(this.tryTeleportTo(entity, pos.getX() + j, pos.getY() + l, pos.getZ() + k)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean tryTeleportTo(MobEntity entity, int x, int y, int z) {
        if(!this.canTeleportTo(entity, new BlockPos(x, y, z))) {
            return false;
        }

        entity.refreshPositionAndAngles(x + 0.5, y, z + 0.5, entity.getYaw(), entity.getPitch());
        entity.getNavigation().stop();
        return true;
    }

    private boolean canTeleportTo(MobEntity entity, BlockPos pos) {
        PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(entity, pos.mutableCopy());
        if(pathNodeType != PathNodeType.WALKABLE) {
            return false;
        }
        BlockPos distance = pos.subtract(entity.getBlockPos());
        return entity.getWorld().isSpaceEmpty(entity, entity.getBoundingBox().offset(distance));
    }
}
