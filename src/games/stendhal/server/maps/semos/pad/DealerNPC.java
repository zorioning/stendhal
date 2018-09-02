/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.pad;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds the NPC who deals in 彩虹豆.
 * Other behaviour defined in maps/quests/RainbowBeans.java
 *
 * @author kymara
 */
public class DealerNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	//
	// IL0_GreeterNPC
	//

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC dealerNPC = new SpeakerNPC("Pdiddi") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 12));
				nodes.add(new Node(4, 10));
				nodes.add(new Node(8, 10));
				nodes.add(new Node(8, 6));
				nodes.add(new Node(2, 6));
				nodes.add(new Node(2, 4));
				nodes.add(new Node(12, 4));
				nodes.add(new Node(12, 6));
				nodes.add(new Node(6, 6));
				nodes.add(new Node(6, 12));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("我想你知道我是什么的.");
				addHelp("说实话，我不能帮你什么，你可以在城镇里生活的更好.");
				addQuest("朋友，没有你要的东西.");
				addOffer("哈! 这个门上的标志是只个封面画！不是酒馆。如果你想喝一杯，最好回到镇上.");
				addGoodbye("Bye.");
			}
		};

		dealerNPC.setEntityClass("drugsdealernpc");
		dealerNPC.setPosition(4, 12);
		dealerNPC.initHP(100);
		dealerNPC.setDescription("你遇见了Pdiddi. 他的精神好像游离于另一个世界...");
		zone.add(dealerNPC);
	}
}
