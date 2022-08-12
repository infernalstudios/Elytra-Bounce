/**
 * Copyright 2022 Infernal Studios
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.infernalstudios.elytrabounce.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

	@Unique
	private int ticksOnGround = 0;

	@Unique
	private boolean wasGoodBefore = false;

	public MixinLivingEntity(EntityType<?> entityType, Level world) {
		super(entityType, world);
	}

	@ModifyArg(method = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setSharedFlag(IZ)V"), index = 1)
	private boolean elytraBounce$travel$flag(boolean in) {
		ItemStack itemstack = this.getItemBySlot(EquipmentSlot.CHEST);
		if (itemstack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(itemstack)) {
			if (ticksOnGround <= 1) {
				return true;
			}
		}

		return in;
	}

	@ModifyArg(method = "Lnet/minecraft/world/entity/LivingEntity;updateFallFlying()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setSharedFlag(IZ)V"), index = 1)
	private boolean elytraBounce$updateFallFlying$flag(boolean in) {
		if (wasGoodBefore && !in && this.isOnGround()) {
			ItemStack itemstack = this.getItemBySlot(EquipmentSlot.CHEST);
			if (itemstack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(itemstack)) {
				wasGoodBefore = in;
				ticksOnGround++;
				return true;
			}
		} else {
			ticksOnGround = 0;
		}

		wasGoodBefore = in;
		return in;
	}

	@Shadow
	public abstract ItemStack getItemBySlot(EquipmentSlot slot);

}
