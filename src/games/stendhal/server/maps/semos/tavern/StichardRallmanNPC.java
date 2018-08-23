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
package games.stendhal.server.maps.semos.tavern;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class StichardRallmanNPC implements ZoneConfigurator {
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

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC stallman = new SpeakerNPC("Stichard Rallman") {

			@Override
			public void say(final String text) {
				setDirection(Direction.DOWN);
				super.say(text, false);
			}

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("欢迎来到 Stendhal! 真正的 #自由 软件!");
				addJob("我是 #自由 软件 (free software) 传道者!");
				addHelp("帮助 #Stendhal 更好玩，花点你的时间，告诉朋友们来玩或是制作地图.");
				addReply("自由",
					"''自由软件'' 不是价格问题，而是权责问题，要理解这个概念，你应该想把 ''free'' 当作 ''自由言论,'' 而不是 ''免费啤酒''.");
				addReply("stendhal",
					"Stendhal 是真正的自由 #free 软件 (客户端，服务端，图片，所有的) 版权都是 #GNU #GPL. 你可以运行，复制，发布，学习，修改并升级此软件.");
				addReply("gnu", "https://www.gnu.org/");
				addReply("gpl", "https://www.gnu.org/licenses/gpl.html");

				addGoodbye();
			}
		};

		stallman.setEntityClass("richardstallmannpc");
		stallman.setDescription("Stichard Rallman 懂得很多有关软件版权的问题.");
		stallman.setPosition(24, 19);
		stallman.setDirection(Direction.DOWN);
		stallman.initHP(100);
		zone.add(stallman);
	}
}
