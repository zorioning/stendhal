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
package games.stendhal.server.maps.kirdneh.city;

import java.util.HashMap;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Builds the flower seller in kirdneh.
 *
 * @author kymara
 */
public class FlowerSellerNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC sellernpc = new SpeakerNPC("弗乐尔") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Hi! Are you here to #trade?");
				addReply(ConversationPhrases.YES_MESSAGES, "Good! I can sell you a beautiful red 玫瑰. Not rhosyd mind you, they're rare. Only 罗斯莉 knows where they grow, and no-one ever knows where 罗斯莉 is!");
				addReply(ConversationPhrases.NO_MESSAGES, "Very well, if I can help you just say.");
				addJob("I sell 玫瑰s in this here market.");
				addHelp("If you need to access your funds, there is a branch of Fado bank right here in Kirdneh. It's the small building north of the museum, on the east of the city.");
				final Map<String, Integer> offerings = new HashMap<String, Integer>();
				offerings.put("玫瑰", 50);
				new SellerAdder().addSeller(this, new SellerBehaviour(offerings));
				addGoodbye("Come back soon!");
			}
		};

		sellernpc.setEntityClass("noimagenpc"); /* woman_000_npc */
		sellernpc.setPosition(64, 82);
		sellernpc.initHP(100);
		sellernpc.setDescription("You see 弗乐尔. Her 玫瑰s are made for young couples.");
		zone.add(sellernpc);
	}
}
