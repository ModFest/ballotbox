package net.modfest.ballotbox.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.layouts.GridLayout$CellInhabitant")
public interface ElementAccessor {
	@Accessor
	int getRow();

	@Mutable
	@Accessor
	void setRow(int row);

	@Accessor
	int getColumn();

	@Mutable
	@Accessor
	void setColumn(int column);
}
