/***************************************************************************
 *                   (C) Copyright 2003-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.nalwor.forest;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayNPCNamesForUnstartedQuestsAction;
import games.stendhal.server.entity.npc.action.SayUnstartedQuestDescriptionFromNPCNameAction;
import games.stendhal.server.entity.npc.condition.TriggerIsNPCNameForUnstartedQuestCondition;
import games.stendhal.server.maps.Region;

/**
 * An oracle who lets players know how they can help others.
 */
public class OracleNPC implements ZoneConfigurator {

	/**
	 * region that this NPC can give information about
	 */
	private final List<String> regions = Arrays.asList(Region.NALWOR_CITY, Region.ORRIL_DUNGEONS, Region.HELL);

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Zinnia") {
			@Override
			public void createDialog() {
				addGreeting("你好, 我们最好小声说话, 不要嗨到精灵们");

				// use a standard action to list the names of NPCs for quests which haven't been started in this region
				addReply(ConversationPhrases.HELP_MESSAGES, null, new SayNPCNamesForUnstartedQuestsAction(regions));

				// if the player says an NPC name, describe the quest (same description as in the travel log)
				add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(regions),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(regions));
				addQuest("在 " + regions + " 附近有几个城镇, 冒险者们可在那里得到帮助 #help ");
				addJob("我只是在这闲逛, 这里能感到些许魔力. ");
				addOffer("就像我的姐妹们 #sisters, 我能 #help 你知道怎么帮助 #help 其他人.");
				addReply("sisters", "我的姐妹们住的很远, 找到她们学会如何帮助 #help 附近的人们. 和我一样, 她们每人都有一个花名 #name .");
				addReply("name", "Zinnia 是一种颜色和我的衣服一样, 像翡翠绿的花, 我觉得也像我喜欢绿色森林一样");

				// just to be nice :)
				addEmotionReply("hugs", "hugs");
				addGoodbye("谢谢你, 在这片魔法森林行走要小心.");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(75, 117));
				nodes.add(new Node(75, 123));
				nodes.add(new Node(82, 123));
				nodes.add(new Node(82, 120));
				nodes.add(new Node(86, 120));
				nodes.add(new Node(86, 123));
				nodes.add(new Node(92, 123));
				nodes.add(new Node(92, 116));
				nodes.add(new Node(90, 116));
				nodes.add(new Node(90, 121));
				nodes.add(new Node(79, 121));
				nodes.add(new Node(79, 118));
				nodes.add(new Node(75, 118));
				setPath(new FixedPath(nodes, true));
			}
		};
		npc.setPosition(75, 117);
		npc.setDescription("你见到了 Zinnia. 她看起来很特别.");
		npc.setEntityClass("oracle3npc");
		zone.add(npc);
	}

}
