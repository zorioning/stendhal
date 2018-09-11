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

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

/*
 * Inside 塞门镇 Tavern - Level 1 (upstairs)
 */
public class RareWeaponsSellerNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildMcPegleg(zone);
	}

	private void buildMcPegleg(final StendhalRPZone zone) {
		// Adding a new NPC that buys some of the stuff that Xin doesn't
		final SpeakerNPC mcpegleg = new SpeakerNPC("McPegleg") {

			@Override
			protected void createPath() {
				// McPegleg doesn't move (room too small)
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("哟！朋友! 看起来你需要我的帮忙 #help.");
				addJob("我是一个商人...过来说...收点稀罕货... #rare.");
				addHelp("我也不确定你是否值得相信.... 一个瘸腿 #leg 海盗 #pirate , 总是打量着新来的客人.");
				addQuest("或许你找到一些稀罕的 #rare 盔甲 #armor 或者武器 #weapon ...");
				addGoodbye("不送!");
				add(ConversationStates.ATTENDING, Arrays.asList("weapon", "armor", "rare", "rare armor"),
				        ConversationStates.ATTENDING,
				        "Ssshh! 我偶尔买点稀有武器和盔甲, 你有吗？看看我的出价 #offer", null);
				addOffer("看下墙上的黑板, 上面有我的出价.");
				add(ConversationStates.ATTENDING, Arrays.asList("eye", "leg", "木头", "patch"),
				        ConversationStates.ATTENDING, "不是每天都这么走运 ...", null);
				add(ConversationStates.ATTENDING, "pirate", null, ConversationStates.ATTENDING,
				        "那跟你没关系!", null);
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyrare")), false);
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		// Add some atmosphere
		mcpegleg.setDescription("你遇见一下可疑的家伙, 他有一只假眼和一条假腿.");

		// Add our new NPC to the game world
		mcpegleg.setEntityClass("pirate_sailornpc");
		mcpegleg.setPosition(15, 4);
		mcpegleg.initHP(100);
		zone.add(mcpegleg);
	}
}
