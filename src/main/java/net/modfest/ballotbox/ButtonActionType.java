package net.modfest.ballotbox;

import folk.sisby.kaleido.lib.quiltconfig.api.values.ComplexConfigValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ConfigSerializableObject;

import java.util.Locale;

public enum ButtonActionType implements ConfigSerializableObject<String> {
	INSERT_AFTER,
	INSERT_BEFORE,
	REPLACE;

	@Override
	public ConfigSerializableObject<String> convertFrom(String string) {
		return ButtonActionType.valueOf(string.toUpperCase(Locale.ROOT));
	}

	@Override
	public String getRepresentation() {
		return this.name();
	}

	@Override
	public ComplexConfigValue copy() {
		return this;
	}
}
