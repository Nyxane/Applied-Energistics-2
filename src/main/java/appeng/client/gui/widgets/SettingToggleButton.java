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

package appeng.client.gui.widgets;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import appeng.api.config.AccessRestriction;
import appeng.api.config.CondenserOutput;
import appeng.api.config.FullnessMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelType;
import appeng.api.config.OperationMode;
import appeng.api.config.PowerUnits;
import appeng.api.config.RedstoneMode;
import appeng.api.config.RelativeDirection;
import appeng.api.config.SchedulingMode;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.StorageFilter;
import appeng.api.config.TerminalStyle;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.core.localization.ButtonToolTips;
import appeng.util.EnumCycler;

public class SettingToggleButton<T extends Enum<T>> extends IconButton {
    private static Map<EnumPair, ButtonAppearance> appearances;
    private final Settings buttonSetting;
    private final IHandler<SettingToggleButton<T>> onPress;
    private final EnumSet<T> validValues;
    private T currentValue;

    @FunctionalInterface
    public interface IHandler<T extends SettingToggleButton<?>> {
        void handle(T button, boolean backwards);
    }

    public SettingToggleButton(final Settings setting, final T val,
            IHandler<SettingToggleButton<T>> onPress) {
        this(setting, val, t -> true, onPress);
    }

