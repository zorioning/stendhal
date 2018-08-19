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
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Find Ghosts
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Carena</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Carena asks you to find the 4 other spirits on Faiumoni</li>
 * <li> You go find them and remember their names</li>
 * <li> You return and say the names</li>
 * <li> Carena checks you have met them, then gives reward</li>
 * <li> Note: you can meet the ghosts before you started the quest with her</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> base HP bonus of 50</li>
 * <li> 5000 XP</li>
 * <li> Karma: 15</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> None.</li>
 * </ul>
 */
public class FindGhosts extends AbstractQuest {

	private static Logger logger = Logger.getLogger(FindGhosts.class);

	public static final String QUEST_SLOT = "find_ghosts";

	private static final List<String> NEEDED_SPIRITS =
		Arrays.asList("mary", "ben", "zak", "goran");

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private List<String> missingNames(final Player player) {
		if (!player.hasQuest(QUEST_SLOT)) {
			return NEEDED_SPIRITS;
		}
		/*
		 * the format of the list quest slot is
		 * "looking;name;name;...:said;name;name;..."
		 */
		final String npcDoneText = player.getQuest(QUEST_SLOT).toLowerCase();
		final String[] doneAndFound = npcDoneText.split(":");
		final List<String> result = new LinkedList<String>();
		if (doneAndFound.length > 1) {
		    final String[] done = doneAndFound[1].split(";");
		    final List<String> doneList = Arrays.asList(done);
		    for (final String name : NEEDED_SPIRITS) {
				if (!doneList.contains(name)) {
				    result.add(name);
				}
		    }
		}
		return result;
	}

