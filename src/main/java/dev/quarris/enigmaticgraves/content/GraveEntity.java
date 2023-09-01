package dev.quarris.enigmaticgraves.content;

import dev.quarris.enigmaticgraves.EnigmaticGraves;
import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import dev.quarris.enigmaticgraves.setup.Registry;
import dev.quarris.enigmaticgraves.utils.ClientHelper;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GraveEntity extends Entity {

    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(GraveEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> OWNER_NAME = SynchedEntityData.defineId(GraveEntity.class, EntityDataSerializers.STRING);

    private List<IGraveData> contents = new ArrayList<>();
    private boolean restored;

    public static GraveEntity createGrave(Player player, List<IGraveData> graveData) {
        GraveEntity grave = new GraveEntity(player);
        BlockPos.MutableBlockPos spawnPos = new BlockPos.MutableBlockPos();
        boolean spawnBlockBelow = GraveManager.getSpawnPosition(player.level, player.position(), spawnPos);
        if (spawnBlockBelow) {
            ResourceLocation blockName = new ResourceLocation(GraveConfigs.COMMON.graveFloorBlock.get());
            BlockState state = ForgeRegistries.BLOCKS.getValue(blockName).defaultBlockState();
            player.level.setBlock(spawnPos.below(), state, 3);
            player.level.levelEvent(2001, spawnPos, Block.getId(state));
        }
        grave.setRot(player.getXRot(), 0);
        grave.setPos(spawnPos.getX() + player.getBbWidth() / 2, spawnPos.getY(), spawnPos.getZ() + player.getBbWidth() / 2);
        grave.setContents(graveData);
        return grave;
    }

    public GraveEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public GraveEntity(Player player) {
        this(Registry.GRAVE_ENTITY_TYPE.get(), player.level);
        this.setOwner(player);
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide()) {
            if (GraveManager.getWorldGraveData(this.level).isGraveRestored(this.getUUID())) {
                this.remove(RemovalReason.DISCARDED);
                GraveManager.getWorldGraveData(this.level).removeGraveRestored(this.getUUID());
            }
        }
        super.tick();
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return this.level.isClientSide ? ClientHelper.shouldGlowOnClient(this) : super.isCurrentlyGlowing();
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER, Optional.empty());
        this.entityData.define(OWNER_NAME, "");
    }

    @Override
    public void playerTouch(Player player) {
        if (GraveConfigs.COMMON.sneakRetrieval.get() && player.isShiftKeyDown()) {
            if (this.canRetrieve(player)) {
                this.restoreGrave(player, player.getUUID().equals(this.getOwnerUUID()));
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.canRetrieve(player)) {
            this.restoreGrave(player, player.getUUID().equals(this.getOwnerUUID()));
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }

        if (player.isCreative()) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (heldItem.getItem() == Registry.GRAVE_FINDER_ITEM.get() && !heldItem.hasTag()) {
                this.remove(RemovalReason.DISCARDED);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    private void restoreGrave(Player player, boolean shouldReplace) {
        if (!this.isAlive() || this.level.isClientSide())
            return;

        // Remove the corresponding grave finder from the player inventory
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() == Registry.GRAVE_FINDER_ITEM.get()) {
                if (stack.hasTag()) {
                    CompoundTag nbt = stack.getTag();
                    if (nbt != null && nbt.contains("GraveUUID")) {
                        if (nbt.getUUID("GraveUUID").equals(this.getUUID())) {
                            player.getInventory().setItem(slot, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        }

        for (IGraveData data : this.contents) {
            data.restore(player, shouldReplace);
        }
        GraveManager.setGraveRestored(this.getOwnerUUID(), this);
        this.restored = true;
        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!this.level.isClientSide && !this.restored) {
            ModRef.LOGGER.warn("Grave at {} was removed without being restored!", this.blockPosition());
        }
    }

    private boolean canRetrieve(Player player) {
        return GraveConfigs.COMMON.allowNonOwnerRetrieval.get() || player.getUUID().equals(this.getOwnerUUID());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        CompoundTag graveNBT = new CompoundTag();
        ListTag contentNBT = new ListTag();
        for (IGraveData data : this.contents) {
            contentNBT.add(data.serializeNBT());
        }
        graveNBT.put("Content", contentNBT);
        if (this.getOwnerUUID() != null) {
            graveNBT.putUUID("Owner", this.getOwnerUUID());
        }
        if (!this.getOwnerName().isEmpty()) {
            graveNBT.putString("OwnerName", this.getOwnerName());
        }
        compound.put("Grave", graveNBT);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        CompoundTag graveNBT = compound.getCompound("Grave");

        List<IGraveData> dataList = new ArrayList<>();
        ListTag contentNBT = graveNBT.getList("Content", Tag.TAG_COMPOUND);
        for (Tag inbt : contentNBT) {
            CompoundTag dataNBT = (CompoundTag) inbt;
            ResourceLocation name = new ResourceLocation(dataNBT.getString("Name"));
            IGraveData data = GraveManager.GRAVE_DATA_SUPPLIERS.get(name).apply(dataNBT);
            dataList.add(data);
        }
        this.setContents(dataList);
        if (graveNBT.contains("Owner")) {
            this.entityData.set(OWNER, Optional.of(graveNBT.getUUID("Owner")));
        }
        if (graveNBT.contains("OwnerName")) {
            this.entityData.set(OWNER_NAME, graveNBT.getString("OwnerName"));
        }
    }

    public void setContents(List<IGraveData> contents) {
        this.contents = contents;
    }

    public void setOwner(Player owner) {
        this.entityData.set(OWNER, Optional.of(owner.getUUID()));
        this.entityData.set(OWNER_NAME, owner.getName().getString());
    }

    public String getOwnerName() {
        return this.entityData.get(OWNER_NAME);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER).orElse(null);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
