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
package games.stendhal.server.maps.semos.village;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;

public class SheepSellerNPC implements ZoneConfigurator {

	public static final int BUYING_PRICE = 30;

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosVillageArea(zone);
	}

	private void buildSemosVillageArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Nishiya") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(33, 44));
				nodes.add(new Node(33, 43));
				nodes.add(new Node(24, 43));
				nodes.add(new Node(24, 44));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				class SheepSellerBehaviour extends SellerBehaviour {
					SheepSellerBehaviour(final Map<String, Integer> items) {
						super(items);
					}

					@Override
					public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
						if (res.getAmount() > 1) {
							seller.say("Hmm... 我不认为你能立刻放弃照看整个羊群。");
							return false;
						} else if (!player.hasSheep()) {
							if (!player.drop("money", getCharge(res, player))) {
								seller.say("看来你的钱不够.");
								return false;
							}
							seller.say("你带走吧，一只毛绒绒的小羊！要照顾好它......");

							final Sheep sheep = new Sheep(player);
							StendhalRPAction.placeat(seller.getZone(), sheep, seller.getX(), seller.getY() + 1);

							player.notifyWorldAboutChanges();

							return true;
						} else {
							say("好吧，为什么不确信你能看好你之前带走的羊？");
							return false;
						}
					}
				}

				final Map<String, Integer> items = new HashMap<String, Integer>();
				items.put("绵羊", BUYING_PRICE);

				addGreeting();
				addJob("我是牧羊人.");
				addHelp("我销售羊只，要买的话，只要告诉我你想买羊 #buy #sheep. 如果你没做过这行，我能告诉你如何 #放羊, 只要小心 #照看 它，最后可以卖出个好价钱。如果你在野外偶然发现羊，也可以使用支配 #own 把羊牵走.");
				addGoodbye();
				new SellerAdder().addSeller(this, new SheepSellerBehaviour(items));
				addReply("照看",
						"羊儿特别喜爱吃这些长在矮树丛的红树莓，只要站在树莓附近，你的羊经过时会自已去吃它. 你可以随时鼠标右击羊，然后选择查看去了解羊的体重；每吃一颗树莓会增加1点体重。");
				addReply("放羊",
						"你需要把羊保持在离你不太远的地方，当你切换地区时，羊要在附近才行；你可以呼出 #sheep 去喊羊回来。如果你决定放生它，你可以鼠标右键，然后选择离开；但坦白的说，我认为这样的行为很不负责.");
				addReply("sell",
						"一旦你把羊放到100的体重，你可以把它给 Semos 镇上的 Sato；他会收购你的羊.");
				addReply("own",
						"如果你发现一只被放生的羊，可以点击鼠标右键，并选择 own 驯服它，羊需要人的照料!");
			}
		};

		npc.setEntityClass("sellernpc");
		npc.setDescription("Nishiya 在路上巡逻并照料着他的羊，你可以从他那里买一只.");
		npc.setPosition(33, 44);
		npc.initHP(100);
		npc.setSounds(Arrays.asList("cough-11", "cough-2", "cough-3"));
		zone.add(npc);
	}
}
