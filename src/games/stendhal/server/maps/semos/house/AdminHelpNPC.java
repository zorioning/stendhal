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
package games.stendhal.server.maps.semos.house;


import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayNPCNamesForUnstartedQuestsAction;
import games.stendhal.server.entity.npc.action.SayUnstartedQuestDescriptionFromNPCNameAction;
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.condition.AdminCondition;
import games.stendhal.server.entity.npc.condition.TriggerIsNPCNameForUnstartedQuestCondition;
import games.stendhal.server.maps.Region;

/**
 * A young lady (original name: Skye) who is lovely to admins.
 */
public class AdminHelpNPC implements ZoneConfigurator {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		SpeakerNPC npc = new SpeakerNPC("Skye") {

			@Override
			public void createDialog() {
				addGreeting("Hello! 今天你看起来特别精神，其实，你每天状态都不错!");
				addJob("我在这你会很愉快. 如果你使用 #/teleportto me, 就会传送到这里. 并且，我在这给你解释 #portals 传送的用法.");
				addHelp("如果你需要，我可以为你治疗 #heal . 或者我只用对我说 #nice #things. 如果你需要知道 #portals 有关的事情，问就好了.");
				addOffer("我能把你送到 #playground 和玩家一起玩!");
				addReply("nice", "想知道多少玩家认为你的帮助很到位？好吧，我告诉你 loads of them do.");
				addReply("things", "因此，你是一个可以测试全部 #blue #words 的人, 对吧? 现在你对你的职责感到惊奇吧！");
				addReply("blue", "Aw, 不要发愁 :( 听个好音乐，可能会好... ");
				addReply("words", "Roses are red, violets are blue, Stendhal is great, and so are you!");
			//	addReply("portals", "The one with the Sun goes to semos city. It shows you where this house really is. The rest are clear, I hope. There is a door to the bank, the jail, and the Death Match in Ados. Of course they are all one way portals so you will not be disturbed by unexpected visitors.");
				addReply("portals", "顶着太阳云了semos镇，它说明了这个房子真实的位置。我希望安静且清楚.这里是通向Ados城的银行、监狱、死亡赛场的入口，当然这些是一种传送方法。所以你也不必奇怪遇到意料之外的玩家");
				addQuest("现在你正测试多少种想法w you're really testing how much thought went into making me!");
				add(ConversationStates.ATTENDING,
						"playground",
						new AdminCondition(500),
						ConversationStates.IDLE,
						"Have fun!",
						new TeleportAction("int_admin_playground", 20, 20, Direction.DOWN));
			    add(ConversationStates.ATTENDING,
						"semos",
						null,
						ConversationStates.ATTENDING,
						null,
						new SayNPCNamesForUnstartedQuestsAction(Region.SEMOS_CITY));
			    add(ConversationStates.ATTENDING,
						"nalwor",
						null,
						ConversationStates.ATTENDING,
						null,
						new SayNPCNamesForUnstartedQuestsAction(Region.NALWOR_CITY));
			    add(ConversationStates.ATTENDING,
						"ados",
						null,
						ConversationStates.ATTENDING,
						null,
						new SayNPCNamesForUnstartedQuestsAction(Region.ADOS_CITY));
			    add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(Region.SEMOS_CITY),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(Region.SEMOS_CITY));
			    add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(Region.NALWOR_CITY),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(Region.NALWOR_CITY));
			    add(ConversationStates.ATTENDING,
						"",
						new TriggerIsNPCNameForUnstartedQuestCondition(Region.ADOS_CITY),
						ConversationStates.ATTENDING,
						null,
						new SayUnstartedQuestDescriptionFromNPCNameAction(Region.ADOS_CITY));
				addGoodbye("Bye, remember to take care of yourself.");
			}

			@Override
			protected void createPath() {
				// do not walk so that admins can
				// idle here 24/7 without using cpu and bandwith.
			}

		};
		new HealerAdder().addHealer(npc, 0);
		npc.setPosition(16, 7);
		npc.setDescription("You see Skye. She knows everything, Admins should know and always has a smile on her face for them :)");
		npc.setDirection(Direction.DOWN);
		npc.setEntityClass("beautifulgirlnpc");
		zone.add(npc);
	}

}
