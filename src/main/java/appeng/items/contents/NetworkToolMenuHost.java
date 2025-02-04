/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.items.contents;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.upgrades.Upgrades;
import appeng.items.tools.NetworkToolItem;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.SupplierInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

public class NetworkToolMenuHost<T extends NetworkToolItem> extends ItemMenuHost<T> {
    @Nullable
    private final IInWorldGridNodeHost host;

    private final SupplierInternalInventory<InternalInventory> supplierInv;

    public NetworkToolMenuHost(T item, Player player, ItemMenuHostLocator locator,
            @Nullable IInWorldGridNodeHost host) {
        super(item, player, locator);
        this.host = host;
        this.supplierInv = new SupplierInternalInventory<>(
                new StackDependentSupplier<>(this::getItemStack, this::createToolboxInventory));
    }

    private InternalInventory createToolboxInventory(ItemStack stack) {
        var inv = new AppEngInternalInventory(new InternalInventoryHost() {
            @Override
            public void saveChangedInventory(AppEngInternalInventory inv) {
                inv.writeToNBT(stack.getOrCreateTag(), "inv");
            }

            @Override
            public boolean isClientSide() {
                return getPlayer().level().isClientSide();
            }
        }, 9);
        inv.setEnableClientEvents(true); // Also write to NBT on the client to prevent desyncs
        inv.setFilter(new NetworkToolInventoryFilter());
        if (stack.hasTag()) // prevent crash when opening network status screen.
        {
            inv.readFromNBT(stack.getOrCreateTag(), "inv");
        }
        return inv;
    }

    private static class NetworkToolInventoryFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return Upgrades.isUpgradeCardItem(stack.getItem());
        }
    }

    public InternalInventory getInternalInventory() {
        return this.supplierInv;
    }

    @Nullable
    public IInWorldGridNodeHost getGridHost() {
        return this.host;
    }

    public InternalInventory getInventory() {
        return this.supplierInv;
    }
}