	private void askingStep() {
		final SpeakerNPC npc = npcs.get("Carena");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new OrCondition(new QuestNotStartedCondition(QUEST_SLOT), new QuestInStateCondition(QUEST_SLOT, "rejected")),
			ConversationStates.QUEST_OFFERED,
			"我很孤单，我只能看到生物和活人，如果我见到有和我一样的 #spirits , 我会感觉好点.",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"我想你能帮忙找到像我一样的精灵，如果找到了，把他们的名字告诉我。",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"谢谢！我知道了在Faiumoni有像我一样的精灵，现在我觉得好多了。",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"你真有爱，希望你能找到他们，祝好运。",
			new SetQuestAction(QUEST_SLOT, "looking:said"));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"唉，没办法，我只是一只幽灵，也不能给你提供什么有用的。",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -15.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("spirits", "spirit"),
			null,
			ConversationStates.QUEST_OFFERED,
			"我意思另外有4只精灵，如果我知道他们的名字，我自已就可以联系到他们。你能找到他们吗。找到的话请把他们的名字告诉我。",
			null);
	}

	private void findingStep() {
		// see the separate GhostNPC classes for what happens when a player
		// finds a ghost (with or without quest slot defined)
	}

	private void tellingStep() {

		final SpeakerNPC npc = npcs.get("Carena");

		// the player returns to Carena after having started the quest, or found
		// some ghosts.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
			ConversationStates.QUESTION_1,
			"如果你找到任何 #spirits, 请告诉我他们的名字.", null);

		for(final String spiritName : NEEDED_SPIRITS) {
			npc.add(ConversationStates.QUESTION_1, spiritName, null,
				ConversationStates.QUESTION_1, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String name = spiritName;

						// although all names are stored as lower case from now on,
						// older versions did not,
						// so we have to be compatible with them
						final String npcQuestText = player.getQuest(QUEST_SLOT).toLowerCase();
						final String[] npcDoneText = npcQuestText.split(":");
		    			final String lookingStr;
		    			final String saidStr;
						if (npcDoneText.length > 1) {
							lookingStr = npcDoneText[0];
							saidStr = npcDoneText[1];
						} else {
							// compatibility with broken quests
							logger.warn("玩家 " + player.getTitle() + " 的找出幽灵任务已接受 " + player.getQuest(QUEST_SLOT) + " - 现在把它设置为完成。");
							player.setQuest(QUEST_SLOT, "done");
							npc.say("抱歉，看起来你已发现他们了，我有点乱.");
							player.notifyWorldAboutChanges();
							npc.setCurrentState(ConversationStates.ATTENDING);
							return;
						}

						final List<String> looking = Arrays.asList(lookingStr.split(";"));
						final List<String> said = Arrays.asList(saidStr.split(";"));
						String reply = "";
						List<String> missing = missingNames(player);
						final boolean isMissing = missing.contains(name);

						if (isMissing && looking.contains(name) && !said.contains(name)) {
							// we haven't said the name yet so we add it to the list
							player.setQuest(QUEST_SLOT, lookingStr
									+ ":" + saidStr + ";" + name);
							reply = "谢谢你.";
						} else if (!looking.contains(name)) {
							// we have said it was a valid name but haven't met them
							reply = "我不相信你已经和那些名字中的精灵说过话。";
						} else if (!isMissing && said.contains(name)) {
							// we have said the name so we are stupid!
							reply = "你已告诉我他们的名字了，谢谢.";
						} else {
							assert false;
						}

						// we may have changed the missing list
						missing = missingNames(player);

						if (!missing.isEmpty()) {
							reply += " 如果你见到任何精灵，请告诉我他们的名字。";
							npc.say(reply);
						} else {
							player.setBaseHP(50 + player.getBaseHP());
							player.heal(50, true);
							player.addXP(5000);
							player.addKarma(15);
							reply += " 现在我已知道了他们四人的名字，现在我已经可以把意念传递给他们。虽然不能给你什么有价值的东西,但我送你的基础健康a boost to your basic wellbeing。，会永远陪伴你，也许让你能活得更长，活得更有质量。 ";
							npc.say(reply);
							player.setQuest(QUEST_SLOT, "done");
							player.notifyWorldAboutChanges();
							npc.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				});
		}

		final List<String> triggers = new ArrayList<String>();
		triggers.add(ConversationPhrases.NO_EXPRESSION);
		triggers.addAll(ConversationPhrases.GOODBYE_MESSAGES);
		npc.add(ConversationStates.QUESTION_1, triggers, null,
				ConversationStates.IDLE, "没关系，等会回来.", null);

		// player says something which isn't in the needed spirits list.
		npc.add(
			ConversationStates.QUESTION_1,
			"",
			new NotCondition(new TriggerInListCondition(NEEDED_SPIRITS)),
			ConversationStates.QUESTION_1,
			"抱歉，我没明白，你想说的是什么名字?",
			null);

		npc.add(
			ConversationStates.QUESTION_1,
			Arrays.asList("spirits", "spirit"),
			null,
			ConversationStates.QUESTION_1,
			"I seek to know more about other spirits who are dead but stalk the earthly world as ghosts. Please tell me any names you know.",
			null);

		// the player goes to Carena and says hi, and has no quest or is completed.
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new NotCondition(new QuestActiveCondition(QUEST_SLOT))),
				ConversationStates.ATTENDING, "哇aaaaa!",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"寻找幽灵",
				"从前，一些旅行者谈到他们普在去Faiumoni的路上见到一些精灵，其中的一个年轻精灵叫 Carena，归隐在Ados附近的某处，并等着好心人的帮助。..",
				true);
		askingStep();
		findingStep();
		tellingStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			res.add("Carena 很孤独，他想知道世界上的其他同类，我必须找到他的同类并把每个精灵的名字告诉他。");
			if ("rejected".equals(player.getQuest(QUEST_SLOT))) {
				res.add("恩, no thanks, ghosts are creepy.");
				return res;
			}
			if (!isCompleted(player)) {
				res.add("我还剩 " + missingNames(player).size() + " " + "个幽灵的名字要告诉Carena.");
			} else {
				res.add("Carena听到其他同类的名字后感到一些安慰。他送给我 a boost to my basic health ,让我像他一样让永远存在.");
			}
			return res;
	}

	@Override
	public String getName() {
		return "FindGhosts";
	}

	@Override
	public int getMinLevel() {
		return 10;
	}

	@Override
	public String getNPCName() {
		return "Carena";
	}

	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
