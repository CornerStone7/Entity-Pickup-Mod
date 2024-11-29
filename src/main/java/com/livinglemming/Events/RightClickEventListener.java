package com.livinglemming.Events;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class RightClickEventListener
{
    public static void registerRightClickEvent()
    {
        UseEntityCallback.EVENT.register(((playerEntity, world, hand, entity, entityHitResult) ->
        {
            if (!world.isClient() && playerEntity.isSneaking() && hand == Hand.MAIN_HAND)
            {
                if (playerEntity instanceof ServerPlayerEntity)
                {
                    EntityType<?> entityType = entity.getType();
                    Item spawnEgg = SpawnEggItem.forEntity(entityType);
                    if (spawnEgg != null)
                    {
                        ItemStack spawnEggStack = new ItemStack(spawnEgg);

                        // Get Entity NBT.
                        NbtCompound nbt = new NbtCompound();
                        entity.writeNbt(nbt);

                        // Remove attributes to enable cloning of same NBT entity.
                        nbt.remove("UUID");
                        nbt.remove("Pos");
                        nbt.remove("Motion");
                        nbt.remove("Rotation");

                        NbtCompound nbtCompound = new NbtCompound();
                        nbtCompound.put("EntityTag", nbt);

                        // Lore Attribute.
                        if (entity instanceof VillagerEntity villager)
                        {
                            NbtCompound textCompound = new NbtCompound();
                            NbtList tooltipList = new NbtList();

                            tooltipList.add(NbtString.of("{\"text\":\"Profession: [" + villager.getVillagerData().getProfession().toString() + "]\",\"color\":\"gray\",\"italic\":false}"));
                            textCompound.put("Lore", tooltipList);
                            nbtCompound.put("display", textCompound);
                        }

                        spawnEggStack.setNbt(nbtCompound);

                        // Drop item if inventory is full.
                        if (playerEntity.getInventory().getEmptySlot() != -1)
                            playerEntity.giveItemStack(spawnEggStack);
                        else
                            playerEntity.dropItem(spawnEggStack, true);

                        // Remove Entity.
                        entity.remove(Entity.RemovalReason.DISCARDED);
                        return ActionResult.SUCCESS;
                    }
                }
            }

            return ActionResult.PASS;
        }));
    }
}