    public SettingToggleButton(final Settings setting, final T val, Predicate<T> isValidValue,
            IHandler<SettingToggleButton<T>> onPress) {
        super(SettingToggleButton::onPress);
        this.onPress = onPress;

        // Build a list of values (in order) that are valid w.r.t. the given predicate
        EnumSet<T> validValues = EnumSet.allOf(val.getDeclaringClass());
        validValues.removeIf(isValidValue.negate());
        validValues.removeIf(s -> !setting.getPossibleValues().contains(s));
        this.validValues = validValues;

        this.buttonSetting = setting;
        this.currentValue = val;

        if (appearances == null) {
            appearances = new HashMap<>();
            registerApp(Icon.CONDENSER_OUTPUT_TRASH, Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH,
                    ButtonToolTips.CondenserOutput,
                    ButtonToolTips.Trash);
            registerApp(Icon.CONDENSER_OUTPUT_MATTER_BALL, Settings.CONDENSER_OUTPUT, CondenserOutput.MATTER_BALLS,
                    ButtonToolTips.CondenserOutput,
                    ButtonToolTips.MatterBalls.text(CondenserOutput.MATTER_BALLS.requiredPower));
            registerApp(Icon.CONDENSER_OUTPUT_SINGULARITY, Settings.CONDENSER_OUTPUT, CondenserOutput.SINGULARITY,
                    ButtonToolTips.CondenserOutput,
                    ButtonToolTips.Singularity.text(CondenserOutput.SINGULARITY.requiredPower));

            registerApp(Icon.ACCESS_READ, Settings.ACCESS, AccessRestriction.READ, ButtonToolTips.IOMode,
                    ButtonToolTips.Read);
            registerApp(Icon.ACCESS_WRITE, Settings.ACCESS, AccessRestriction.WRITE, ButtonToolTips.IOMode,
                    ButtonToolTips.Write);
            registerApp(Icon.ACCESS_READ_WRITE, Settings.ACCESS, AccessRestriction.READ_WRITE, ButtonToolTips.IOMode,
                    ButtonToolTips.ReadWrite);

            registerApp(Icon.POWER_UNIT_AE, Settings.POWER_UNITS, PowerUnits.AE, ButtonToolTips.PowerUnits,
                    PowerUnits.AE.textComponent());
            // registerApp(Icon.POWER_UNIT_EU, Settings.POWER_UNITS, PowerUnits.EU, ButtonToolTips.PowerUnits,
            // PowerUnits.EU.textComponent());
            registerApp(Icon.POWER_UNIT_RF, Settings.POWER_UNITS, PowerUnits.RF, ButtonToolTips.PowerUnits,
                    PowerUnits.RF.textComponent());

            registerApp(Icon.REDSTONE_IGNORE, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE,
                    ButtonToolTips.RedstoneMode,
                    ButtonToolTips.AlwaysActive);
            registerApp(Icon.REDSTONE_LOW, Settings.REDSTONE_CONTROLLED, RedstoneMode.LOW_SIGNAL,
                    ButtonToolTips.RedstoneMode,
                    ButtonToolTips.ActiveWithoutSignal);
            registerApp(Icon.REDSTONE_HIGH, Settings.REDSTONE_CONTROLLED, RedstoneMode.HIGH_SIGNAL,
                    ButtonToolTips.RedstoneMode,
                    ButtonToolTips.ActiveWithSignal);
            registerApp(Icon.REDSTONE_PULSE, Settings.REDSTONE_CONTROLLED, RedstoneMode.SIGNAL_PULSE,
                    ButtonToolTips.RedstoneMode,
                    ButtonToolTips.ActiveOnPulse);

            registerApp(Icon.REDSTONE_LOW, Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL,
                    ButtonToolTips.RedstoneMode,
                    ButtonToolTips.EmitLevelsBelow);
            registerApp(Icon.REDSTONE_HIGH, Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL,
                    ButtonToolTips.RedstoneMode,
                    ButtonToolTips.EmitLevelAbove);

            registerApp(Icon.ARROW_LEFT, Settings.OPERATION_MODE, OperationMode.FILL,
                    ButtonToolTips.TransferDirection,
                    ButtonToolTips.TransferToStorageCell);
            registerApp(Icon.ARROW_RIGHT, Settings.OPERATION_MODE, OperationMode.EMPTY,
                    ButtonToolTips.TransferDirection,
                    ButtonToolTips.TransferToNetwork);

            registerApp(Icon.ARROW_LEFT, Settings.IO_DIRECTION, RelativeDirection.LEFT,
                    ButtonToolTips.TransferDirection,
                    ButtonToolTips.TransferToStorageCell);
            registerApp(Icon.ARROW_RIGHT, Settings.IO_DIRECTION, RelativeDirection.RIGHT,
                    ButtonToolTips.TransferDirection,
                    ButtonToolTips.TransferToNetwork);

            registerApp(Icon.ARROW_UP, Settings.SORT_DIRECTION, SortDir.ASCENDING, ButtonToolTips.SortOrder,
                    ButtonToolTips.ToggleSortDirection);
            registerApp(Icon.ARROW_DOWN, Settings.SORT_DIRECTION, SortDir.DESCENDING, ButtonToolTips.SortOrder,
                    ButtonToolTips.ToggleSortDirection);

            registerApp(Icon.SEARCH_AUTO, Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH, ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_Auto);
            registerApp(Icon.SEARCH_MANUAL, Settings.SEARCH_MODE, SearchBoxMode.MANUAL_SEARCH,
                    ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_Standard);
            registerApp(Icon.SEARCH_AUTO_JEI, Settings.SEARCH_MODE, SearchBoxMode.JEI_AUTOSEARCH,
                    ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_JEIAuto);
            registerApp(Icon.SEARCH_MANUAL_JEI, Settings.SEARCH_MODE, SearchBoxMode.JEI_MANUAL_SEARCH,
                    ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_JEIStandard);
            registerApp(Icon.SEARCH_AUTO_KEEP, Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH_KEEP,
                    ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_AutoKeep);
            registerApp(Icon.SEARCH_MANUAL_KEEP, Settings.SEARCH_MODE, SearchBoxMode.MANUAL_SEARCH_KEEP,
                    ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_StandardKeep);
            registerApp(Icon.SEARCH_AUTO_JEI_KEEP, Settings.SEARCH_MODE, SearchBoxMode.JEI_AUTOSEARCH_KEEP,
                    ButtonToolTips.SearchMode,
                    ButtonToolTips.SearchMode_JEIAutoKeep);
            registerApp(Icon.SEARCH_MANUAL_JEI_KEEP, Settings.SEARCH_MODE, SearchBoxMode.JEI_MANUAL_SEARCH_KEEP,
                    ButtonToolTips.SearchMode, ButtonToolTips.SearchMode_JEIStandardKeep);

            registerApp(Icon.LEVEL_ENERGY, Settings.LEVEL_TYPE, LevelType.ENERGY_LEVEL, ButtonToolTips.LevelType,
                    ButtonToolTips.LevelType_Energy);
            registerApp(Icon.LEVEL_ITEM, Settings.LEVEL_TYPE, LevelType.ITEM_LEVEL, ButtonToolTips.LevelType,
                    ButtonToolTips.LevelType_Item);

            registerApp(Icon.TERMINAL_STYLE_TALL, Settings.TERMINAL_STYLE, TerminalStyle.TALL,
                    ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Tall);
            registerApp(Icon.TERMINAL_STYLE_SMALL, Settings.TERMINAL_STYLE, TerminalStyle.SMALL,
                    ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Small);
            registerApp(Icon.TERMINAL_STYLE_FULL, Settings.TERMINAL_STYLE, TerminalStyle.FULL,
                    ButtonToolTips.TerminalStyle,
                    ButtonToolTips.TerminalStyle_Full);

            registerApp(Icon.SORT_BY_NAME, Settings.SORT_BY, SortOrder.NAME, ButtonToolTips.SortBy,
                    ButtonToolTips.ItemName);
            registerApp(Icon.SORT_BY_AMOUNT, Settings.SORT_BY, SortOrder.AMOUNT, ButtonToolTips.SortBy,
                    ButtonToolTips.NumberOfItems);
            registerApp(Icon.SORT_BY_MOD, Settings.SORT_BY, SortOrder.MOD, ButtonToolTips.SortBy, ButtonToolTips.Mod);

            registerApp(Icon.VIEW_MODE_STORED, Settings.VIEW_MODE, ViewItems.STORED, ButtonToolTips.View,
                    ButtonToolTips.StoredItems);
            registerApp(Icon.VIEW_MODE_ALL, Settings.VIEW_MODE, ViewItems.ALL, ButtonToolTips.View,
                    ButtonToolTips.StoredCraftable);
            registerApp(Icon.VIEW_MODE_CRAFTING, Settings.VIEW_MODE, ViewItems.CRAFTABLE, ButtonToolTips.View,
                    ButtonToolTips.Craftable);

            registerApp(Icon.FUZZY_PERCENT_25, Settings.FUZZY_MODE, FuzzyMode.PERCENT_25, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_25);
            registerApp(Icon.FUZZY_PERCENT_50, Settings.FUZZY_MODE, FuzzyMode.PERCENT_50, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_50);
            registerApp(Icon.FUZZY_PERCENT_75, Settings.FUZZY_MODE, FuzzyMode.PERCENT_75, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_75);
            registerApp(Icon.FUZZY_PERCENT_99, Settings.FUZZY_MODE, FuzzyMode.PERCENT_99, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZPercent_99);
            registerApp(Icon.FUZZY_IGNORE, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL, ButtonToolTips.FuzzyMode,
                    ButtonToolTips.FZIgnoreAll);

            registerApp(Icon.FULLNESS_EMPTY, Settings.FULLNESS_MODE, FullnessMode.EMPTY, ButtonToolTips.OperationMode,
                    ButtonToolTips.MoveWhenEmpty);
            registerApp(Icon.FULLNESS_HALF, Settings.FULLNESS_MODE, FullnessMode.HALF, ButtonToolTips.OperationMode,
                    ButtonToolTips.MoveWhenWorkIsDone);
            registerApp(Icon.FULLNESS_FULL, Settings.FULLNESS_MODE, FullnessMode.FULL, ButtonToolTips.OperationMode,
                    ButtonToolTips.MoveWhenFull);

            registerApp(Icon.BLOCKING_MODE_YES, Settings.BLOCK, YesNo.YES, ButtonToolTips.InterfaceBlockingMode,
                    ButtonToolTips.Blocking);
            registerApp(Icon.BLOCKING_MODE_NO, Settings.BLOCK, YesNo.NO, ButtonToolTips.InterfaceBlockingMode,
                    ButtonToolTips.NonBlocking);

            registerApp(Icon.VIEW_MODE_CRAFTING, Settings.CRAFT_ONLY, YesNo.YES, ButtonToolTips.Craft,
                    ButtonToolTips.CraftOnly);
            registerApp(Icon.VIEW_MODE_ALL, Settings.CRAFT_ONLY, YesNo.NO, ButtonToolTips.Craft,
                    ButtonToolTips.CraftEither);

            registerApp(Icon.PERMISSION_CRAFT, Settings.CRAFT_VIA_REDSTONE, YesNo.YES, ButtonToolTips.EmitterMode,
                    ButtonToolTips.CraftViaRedstone);
            registerApp(Icon.PERMISSION_EXTRACT, Settings.CRAFT_VIA_REDSTONE, YesNo.NO, ButtonToolTips.EmitterMode,
                    ButtonToolTips.EmitWhenCrafting);

            registerApp(Icon.STORAGE_FILTER_EXTRACTABLE_ONLY, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY,
                    ButtonToolTips.ReportInaccessibleItems, ButtonToolTips.ReportInaccessibleItemsNo);
            registerApp(Icon.STORAGE_FILTER_EXTRACTABLE_NONE, Settings.STORAGE_FILTER, StorageFilter.NONE,
                    ButtonToolTips.ReportInaccessibleItems,
                    ButtonToolTips.ReportInaccessibleItemsYes);

            registerApp(Icon.PLACEMENT_BLOCK, Settings.PLACE_BLOCK, YesNo.YES, ButtonToolTips.BlockPlacement,
                    ButtonToolTips.BlockPlacementYes);
            registerApp(Icon.PLACEMENT_ITEM, Settings.PLACE_BLOCK, YesNo.NO, ButtonToolTips.BlockPlacement,
                    ButtonToolTips.BlockPlacementNo);

            registerApp(Icon.SCHEDULING_DEFAULT, Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT,
                    ButtonToolTips.SchedulingMode,
                    ButtonToolTips.SchedulingModeDefault);
            registerApp(Icon.SCHEDULING_ROUND_ROBIN, Settings.SCHEDULING_MODE, SchedulingMode.ROUNDROBIN,
                    ButtonToolTips.SchedulingMode,
                    ButtonToolTips.SchedulingModeRoundRobin);
            registerApp(Icon.SCHEDULING_RANDOM, Settings.SCHEDULING_MODE, SchedulingMode.RANDOM,
                    ButtonToolTips.SchedulingMode,
                    ButtonToolTips.SchedulingModeRandom);

            registerApp(Icon.OVERLAY_OFF, Settings.OVERLAY_MODE, YesNo.NO, ButtonToolTips.OverlayMode,
                    ButtonToolTips.OverlayModeNo);
            registerApp(Icon.OVERLAY_ON, Settings.OVERLAY_MODE, YesNo.YES, ButtonToolTips.OverlayMode,
                    ButtonToolTips.OverlayModeYes);
        }
    }

