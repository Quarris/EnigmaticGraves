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
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.stream.Collectors;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow protected abstract void dropEquipment();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void dropAllDeathLoot(DamageSource damageSourceIn) {
        Entity entity = damageSourceIn.getEntity();
        PlayerEntity thisPlayer = (PlayerEntity) this.getEntity();

        int i = net.minecraftforge.common.ForgeHooks.getLootingLevel(this, entity, damageSourceIn);
        this.captureDrops(new java.util.ArrayList<>());

        boolean flag = this.lastHurt > 0;
        if (!this.isBaby() && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.dropFromLootTable(damageSourceIn, flag);
            this.dropCustomDeathLoot(damageSourceIn, i, flag);
        }

        this.dropEquipment();
        if (GraveConfigs.COMMON.experienceGraveHandling.get() == GraveConfigs.Common.ExperienceHandling.DROP) {
            this.dropExperience();
        }

        Collection<ItemEntity> drops = captureDrops(null);
        CompatManager.cacheModdedHandlers(thisPlayer);
        if (!net.minecraftforge.common.ForgeHooks.onLivingDrops(this, damageSourceIn, drops, i, this.lastHurt > 0)) {
            GraveManager.populatePlayerGrave(thisPlayer, drops.stream().map(ItemEntity::getItem).collect(Collectors.toList()));
        }
        GraveManager.spawnPlayerGrave((PlayerEntity) this.getEntity());
    }
}
