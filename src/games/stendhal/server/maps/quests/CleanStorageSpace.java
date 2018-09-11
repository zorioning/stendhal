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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.Pair;

/**
 * QUEST: CleanStorageSpace
 * <p>
 * PARTICIPANTS:
 * <li> Eonna
 * <p>
 * STEPS:
 * <li> Eonna asks you to clean her storage space.
 * <li> You go kill at least a rat, a cave rat and a cobra.
 * <li> Eonna checks your kills and then thanks you.
 * <p>
 * REWARD:
 * <li> 100 XP, karma
 * <p>
 * REPETITIONS:
 * <li> None.
 */
public class CleanStorageSpace extends AbstractQuest {
	private static final String QUEST_SLOT = "clean_storage";

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
		res.add("我在塞门镇面包房隔壁的房子里见到了 Eonna.");
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if ("rejected".equals(questState)) {
			res.add("我不想帮她清除储藏室的害虫.");
			return res;
		}
		res.add("我向 Eonna 保证去地下室清除里面的老鼠和蛇.");
		if ("start".equals(questState) && player.hasKilled("老鼠") && player.hasKilled("洞穴老鼠") && player.hasKilled("蛇") || "done".equals(questState)) {
			res.add("我已清除了 Eonna 的储藏室.");
		}
		if ("done".equals(questState)) {
			res.add("哇, Eonna 觉得我是大英雄. *blush*");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Eonna");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"我的 #地下室 被老鼠占领了, 你能帮帮我吗?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"再次感谢! 我觉得地下室干净多了.", null);

		final List<ChatAction> start = new LinkedList<ChatAction>();

		final HashMap<String, Pair<Integer, Integer>> toKill =
			new HashMap<String, Pair<Integer, Integer>>();
		// first number is required solo kills, second is required shared kills
		toKill.put("老鼠", new Pair<Integer, Integer>(0,1));
		toKill.put("洞穴老鼠", new Pair<Integer, Integer>(0,1));
		toKill.put("蛇", new Pair<Integer, Integer>(0,1));

		start.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		start.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Oh, 谢谢你! 我在上面等着, 如果有老鼠想逃跑, 我会拿扫把打死它!",
				new MultipleActions(start));

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"*sigh* 好吧, 也许还有别的英雄帮我...",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -2.0));

		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("地下室", "储藏室"),
				null,
				ConversationStates.QUEST_OFFERED,
				"是的, 就是楼梯下面, 都是一群群恶心的老鼠, 还有蛇！你要小心点...可以帮帮我吗?",
				null);
	}

	private void step_2() {
		// Go kill at least a rat, a cave rat and a snake.
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Eonna");

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new IncreaseKarmaAction(5.0));
		reward.add(new IncreaseXPAction(100));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		// the player returns to Eonna after having started the quest.
		// Eonna checks if the player has killed one of each animal race.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"), new KilledForQuestCondition(QUEST_SLOT,1)),
				ConversationStates.ATTENDING, "真是个大英雄! 谢谢你!",
				new MultipleActions(reward));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"), new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.QUEST_STARTED,
				"你不记得曾说过要帮我清除 #地下室 的害虫了吗?",
				null);

		npc.add(
				ConversationStates.QUEST_STARTED,
				"地下室",
				null,
				ConversationStates.ATTENDING,
				"就是我说的那个楼梯下面. 请清除下面全部的老鼠, 可能还有蛇!",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"清理储藏室",
				"Eonna 很害怕进到她的地下储藏室, 由于里面被老鼠和蛇占据了.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "CleanStorageSpace";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Eonna";
	}
}
