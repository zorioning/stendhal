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
package games.stendhal.server.maps.ados.abandonedkeep;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Inside Ados Abandoned Keep - level -4 .
 */
public class DwarfWeaponArmorGuyNPC implements ZoneConfigurator {
    private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildgulimo(zone);
	}

	private void buildgulimo(final StendhalRPZone zone) {
		final SpeakerNPC gulimo = new SpeakerNPC("Gulimo") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 24));
				nodes.add(new Node(3, 27));
				nodes.add(new Node(11, 27));
				nodes.add(new Node(11, 24));
				nodes.add(new Node(19, 24));
				nodes.add(new Node(19, 27));
				nodes.add(new Node(11, 27));
				nodes.add(new Node(11, 24));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("欢迎光临本店 #help ");
				addJob("本店销售高质量的盔甲和武器.");
				addHelp("可以看看本店的盔甲和武器. #offer ");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellbetterstuff2")), false);
				addOffer("黑板上可以看看我卖的好东西 #offer.");
				addQuest("谢谢你的好意. 但我很好.");
				addGoodbye();
			}
		};

		gulimo.setEntityClass("greendwarfnpc");
		gulimo.setPosition(3, 24);
		gulimo.initHP(100);
		gulimo.setDescription("你看见 Gulimo. 他销信高质量的盔甲和武器.");
		zone.add(gulimo);
	}
}