    private static void onPress(Button btn) {
        if (btn instanceof SettingToggleButton) {
            ((SettingToggleButton<?>) btn).triggerPress();
        }
    }

    private void triggerPress() {
        boolean backwards = false;
        // This isn't great, but we don't get any information about right-clicks
        // otherwise
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof AEBaseScreen) {
            backwards = ((AEBaseScreen<?>) currentScreen).isHandlingRightClick();
        }
        onPress.handle(this, backwards);
    }

    private static void registerApp(final Icon icon, final Settings setting, final Enum<?> val,
            final ButtonToolTips title, final Component hint) {
        appearances.put(
                new EnumPair(setting, val),
                new ButtonAppearance(
                        icon,
                        title.text(),
                        hint));
    }

    private static void registerApp(final Icon icon, final Settings setting, final Enum<?> val,
            final ButtonToolTips title, final ButtonToolTips hint) {
        registerApp(icon, setting, val, title, hint.text());
    }

    @Override
    protected Icon getIcon() {
        if (this.buttonSetting != null && this.currentValue != null) {
            final ButtonAppearance app = appearances.get(new EnumPair(this.buttonSetting, this.currentValue));
            if (app != null) {
                return app.icon;
            }
        }
        return Icon.TOOLBAR_BUTTON_BACKGROUND;
    }

    public Settings getSetting() {
        return this.buttonSetting;
    }

    public T getCurrentValue() {
        return this.currentValue;
    }

    public void set(final T e) {
        if (this.currentValue != e) {
            this.currentValue = e;
        }
    }

    public T getNextValue(boolean backwards) {
        return EnumCycler.rotateEnum(currentValue, backwards, validValues);
    }

    @Override
    public List<Component> getTooltipMessage() {

        if (this.buttonSetting == null || this.currentValue == null) {
            return Collections.emptyList();
        }

        final ButtonAppearance buttonAppearance = appearances
                .get(new EnumPair(this.buttonSetting, this.currentValue));
        if (buttonAppearance == null) {
            return Collections.singletonList(new TextComponent("No Such Message"));
        }

        return Arrays.asList(buttonAppearance.displayName, buttonAppearance.hint);
    }

    private static final class EnumPair {

        final Settings setting;
        final Enum<?> value;

        EnumPair(final Settings a, final Enum<?> b) {
            this.setting = a;
            this.value = b;
        }

        @Override
        public int hashCode() {
            return this.setting.hashCode() ^ this.value.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final EnumPair other = (EnumPair) obj;
            return other.setting == this.setting && other.value == this.value;
        }
    }

    private static class ButtonAppearance {
        @Nonnull
        public final Icon icon;
        @Nonnull
        public final Component displayName;
        @Nonnull
        public final Component hint;

        public ButtonAppearance(Icon icon, Component displayName, Component hint) {
            this.icon = Objects.requireNonNull(icon);
            this.displayName = Objects.requireNonNull(displayName);
            this.hint = Objects.requireNonNull(hint);
        }
    }
}
