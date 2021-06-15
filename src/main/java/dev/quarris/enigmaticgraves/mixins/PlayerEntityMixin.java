package dev.quarris.enigmaticgraves.mixins;

import dev.quarris.enigmaticgraves.compat.CompatManager;
import dev.quarris.enigmaticgraves.compat.CurioCompat;
import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;
import java.util.stream.Collectors;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void spawnDrops(DamageSource damageSourceIn) {
        Entity entity = damageSourceIn.getTrueSource();
        PlayerEntity thisPlayer = (PlayerEntity) this.getEntity();

        int i = net.minecraftforge.common.ForgeHooks.getLootingLevel(this, entity, damageSourceIn);
        this.captureDrops(new java.util.ArrayList<>());

        boolean flag = this.recentlyHit > 0;
        if (!this.isChild() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            this.dropLoot(damageSourceIn, flag);
            this.dropSpecialItems(damageSourceIn, i, flag);
        }

        this.dropInventory();
        if (GraveConfigs.COMMON.experienceGraveHandling.get() == GraveConfigs.Common.ExperienceHandling.DROP) {
            this.dropExperience();
        }

        Collection<ItemEntity> drops = captureDrops(null);
        CompatManager.cacheModdedHandlers(thisPlayer);
        if (!net.minecraftforge.common.ForgeHooks.onLivingDrops(this, damageSourceIn, drops, i, recentlyHit > 0)) {
            GraveManager.populatePlayerGrave(thisPlayer, drops.stream().map(ItemEntity::getItem).collect(Collectors.toList()));
            //drops.forEach(e -> world.addEntity(e));
        }
        GraveManager.spawnPlayerGrave((PlayerEntity) this.getEntity());
    }
}
