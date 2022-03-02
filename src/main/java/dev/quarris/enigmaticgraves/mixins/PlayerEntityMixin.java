package dev.quarris.enigmaticgraves.mixins;

import dev.quarris.enigmaticgraves.compat.CompatManager;
import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.stream.Collectors;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow protected abstract void dropEquipment();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void dropAllDeathLoot(DamageSource damageSourceIn) {
        Entity entity = damageSourceIn.getEntity();
        Player thisPlayer = (Player) (Entity) this;

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
        GraveManager.spawnPlayerGrave((Player) (Entity) this);
    }
}
