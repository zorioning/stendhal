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
package games.stendhal.server.maps.semos.plains;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * A man hoeing the farm ground
 *
 */
public class HoeingManNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Jingo Radish") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(48, 62));
				nodes.add(new Node(43, 76));
				nodes.add(new Node(43, 62));

				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting("你好，旅行者!");
				addJob("看看? 我用锄头 #hoe 把长着杂草的硬士翻开，但这些杂草每次还能长出来...");
				addHelp("可以花点时间了解下周围的地点...稍北边一点是个磨坊，东边是个不错的农场...很好很富饶的乡村，你也可以在这里打猎!");
				addReply("hoe",
                    "Oh 好吧,我的锄头没什么特殊的。。。如果你需要像镰刀这类的农具，你到 Semos 镇上的铁匠商店看看可能有帮助!");
				addGoodbye("再见，你的脚印印在草上了!");
			}

		};
		npc.setEntityClass("hoeingmannpc");
		npc.setDescription("你遇见了一个手拿锄头的农夫，他正忙着翻开板结的泥土.");
		npc.setPosition(48,62);
		npc.initHP(100);
		zone.add(npc);
	}

}
