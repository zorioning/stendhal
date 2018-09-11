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
package games.stendhal.server.maps.semos.plains;

import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SeedSellerBehaviour;

/**
 * The miller (original name: 詹妮). She mills flour for players who bring
 * grain.
 */
public class MillerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("詹妮") {
			@Override
			public void createDialog() {
				addJob("我管理着这个风车, 用它把农民生产的谷物 #grain 磨 #mill 成面粉, 我也为 塞门镇 的面包房供货.");
				addReply("grain",
				        "附近是个农场；经常有一些人在那里收割农作物. 要收获农作物, 当然需要一把镰刀.");
				addHelp("你知道 塞门镇 的面包房？自豪的说他们用的是我的面粉, 但最近狼群又咬了我的运货员... 他们可能被吓跑了.");
				addGoodbye();
				addOffer("你也能买点种子种植 #plant, 然后开始漂亮的花.");
				//addReply("plant","Your seeds should be planted on fertile ground. Look for the brown ground just over the path from the arandula patch in semos plains over yonder. Seeds will thrive there, you can visit each day to see if your flower has grown. When it is ready, it can be picked. The area is open to everyone so there's a chance someone else will pick your flower, but luckily seeds are cheap!");
				addReply("plant","种子应该种在肥沃的土地上. 只要找找 塞门镇 平原 到 arandula 的道路经过的地方, 种子都可以在那里长的很好, 你可以每天看看花儿生长的情况. 当花儿长成时, 就能被采摘了. 这些地区对每个人开放, 所以每个人都有机会得到你种的花, 不好好在这些种子很便宜!");
			}

			/*
			 * (non-Javadoc)
			 * @see games.stendhal.server.entity.npc.SpeakerNPC#onGoodbye(games.stendhal.server.entity.RPEntity)
			 */
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}

		};
		// Jenny mills flour if you bring her grain.
		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("grain", 5);

		final ProducerBehaviour behaviour = new ProducerBehaviour("jenny_mill_flour",
				"mill", "flour", requiredResources, 2 * 60);
		new SellerAdder().addSeller(npc, new SeedSellerBehaviour());
		new ProducerAdder().addProducer(npc, behaviour,"你好! 我叫 詹妮, 当地的磨坊厂长. 如果你给我带来一些谷物 #grain, 我能把它们磨 #mill 成面粉flour.");
		npc.setPosition(19, 39);
		npc.setDescription("你看到了 詹妮. 她是当地的磨坊厂长.");
		npc.setDirection(Direction.DOWN);
		npc.setEntityClass("woman_003_npc");
		zone.add(npc);
	}

}
