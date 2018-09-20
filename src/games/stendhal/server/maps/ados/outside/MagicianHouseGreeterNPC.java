/***************************************************************************
 *                   (C) Copyright 2011-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.outside;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds a NPC outside Magician house in Ados  (name:温妮尔) who is the pupil of Magician 海震
 *
 * @author geomac
 */
public class MagicianHouseGreeterNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		createMagicianHouseGreeterNPC(zone);
	}

	private void createMagicianHouseGreeterNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("温妮尔") {

			@Override
			protected void createDialog() {
				addGreeting("喂, 我对魔法 #迷阵 很感兴趣!");
				addHelp("如果你觉得累了, 你就会退出然后回去找 #海震.");
				addReply("迷阵", "我想你是迷路了, 不过我有 #卷轴 可帮你指路.");
				addReply("卷轴", "在 #迷阵 里你只有十分钟时间捡起卷轴.");
				addQuest("我正让玩家完成 #迷阵. 海震就会让我成为他的 #助手 .");
				addReply("助手", "某天, 我可以学习如何运用魔法.");
				addReply("海震", "他教我使用魔法.");
				addOffer("我可以给你一些 #建议.");
				addReply("建议", "常看小地图会很有帮助.");
				addJob("我希望可以马上成为海震的 #助手 .");
				addGoodbye("谢谢你, 祝你开心.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("你遇见了 温妮尔. 她想学习魔法.");
		npc.setEntityClass("magicianhousegreeternpc");
		npc.setPosition(70, 52);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}
}