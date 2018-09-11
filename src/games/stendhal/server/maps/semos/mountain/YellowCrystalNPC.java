/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.mountain;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * A crystal NPC
 *
 * @author AntumDeluge
 *
 */
public class YellowCrystalNPC implements ZoneConfigurator {
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

		// Create the NPC
		final SpeakerNPC crystal = new SpeakerNPC("Yellow Crystal") {

			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Hello, 希望你喜欢这里的自然风景.");
				addHelp("这个漂亮的塔建在大山中, 太壮观了!");
				addJob("我是一个水晶. 我还能说什么呢?");
				addGoodbye("再会, 需要我帮忙时随时回来.");

			}
		};

		crystal.setEntityClass("transparentnpc");
		crystal.setAlternativeImage("crystalyellownpc");
		crystal.setPosition(76, 16);
		crystal.initHP(100);
		crystal.setDescription("你看到了一个黄色的水晶 crystal. 多么让人振奋的景象.");
		crystal.setResistance(0);

		zone.add(crystal);
	}

}
