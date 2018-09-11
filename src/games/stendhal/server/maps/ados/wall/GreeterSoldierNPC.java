/***************************************************************************
 *                   (C) Copyright 2003-2017 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.wall;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ExamineChatAction;

/**
 * Ados Wall North population.
 *
 * @author hendrik
 * @author kymara
 */
public class GreeterSoldierNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildAdosGreetingSoldier(zone);
	}

	/**
	 * Creatures a soldier telling people a story, why Ados is so empty.
	 *
	 * @param zone StendhalRPZone
	 */
	private void buildAdosGreetingSoldier(final StendhalRPZone zone) {

		final SpeakerNPC npc = new SpeakerNPC("Julius") {

			@Override
			protected void createPath() {
				final List<Node> path = new LinkedList<Node>();
				path.add(new Node(84, 109));
				path.add(new Node(84, 116));
				setPath(new FixedPath(path, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Hi, 欢迎来到 阿多斯城!");
				addReply("地图", "说明\n"
					+ "1 银库,   2 金匠,   3 面包店,   4 闹鬼小屋,\n"
					+ "5 城堡,   6 菲琳娜的家,   7 军营 \n"
					+ "8 酒吧,   9 缝纫店, ida \n"
					+ "10 鱼肉小屋,   11 市政厅,   12 图书馆",
					new ExamineChatAction("map-ados-city.png", "阿多斯城", "阿多斯城地图"));
				addJob("我在 阿多斯 城防御外部进攻, 顺便 #帮助 来访者.");
				addHelp("如果你需要阿多斯城的 #地图 做指导, 只管问我.");
				addGoodbye("希望你在阿多斯城玩的开心.");
			}
		};

		npc.setEntityClass("youngsoldiernpc");
		npc.setPosition(84, 109);
		npc.initHP(100);
		npc.setDescription("你遇见了 Julius, 他是一位阿多斯城的入口驻守的卫兵.");
		zone.add(npc);
	}
}
