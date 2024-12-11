package com.livinglemming.Events;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.List;

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
                    if (spawnEgg != null) {
                        ItemStack spawnEggStack = new ItemStack(spawnEgg);

                        // Get Entity NBT.
                        NbtCompound nbt = new NbtCompound();
                        entity.writeNbt(nbt);

                        // Add "id" key to identify the entity type.
                        String entityId = EntityType.getId(entity.getType()).toString(); // Get entity's ID (e.g., "minecraft:villager").
                        nbt.putString("id", entityId);

                        // Remove attributes to enable cloning of same NBT entity.
                        nbt.remove("UUID");
                        nbt.remove("Pos");
                        nbt.remove("Motion");
                        nbt.remove("Rotation");
                        nbt.remove("onGround");

                        NbtComponent entityComponent = NbtComponent.of(nbt);

                        // Lore Attribute.
                        List<Text> loreLines = null;
                        if (entity instanceof VillagerEntity villager)
                        {
                            loreLines = List.of(
                                    Text.literal("Profession: [" + villager.getVillagerData().getProfession().toString() + "]")
                                    );
                        }

                        // Create LoreComponent.
                        if (loreLines != null)
                        {
                            LoreComponent loreComponent = new LoreComponent(loreLines);
                            spawnEggStack.set(DataComponentTypes.LORE, loreComponent);
                        }

                        spawnEggStack.set(DataComponentTypes.ENTITY_DATA, entityComponent);

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