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
package games.stendhal.server.maps.semos.yeticave;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class MrYetiNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildYeti(zone);
	}

	private void buildYeti(final StendhalRPZone zone) {
		final SpeakerNPC yetimale = new SpeakerNPC("耶提先生") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(29, 29));
				nodes.add(new Node(17, 29));
				nodes.add(new Node(17, 32));
				nodes.add(new Node(14, 32));
				nodes.add(new Node(14, 38));
				nodes.add(new Node(13, 38));
				nodes.add(new Node(13, 46));
				nodes.add(new Node(19, 46));
				nodes.add(new Node(19, 54));
				nodes.add(new Node(23, 54));
				nodes.add(new Node(21, 54));
				nodes.add(new Node(21, 45));
				nodes.add(new Node(26, 45));
				nodes.add(new Node(26, 37));
				nodes.add(new Node(29, 37));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
//				addGreeting("Greetings, strange foreigner!");
//				addJob("My job is to clean up all this around you!");
//				addHelp("I am not able to help you!");
				addGreeting("欢迎你, 奇怪的外乡人！");
				addJob("我的工作是清理你四周的这些地方. ");
				addHelp("我帮不了你！");
				addGoodbye();
			}
		};

		yetimale.setEntityClass("yetimalenpc");
		yetimale.setDescription("你遇见了 耶提先生, 有着一双大脚的他身上长满白色毛发！");
		yetimale.setPosition(29, 29);
		yetimale.setCollisionAction(CollisionAction.STOP);
		yetimale.initHP(100);
		zone.add(yetimale);
	}
}
