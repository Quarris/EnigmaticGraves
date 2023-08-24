package dev.quarris.enigmaticgraves.mixins;

import dev.quarris.enigmaticgraves.compat.CompatManager;
import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Mixin(value = PlayerEntity.class, remap = false)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow protected abstract void dropEquipment();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void dropAllDeathLoot(DamageSource damageSourceIn) {
        ModRef.LOGGER.info("\"Dropping\" Player Loot");
        Entity entity = damageSourceIn.getEntity();
        PlayerEntity thisPlayer = (PlayerEntity) this.getEntity();

        int i = ForgeHooks.getLootingLevel(this, entity, damageSourceIn);
        this.captureDrops(new ArrayList<>());

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
        if (!ForgeHooks.onLivingDrops(this, damageSourceIn, drops, i, this.lastHurt > 0)) {
            GraveManager.populatePlayerGrave(thisPlayer, drops.stream().map(ItemEntity::getItem).collect(Collectors.toList()));
        } else {
            ModRef.LOGGER.info("Living Drops has been cancelled, grave will not populate.");
        }
        GraveManager.spawnPlayerGrave((PlayerEntity) this.getEntity());
    }
}
