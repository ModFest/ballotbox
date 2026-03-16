package net.modfest.ballotbox.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;

@Mixin(GridLayout.class)
public interface GridWidgetAccessor {
	@Accessor
	List<LayoutElement> getChildren();

	@Accessor
	List<Object> getCellInhabitants();
}
