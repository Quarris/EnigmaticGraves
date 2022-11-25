package dev.quarris.enigmaticgraves.content;

import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import dev.quarris.enigmaticgraves.setup.Registry;
import dev.quarris.enigmaticgraves.utils.ClientHelper;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GraveEntity extends Entity {

    private static final DataParameter<Optional<UUID>> OWNER = EntityDataManager.defineId(GraveEntity.class, DataSerializers.OPTIONAL_UUID);
    private static final DataParameter<String> OWNER_NAME = EntityDataManager.defineId(GraveEntity.class, DataSerializers.STRING);

    private List<IGraveData> contents = new ArrayList<>();
    private boolean restored;

    public static GraveEntity createGrave(PlayerEntity player, List<IGraveData> graveData) {
        GraveEntity grave = new GraveEntity(player);
        BlockPos.Mutable spawnPos = new BlockPos.Mutable();
        boolean spawnBlockBelow = GraveManager.getSpawnPosition(player.level, player.position(), spawnPos);
        if (spawnBlockBelow) {
            ResourceLocation blockName = new ResourceLocation(GraveConfigs.COMMON.graveFloorBlock.get());
            BlockState state = ForgeRegistries.BLOCKS.getValue(blockName).defaultBlockState();
            player.level.setBlock(spawnPos.below(), state, 3);
            player.level.levelEvent(2001, spawnPos, Block.getId(state));
        }
        grave.setRot(player.xRot, 0);
        grave.setPos(spawnPos.getX() + player.getBbWidth() / 2, spawnPos.getY(), spawnPos.getZ() + player.getBbWidth() / 2);
        grave.setContents(graveData);
        ModRef.LOGGER.info("Creating Grave Entity");
        return grave;
    }

    public GraveEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    public GraveEntity(PlayerEntity player) {
        this(Registry.GRAVE_ENTITY_TYPE.get(), player.level);
        this.setOwner(player);
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide()) {
            if (GraveManager.getWorldGraveData(this.level).isGraveRestored(this.getUUID())) {
                this.remove();
                GraveManager.getWorldGraveData(this.level).removeGraveRestored(this.getUUID());
            }
        }
        super.tick();
    }

    @Override
    public boolean isGlowing() {
        return this.level.isClientSide ? ClientHelper.shouldGlowOnClient(this) : super.isGlowing();
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
    public void playerTouch(PlayerEntity player) {
        if (GraveConfigs.COMMON.sneakRetrieval.get() && player.isShiftKeyDown()) {
            if (this.belongsTo(player)) {
                this.restoreGrave(player);
            }
        }
    }

    @Override
    public ActionResultType interact(PlayerEntity player, Hand hand) {
        if (this.belongsTo(player)) {
            this.restoreGrave(player);
            return ActionResultType.sidedSuccess(this.level.isClientSide);
        }

        if (player.isCreative()) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (heldItem.getItem() == Registry.GRAVE_FINDER_ITEM.get() && !heldItem.hasTag()) {
                this.remove();
                return ActionResultType.sidedSuccess(this.level.isClientSide);
            }
        }

        return ActionResultType.PASS;
    }

    private void restoreGrave(PlayerEntity player) {
        if (!this.isAlive() || this.level.isClientSide())
            return;

        // Remove the corresponding grave finder from the player inventory
        for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
            ItemStack stack = player.inventory.getItem(slot);
            if (stack.getItem() == Registry.GRAVE_FINDER_ITEM.get()) {
                if (stack.hasTag()) {
                    CompoundNBT nbt = stack.getTag();
                    if (nbt != null && nbt.contains("GraveUUID")) {
                        if (nbt.getUUID("GraveUUID").equals(this.getUUID())) {
                            player.inventory.setItem(slot, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        }

        for (IGraveData data : this.contents) {
            ModRef.LOGGER.info("Restoring {}", data.getName());
            data.restore(player);
        }
        GraveManager.setGraveRestored(this.getOwnerUUID(), this);
        this.restored = true;
        this.remove();
    }

    @Override
    public void remove(boolean keepData) {
        if (!this.level.isClientSide && !this.restored) {
            ModRef.LOGGER.warn("Grave at {} was removed without being restored!", this.blockPosition());
        }
        super.remove(keepData);
    }

    private boolean belongsTo(PlayerEntity player) {
        return player.getUUID().equals(this.getOwnerUUID());
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {
        CompoundNBT graveNBT = new CompoundNBT();
        ListNBT contentNBT = new ListNBT();
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
    protected void readAdditionalSaveData(CompoundNBT compound) {
        CompoundNBT graveNBT = compound.getCompound("Grave");

        List<IGraveData> dataList = new ArrayList<>();
        ListNBT contentNBT = graveNBT.getList("Content", Constants.NBT.TAG_COMPOUND);
        for (INBT inbt : contentNBT) {
            CompoundNBT dataNBT = (CompoundNBT) inbt;
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

    public void setOwner(PlayerEntity owner) {
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
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
