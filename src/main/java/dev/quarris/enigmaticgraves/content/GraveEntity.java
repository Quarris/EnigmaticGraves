package dev.quarris.enigmaticgraves.content;

import dev.quarris.enigmaticgraves.config.GraveConfigs;
import dev.quarris.enigmaticgraves.grave.GraveManager;
import dev.quarris.enigmaticgraves.grave.data.IGraveData;
import dev.quarris.enigmaticgraves.setup.Registry;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
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

    private static final DataParameter<Optional<UUID>> OWNER = EntityDataManager.createKey(GraveEntity.class, DataSerializers.OPTIONAL_UNIQUE_ID);

    private List<IGraveData> contents = new ArrayList<>();

    public static GraveEntity createGrave(PlayerEntity player, List<IGraveData> graveData) {
        GraveEntity grave = new GraveEntity(player);
        BlockPos.Mutable spawnPos = new BlockPos.Mutable();
        boolean spawnBlockBelow = GraveManager.getSpawnPosition(player.world, player.getPositionVec(), spawnPos);
        if (spawnBlockBelow) {
            ResourceLocation blockName = new ResourceLocation(GraveConfigs.COMMON.graveFloorBlock.get());
            BlockState state = ForgeRegistries.BLOCKS.getValue(blockName).getDefaultState();
            player.world.setBlockState(spawnPos.down(), state, 3);
            player.world.playEvent(2001, spawnPos, Block.getStateId(state));
        }
        grave.setRotation(player.rotationYaw, 0);
        grave.setPositionAndUpdate(spawnPos.getX() + player.getWidth() / 2, spawnPos.getY(), spawnPos.getZ() + player.getWidth() / 2);
        grave.setContents(graveData);
        return grave;
    }

    public GraveEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    public GraveEntity(PlayerEntity player) {
        this(Registry.GRAVE_ENTITY_TYPE.get(), player.world);
        this.setOwner(player.getUniqueID());
    }

    @Override
    public void tick() {
        if (!this.world.isRemote) {
            if (GraveManager.getWorldGraveData(this.world).isGraveRestored(this.getUniqueID())) {
                this.remove();
                GraveManager.getWorldGraveData(this.world).removeGraveRestored(this.getUniqueID());
            }
        }
        super.tick();
    }

    @Override
    public boolean isGlowing() {
        if (this.world.isRemote && this.getOwner() != null && this.getOwnerUUID().equals(Minecraft.getInstance().player.getUniqueID())) {
            PlayerEntity player = this.getOwner();
            // Try to find the grave item in one of the hands, prioritising the main hand
            ItemStack stack = null;
            if (player.getHeldItemMainhand().getItem() == Registry.GRAVE_FINDER_ITEM.get()) {
                stack = player.getHeldItemMainhand();
            }
            if (stack == null && player.getHeldItemOffhand().getItem() != Registry.GRAVE_FINDER_ITEM.get()) {
                stack = player.getHeldItemOffhand();
            }

            if (stack == null)
                return false;

            if (!stack.hasTag() || !stack.getTag().contains("GraveUUID"))
                return false;

            UUID graveUUID = stack.getTag().getUniqueId("GraveUUID");
            if (this.getUniqueID().equals(graveUUID))
                return true;
        }
        return super.isGlowing();
    }

    @Override
    public PushReaction getPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    protected void registerData() {
        this.dataManager.register(OWNER, Optional.empty());
    }

    @Override
    public void onCollideWithPlayer(PlayerEntity player) {
        if (player.isSneaking()) {
            if (this.belongsTo(player)) {
                this.restoreGrave(player);
            }
        }
    }

    @Override
    public ActionResultType processInitialInteract(PlayerEntity player, Hand hand) {
        if (this.belongsTo(player)) {
            this.restoreGrave(player);
            return ActionResultType.func_233537_a_(this.world.isRemote);
        }

        return ActionResultType.PASS;
    }

    private void restoreGrave(PlayerEntity player) {
        if (!this.isAlive() || this.world.isRemote)
            return;

        // Remove the corresponding grave finder from the player inventory
        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (stack.getItem() == Registry.GRAVE_FINDER_ITEM.get()) {
                if (stack.hasTag()) {
                    CompoundNBT nbt = stack.getTag();
                    if (nbt != null && nbt.contains("GraveUUID")) {
                        if (nbt.getUniqueId("GraveUUID").equals(this.getUniqueID())) {
                            player.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        }

        for (IGraveData data : this.contents) {
            data.restore(player);
        }
        GraveManager.setGraveRestored(this.getOwnerUUID(), this);
        this.remove();
    }

    private boolean belongsTo(PlayerEntity player) {
        return player.getUniqueID().equals(this.getOwnerUUID());
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        CompoundNBT graveNBT = new CompoundNBT();
        ListNBT contentNBT = new ListNBT();
        for (IGraveData data : this.contents) {
            contentNBT.add(data.serializeNBT());
        }
        graveNBT.put("Content", contentNBT);
        if (this.getOwnerUUID() != null) {
            graveNBT.putUniqueId("Owner", this.getOwnerUUID());
        }
        compound.put("Grave", graveNBT);
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
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
            this.dataManager.set(OWNER, Optional.of(graveNBT.getUniqueId("Owner")));
        }
    }

    public void setContents(List<IGraveData> contents) {
        this.contents = contents;
    }

    public void setOwner(UUID ownerUUID) {
        this.dataManager.set(OWNER, Optional.of(ownerUUID));
    }

    @Nullable
    public PlayerEntity getOwner() {
        return this.dataManager.get(OWNER).map(this.world::getPlayerByUuid).orElse(null);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.dataManager.get(OWNER).orElse(null);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
