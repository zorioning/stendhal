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
package games.stendhal.server.maps.semos.bakery;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/**
 * The bakery chef. Father of the camping girl.
 * He makes sandwiches for players.
 * He buys cheese.
 *
 * @author daniel
 * @see games.stendhal.server.maps.orril.river.CampingGirlNPC
 * @see games.stendhal.server.maps.quests.PizzaDelivery
 */
public class ChefNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Leander") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// to the well
				nodes.add(new Node(15,3));
				// to a barrel
				nodes.add(new Node(15,8));
				// to the baguette on the table
				nodes.add(new Node(13,8));
				// around the table
				nodes.add(new Node(13,10));
				nodes.add(new Node(10,10));
				// to the sink
				nodes.add(new Node(10,12));
				// to the pizza/cake/whatever
				nodes.add(new Node(7,12));
				nodes.add(new Node(7,10));
				// to the pot
				nodes.add(new Node(3,10));
				// towards the oven
				nodes.add(new Node(3,4));
				nodes.add(new Node(5,4));
				// to the oven
				nodes.add(new Node(5,3));
				// one step back
				nodes.add(new Node(5,4));
				// towards the well
				nodes.add(new Node(15,4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addJob("我是这里的面包师，也做 #pizza 外卖服务。在战争破坏和路被封锁之前，有很多来自 Ados 城的订单: 但也给了我更多时间给重要的客户制作 #make sandwiches 三明治；他们都说很好吃!");
				addHelp("如果你想挣钱，可以帮我送一披撒 #pizza 外卖. 本来以前由我女儿 #Sally 送，但他这会在外渡假去了");
				addReply("bread", "Oh, Erna 管理对外的商务工作。你可以到外面房间跟她说.");
				addReply("cheese",
				"现在 Cheese 干酪相当难找，我们这最近闹鼠灾。我很奇怪这些老鼠把干酪藏哪了？如果你卖 #'sell cheese' ，我很乐意买一些!");
				addReply("ham",
				"很好，你好像一个专业猎人；为什么不去森林猎取一些上等肉呢？不要给我这些小片的肉，虽然。。。我只用高端火腿做三明治sandwiches !");
				addReply("Sally",
				"我女儿 Sally 可能会帮你弄到火腿 ham. 她是一个侦察员。你明白，我想她现在可能在 Or'ril 城堡的南面露营.");
				addReply("pizza", "我需要某个能帮我送pizza外卖的人。你原意接受这个任务吗？ #task");
				addReply(Arrays.asList("sandwich", "sandwiches"),
				"我的 sandwiches 三明治营养且可口，如果你想要，就对我说 #'make 1 sandwich'.");
				addOffer("我的 #pizza 需要干酪 cheese ,我们也不供应，如果你有 cheese 干酪，可以卖给我 #sell.");
				final Map<String, Integer> offers = new TreeMap<String, Integer>();
				offers.put("cheese", 5);
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(offers), false);

				addGoodbye();

				// Leander makes sandwiches if you bring him bread, cheese, and ham.
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("bread", 1);
				requiredResources.put("cheese", 2);
				requiredResources.put("ham", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour(
						"leander_make_sandwiches", "make", "sandwich",
						requiredResources, 3 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				"你好! 很高兴在我制做 #pizza 和 #sandwiches 的烹饪间见到你.");


			}};
			npc.setPosition(15, 3);
			npc.setEntityClass("chefnpc");
			npc.setDescription("你见到了 Leander. 他怕工作是做一些好吃的东西.");
			zone.add(npc);
	}
}


