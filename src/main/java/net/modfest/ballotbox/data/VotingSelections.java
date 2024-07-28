package net.modfest.ballotbox.data;

import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import net.modfest.ballotbox.util.MapUtil;

public record VotingSelections(Multimap<String, String> votes) {
	public static final Codec<VotingSelections> CODEC = Codec.unboundedMap(Codec.STRING, Codec.list(Codec.STRING))
		.xmap(MapUtil::asMultiMap, MapUtil::asListMap)
		.xmap(VotingSelections::new, VotingSelections::votes);
}
