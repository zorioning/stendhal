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
package games.stendhal.server.maps.semos.caves;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.creature.BabyDragon;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

public class BabyDragonSellerNPC implements ZoneConfigurator {

	private static final String QUEST_SLOT = "hatching_dragon";
	// A baby dragon takes this long to hatch
	private static final long REQUIRED_DAYS = 7L;
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildHouseArea(zone);
	}

	private void buildHouseArea(final StendhalRPZone zone) {

		final SpeakerNPC npc = new SpeakerNPC("Terry") {
			@Override
			protected void createPath() {
			      	final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(66, 8));
				nodes.add(new Node(69, 8));
				nodes.add(new Node(69, 17));
				nodes.add(new Node(74, 17));
				nodes.add(new Node(74, 11));
				nodes.add(new Node(73, 11));
				nodes.add(new Node(73, 10));
				nodes.add(new Node(72, 10));
				nodes.add(new Node(72, 9));
				nodes.add(new Node(66, 9));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					    if (player.hasQuest(QUEST_SLOT)) {
						final long delay = REQUIRED_DAYS * MathHelper.MILLISECONDS_IN_ONE_DAY;
						final long timeRemaining = (Long.parseLong(player.getQuest(QUEST_SLOT))
								      + delay) - System.currentTimeMillis();
						if (timeRemaining > 0L) {
						    raiser.say("蛋还在孵化中, 估计还少还要 "
										+ TimeUtil.timeUntil((int) (timeRemaining / 1000L))
										+ " 才能完成.");
								return;
					        }

    						if (player.hasPet()) {
    						    // we don't want him to give a dragon if player already has a pet
    						    raiser.say("我不能把新孵化的龙交给你, 因为你还带着另一只宠物, 我不认为你有精力照顾两只.");
    						    return;
    						}

							raiser.say("蛋已经孵化成功! 这只尖刺幼龙就交给你了, 别忘了给它 #喂食 ,还要记住要 #保护 它.");
					       	final BabyDragon babydragon = new BabyDragon(player);

					       	babydragon.setPosition(raiser.getX(), raiser.getY() + 1);

					       	player.setPet(babydragon);
    						// clear the quest slot completely when it's not
    						// being used to store egg hatching times
					       	player.removeQuest(QUEST_SLOT);
					       	player.notifyWorldAboutChanges();
					    } else if (player.isEquipped("传说之卵")) {
					    	raiser.say("你在哪儿得到的蛋? ! 没办法,如果你需要 #孵化 可以找我,因为这是我的爱好.");
					    } else {
							raiser.say("Hi. 我不想有太多来访客人, 请出去.");
					    }
					}
				});
		        addReply("孵化", null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					    if (player.hasPet()) {
						// there's actually also a check for this when the egg is hatched,
						// but we might as well warn player here that they wouldn't be allowed two.
							raiser.say("你已经有了一只宠物. 再有一只的话怕是它们要打架...或者更糟...");
					   } else {
						if (player.isEquipped("传说之卵")) {
						    player.drop("传说之卵");
						    raiser.say("Ok, 你的蛋就放在这些收纳畣中孵化, 等待 " + 7 + "天后, 就可以孵化出一只新的幼龙了.");
						    player.setQuest(QUEST_SLOT, Long.toString(System.currentTimeMillis()));
						    player.notifyWorldAboutChanges();
						} else {
						    raiser.say("你身上没有带着龙蛋, 没蛋还怎么孵化.");
						}
					    }
					}
				    });
				addJob("我可以 #孵化 龙蛋, 想要的话带一只龙蛋来.");
				addQuest("如果你找到了龙蛋, 我可以帮你 #孵化 它.");
				addHelp("我饲养幼龙. 如果你有蛋的化, 我不但可以 #孵化 它. 还可以告诉你如何带宠物 #旅行 ,还如如何 #照料 它. 如果你偶尔找到野生的幼龙, 也可以使用 #支配 它.");
				addGoodbye("出门小心路上的巨人!");
				addReply("喂食", "幼龙吃 肉 和 ham. 但你最好弄点 pizza 喂它, 幼龙最喜欢吃这个.");
				addReply("照料",
						"幼龙吃 肉 和 ham 还有 pizza. 把食物扔到地上, 幼龙就会跑去吃. 你可以右键点击幼龙,然后选择 'Look' 查看龙的成长情况. 每吃一片食物就能成长一点.");
				addReply("旅行",
						"当你走到另一地区时, 要保证幼龙跟在你身边; 如果它跑去玩, 你也可以喴 #pet 叫它回到你身边. 如果你决定丢弃它, 右点自已选择 'Leave Pet'.");
				addReply("保护",
					 "野外的生物能闻到幼龙的气味, 并攻击它. 幼龙虽然也会反击, 但它也需要帮助, 要不然可能会被杀死.");
				addReply("支配",
						"像羊或其它宠物, 如果你找到野生或被丢弃的幼龙, 可以右点它点击 'Own' 获得它, 然后它就跟你走了, 我建议你最好马上给它 #喂食 .");
			}
		};

		npc.setEntityClass("man_005_npc");
		npc.setDescription("你遇见了 Terry. 他一有空就和幼龙们戏耍.");
		npc.setPosition(66, 8);
		npc.initHP(100);
		zone.add(npc);

		// Also put a dragon in the caves (people can't Own it as it is behind rocks)
		final BabyDragon drag = new BabyDragon();
                drag.setPosition(62, 8);
                zone.add(drag);
	}
}
