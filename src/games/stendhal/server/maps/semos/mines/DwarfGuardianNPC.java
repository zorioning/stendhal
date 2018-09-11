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
package games.stendhal.server.maps.semos.mines;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

public class DwarfGuardianNPC implements ZoneConfigurator {
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
		buildMineArea(zone);
	}

	private void buildMineArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Phalk") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						String reply = "有个大家伙在那！很个人都害怕. ";
						if (player.getLevel() < 60) {
							reply += "要进入里面但你还太弱了. 当你再强壮一些的时候, 把那个石头 #stones 推到边上, 就能看到进入矿山的黑暗通道...";
						} else {
							reply += "小心! 要进入矿山的黑暗通道, 需把设置在矿山入口的石头 #stones 推开...";
						}
						raiser.say(reply);
					}
				});
				addReply(Arrays.asList("stone", "stones"), "在 Faiumoni 周围, 你可以找到成吨的石头, 一些石头不算大, 依你的力量应该能推开它...我觉得像你这样的大英雄, 推开它们都不是问题.");
				addJob("我是一个矮人守卫, 尽力让冒险者们放弃他们的使命.");
				addHelp("当你在 Semos 矿山时要格外小心, 那里有凶猛的野兽！如果你需要好一些的装备, 可以向 Semos 酒馆的 Harold 买一些, 也许他能帮到你...");
				addGoodbye();
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.LEFT);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("PhalkFirstChat")) {
					player.setQuest("PhalkFirstChat", "done");
					player.addXP(500);
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setEntityClass("dwarf_guardiannpc");
		npc.setDescription("你遇见了 Phalk. 他不想放任冒险者完成他们的使命.");
		npc.setPosition(118, 26);
		npc.setDirection(Direction.LEFT);
		npc.initHP(25);
		zone.add(npc);
	}
}
