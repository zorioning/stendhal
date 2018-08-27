/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Speak with Hayunn
 * <p>
 * PARTICIPANTS: <ul><li> Hayunn Naratha</ul>
 *
 * STEPS: <ul>
 * <li> Talk to Hayunn to activate the quest.
 * <li> He asks you to kill a rat, also offering to teach you how
 * <li> Return and get directions to Semos
 * <li> Return and learn how to click move, and get some URLs
 * </ul>
 *
 * REWARD: <ul><li> 20 XP <li> 5 gold coins <li> studded shield </ul>
 *
 * REPETITIONS: <ul><li> Get the URLs as much as wanted but you only get the reward once.</ul>
 */
public class MeetHayunn extends AbstractQuest {

	private static final String QUEST_SLOT = "meet_hayunn";

	private static final int TIME_OUT = 60;

	private static Logger logger = Logger.getLogger(MeetHayunn.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Hayunn Naratha 是我在这个世上第一个遇到的人，他给我的任务是杀死一只老鼠。");
		if (player.getQuest(QUEST_SLOT, 0).equals("start") && new KilledForQuestCondition(QUEST_SLOT,1).fire(player, null, null)) {
			res.add("我杀了一只老鼠，我应该回去告诉他!");
		}
		if (player.getQuest(QUEST_SLOT, 0).equals("start")) {
			return res;
		}
		res.add("我杀死了老鼠。Hayunn 会教我有关这个世界的更多知识。");
		if ("killed".equals(questState)) {
			return res;
		}
		res.add("Hayunn 给了我一点钱，并告诉我去找 Semos 镇的 Monogenes 要一张地图");
		if ("taught".equals(questState)) {
			return res;
		}
		res.add("Hayunn 告诉我很有用的生存知识，并给了我一面学徒盾和一些钱。");
		if (isCompleted(player)) {
			return res;
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("任务状态: " + questState);
		logger.error("History doesn't have a matching quest state for " + questState);
		return debug;
	}

	private void prepareHayunn() {

		final SpeakerNPC npc = npcs.get("Hayunn Naratha");

		// player wants to learn how to attack
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				ConversationStates.ATTENDING,
				"好吧，当我还是年轻的冒险家的时候，我轻点敌人打败他们，我相信这也是人要做的，祝好运，完成后回来见我",
				null);

		//player doesn't want to learn how to attack
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				ConversationStates.ATTENDING,
				"好的, 你挺聪明，我相信你一定能完成!",
				null);

		//player returns to Hayunn not having killed a rat
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT,1))),
				ConversationStates.ATTENDING,
				"我看你还没有完成杀老鼠的任务，还需要我告诉你怎么杀死它吗？",
				null);

		//player returns to Hayunn having killed a rat
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new IncreaseXPAction(10));
		actions.add(new SetQuestAction(QUEST_SLOT, "killed"));

		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.INFORMATION_1,
				"你杀死了老鼠！现在，我猜你想去游历一翻，那你知道去 Semos 的路吗？",
				new MultipleActions(actions));


	   	// The player has had enough info for now. Send them to semos. When they come back they can learn some more tips.

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new EquipItemAction("money", 5));
		reward.add(new IncreaseXPAction(10));
		reward.add(new SetQuestAction(QUEST_SLOT, "taught"));
		reward.add(new ExamineChatAction("monogenes.png", "Monogenes", "North part of Semos city."));

		npc.add(
			ConversationStates.INFORMATION_1,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.IDLE,
			"出门顺着路向东走，你不会错过 Semos 镇，如果你和 Monogenes 说话, 就是图片上的这个老人，他会给你一张地图，另外再给你5元钱做路费，拜拜！",
			new MultipleActions(reward));

	   	// incase player didn't finish learning everything when he came after killing the rat, he must have another chance. Here it is.
		// 'little tip' is a pun as he gives some money, that is a tip, too.
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "killed")),
				ConversationStates.INFORMATION_1,
		        "当你能杀死老鼠后麻利跑回来告诉我！我会给你点提示，你做的到吗？",
				null);

		// Player has returned to say hi again.
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "taught")),
				ConversationStates.INFORMATION_2,
		        "又见面了，你有向我学过东西吗？",
				null);

		npc.add(
			ConversationStates.INFORMATION_2,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_3,
			"也许现在你已经发现了 Semos 的地牢. 那里的地下走廊很狭窄，因此快速准确的移动会很有效, 你想详细了解,对吧? #Yes?",
			null);

		npc.add(
			ConversationStates.INFORMATION_3,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_4,
			"很简单，真的，只用点击你想移动的地方。另外还有比我讲给你的信息多得多的地方，你想知道去哪里找吗？",
			null);

		final String epilog = "经常打 #/faq 问一些问题，你就能找到答案.  \n还可以读到一些当前最勇猛、最成功的勇士排行在 #https://stendhalgame.org\n ";

			//This is used if the player returns, asks for #help and then say #yes
			npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES, new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			epilog + "你可知道，你让我想起了年轻时的自己...",
			null);

		final List<ChatAction> reward2 = new LinkedList<ChatAction>();
		reward2.add(new EquipItemAction("studded shield"));
		reward2.add(new IncreaseXPAction(20));
		reward2.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.INFORMATION_4,
				ConversationPhrases.YES_MESSAGES, new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				epilog + "好的，祝你在地牢中好运！这个盾可以帮助你。希望你能得到名望与荣耀。保持警惕！",
				new MultipleActions(reward2));

		npc.add(new ConversationStates[] { ConversationStates.ATTENDING,
					ConversationStates.INFORMATION_1,
					ConversationStates.INFORMATION_2,
					ConversationStates.INFORMATION_3,
					ConversationStates.INFORMATION_4},
				ConversationPhrases.NO_MESSAGES, new NotCondition(new QuestInStateCondition(QUEST_SLOT, "start")), ConversationStates.IDLE,
				"Oh , 我觉得其他人在等着我们结束谈话，再见...",
				null);

		npc.setPlayerChatTimeout(TIME_OUT);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"会见 Hayunn Naratha",
				"在这个世界上，Hayunn Naratha 是年轻英雄们重要的导师.",
				false);
		prepareHayunn();
	}

	@Override
	public String getName() {
		return "MeetHayunn";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Hayunn Naratha";
	}
}
