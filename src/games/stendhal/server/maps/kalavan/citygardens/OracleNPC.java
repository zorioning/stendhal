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
package games.stendhal.server.maps.kalavan.citygardens;

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
	private final List<String> regions = Arrays.asList(Region.KALAVAN, Region.KIRDNEH, Region.FADO_CITY, Region.FADO_CAVES);

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Lobelia") {
			@Override
			public void createDialog() {
				addGreeting("你好，刚好我在这里赏花.");

				// use a standard action to list the names of NPCs for quests which haven't been started in this region
				addReply(ConversationPhrases.HELP_MESSAGES, null, new SayNPCNamesForUnstartedQuestsAction(regions));

				// if the player says an NPC name, describe the quest (same description as in the travel log)
				add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(regions),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(regions));
				addQuest("在 " + regions + " 有个需要你帮助 #help 的朋友 .");
				addJob("我有时在这照料这些漂亮的花，但这其实是可爱的园丁 Sue 的工作..");
				addOffer("和在其他地方的我的姐妹们 #sisters 一样, 我在这指引你如何帮助 #help 他人.");
				addReply("sisters", "我和我的姐妹们都有一个花名 #name . 找出他们，党会如何帮助 #help 附近的人们.");
				addReply("name", "Lobelia 是小小的紫色花。可能你在花坛中看到了，在这我只爱它，Sue 很聪明.");

				// just to be nice :)
				addEmotionReply("hugs", "hugs");
				addGoodbye("谢谢你，再见.");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(22, 111));
				nodes.add(new Node(58, 111));
				nodes.add(new Node(58, 109));
				nodes.add(new Node(61, 109));
				nodes.add(new Node(61, 100));
				nodes.add(new Node(56, 100));
				nodes.add(new Node(56, 101));
				nodes.add(new Node(54, 101));
				nodes.add(new Node(54, 105));
				nodes.add(new Node(27, 105));
				nodes.add(new Node(27, 107));
				nodes.add(new Node(22, 111));
				setPath(new FixedPath(nodes, true));
			}
		};
		npc.setPosition(22,111);
		npc.setDescription("你见到了 Lobelia. 她正注视着花坛中的花朵.");
		npc.setEntityClass("oracle4npc");
		zone.add(npc);
	}

}
