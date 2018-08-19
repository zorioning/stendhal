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
package games.stendhal.server.maps.semos.gnomevillage;

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
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

/**
 * Inside Gnome Village.
 */
public class GarbiddleNPC implements ZoneConfigurator {
    private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildgarbiddle(zone);
	}

	private void buildgarbiddle(final StendhalRPZone zone) {
		final SpeakerNPC garbiddle = new SpeakerNPC("Garbiddle") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(37, 112));
				nodes.add(new Node(41, 112));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("欢迎来到我们的精彩农庄.");
				addJob("我在雨季时收购些物资.");
				addHelp("我收购几种商品，请在标识牌中看看我的需求.");
				addOffer("读这个标识，能看到我们的需求.");
				addQuest("谢谢关心，但我很好.");
				addGoodbye("再见，很高兴你停下拜访我们。");
 				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buy4gnomes")), false);
			}
		};

		garbiddle.setEntityClass("gnomenpc");
		garbiddle.setPosition(37, 112);
		garbiddle.initHP(100);
		garbiddle.setDescription("你看见 Garbiddle, 长的很精致的侏儒女士. 她等着顾客上门");
		zone.add(garbiddle);
	}
}
