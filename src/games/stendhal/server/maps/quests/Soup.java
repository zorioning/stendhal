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

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.maps.Region;
import marauroa.common.game.IRPZone;

/**
 * QUEST: Special 蔬菜汤.
 * <p>
 * PARTICIPANTS: <ul><li> 老妇荷茉娜 in Fado tavern</ul>
 *
 * STEPS: <ul><li> 老妇荷茉娜 tells you the ingredients of a special 蔬菜汤 <li> You
 * collect the ingredients <li> You bring the ingredients to the tavern <li> The 蔬菜汤
 * is served at table<li> Eating the 蔬菜汤 heals you fully over time <li> Making it adds karma
 * </ul>
 *
 * REWARD: <ul><li>healing 蔬菜汤 <li> Karma bonus of 5 (if ingredients given individually)<li>20 XP</ul>
 *
 * REPETITIONS: <ul><li> as many as desired <li> Only possible to repeat once every ten
 * minutes</ul>
 *
 * @author kymara
 */
public class Soup extends AbstractQuest {

	private static final List<String> NEEDED_FOOD = Arrays.asList("胡萝卜",
			"菠菜", "西葫芦", "甘蓝", "色拉", "洋葱", "洋花菜",
			"西蓝花", "韭菜");

	private static final String QUEST_SLOT = "soup_maker";

	private static final int REQUIRED_MINUTES = 10;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	/**
	 * Returns a list of the names of all food that the given player still has
	 * to bring to fulfill the quest.
	 *
	 * @param player
	 *            The player doing the quest
	 * @param hash
	 *            If true, sets a # character in front of every name
	 * @return A list of food item names
	 */
	private List<String> missingFood(final Player player, final boolean hash) {
		final List<String> result = new LinkedList<String>();

		String doneText = player.getQuest(QUEST_SLOT);
		if (doneText == null) {
			doneText = "";
		}
		final List<String> done = Arrays.asList(doneText.split(";"));
		for (String ingredient : NEEDED_FOOD) {
			if (!done.contains(ingredient)) {
				if (hash) {
					ingredient = "#" + ingredient;
				}
				result.add(ingredient);
			}
		}
		return result;
	}

	/**
	 * Serves the soup as a reward for the given player.
	 * @param player to be rewarded
	 */
	private void placeSoupFor(final Player player) {
		final Item soup = SingletonRepository.getEntityManager()
				.getItem("蔬菜汤");
		final IRPZone zone = SingletonRepository.getRPWorld().getZone("int_fado_tavern");
		// place on table (for effect only :) )
		soup.setPosition(17, 23);
		// only allow player who made soup to eat the soup
		soup.setBoundTo(player.getName());
		// here the soup is altered to have the same heal value as the player's
		// base HP. soup is already persistent so it will last.
		soup.put("amount", player.getBaseHP());
		zone.add(soup);
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("老妇荷茉娜");

		// player says hi before starting the quest
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.INFORMATION_1,
			"Hello, stranger. You look weary from your travels. I know what would #revive you.",
			null);

		// player returns after finishing the quest (it is repeatable) after the
		// time as finished
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestCompletedCondition(QUEST_SLOT),
					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
			ConversationStates.QUEST_OFFERED,
			"Hello again. Have you returned for more of my special 蔬菜汤?",
			null);

		// player returns after finishing the quest (it is repeatable) before
		// the time as finished
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES , "I hope you don't want more 蔬菜汤, because I haven't finished washing the dishes. Please check back in")
			);

		// player responds to word 'revive'
		npc.add(ConversationStates.INFORMATION_1,
				"revive",
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				null,
				new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (player.hasQuest(QUEST_SLOT) && player.isQuestCompleted(QUEST_SLOT)) {
						// to be honest i don't understand when this
								// would be implemented. i put the text i
								// want down in stage 3 and it works fine.
						npc.say("I have everything for the recipe now.");
						npc.setCurrentState(ConversationStates.ATTENDING);
					} else {
						npc.say("My special 蔬菜汤 has a magic touch. "
								+ "I need you to bring me the #ingredients.");
					}
				}
			});

		// player asks what exactly is missing
		npc.add(ConversationStates.QUEST_OFFERED, "ingredients", null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final List<String> needed = missingFood(player, true);
					npc.say("I need "
							+ needed.size() +
									"ingredient"
							+ " before I make the 蔬菜汤: "
							+ needed
							+ ". Will you collect them?");
				}
			});

		// player is willing to collect
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.QUESTION_1,
			"You made a wise choice. Do you have anything I need already?",
			new SetQuestAction(QUEST_SLOT, ""));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Oh, never mind. It's your loss.", null);

		// players asks about the vegetables individually
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("菠菜", "西葫芦", "洋葱", "洋花菜", "西蓝花", "韭菜"),
			null,
			ConversationStates.QUEST_OFFERED,
			"You will find that in allotments in Fado. So will you fetch the ingredients?",
			null);

		// players asks about the vegetables individually
		npc.add(ConversationStates.QUEST_OFFERED, "甘蓝", null,
			ConversationStates.QUEST_OFFERED,
			"That grows indoors in pots. Someone like a witch or an elf might grow it. "
					+ "So will you fetch the ingredients?", null);

		// players asks about the vegetables individually
		npc.add(
			ConversationStates.QUEST_OFFERED,
			Arrays.asList("色拉", "胡萝卜"),
			null,
			ConversationStates.QUEST_OFFERED,
			"I usually have to get them imported from 塞门镇. So do you want the 蔬菜汤?",
			null);
	}

	private void step_2() {
		// Fetch the ingredients and bring them back to Helena.
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("老妇荷茉娜");

		// player returns while quest is still active
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.QUESTION_1,
			"Welcome back! I hope you collected some #ingredients for the 蔬菜汤, or #everything.",
			null);

		// player asks what exactly is missing
		npc.add(ConversationStates.QUESTION_1, "ingredients",
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.QUESTION_1, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final List<String> needed = missingFood(player, true);
					npc.say("I still need "
							+ needed.size()+
									"ingredient"+ ": "
							+ needed
							+ ". Did you bring anything I need?");
				}
			});

		// player says he has a required ingredient with him
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_1, "What did you bring?", null);

		for(final String itemName : NEEDED_FOOD) {
			npc.add(ConversationStates.QUESTION_1, itemName, null,
				ConversationStates.QUESTION_1, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						List<String> missing = missingFood(player, false);

						if (missing.contains(itemName)) {
							if (player.drop(itemName)) {
								// register ingredient as done
								final String doneText = player.getQuest(QUEST_SLOT);
								player.setQuest(QUEST_SLOT, doneText + ";" + itemName);

								// check if the player has brought all Food
								missing = missingFood(player, true);

								if (!missing.isEmpty()) {
									npc.say("Thank you very much! What else did you bring?");
								} else {
									player.addKarma(5.0);
									player.addXP(20);
									/*
									 * place soup after XP added otherwise
									 * the XP change MIGHT change level and
									 * player MIGHT gain health points which
									 * changes the base HP, which is desired
									 * to be accurate for the place 蔬菜汤
									 * stage
									 */
									placeSoupFor(player);
									player.getStatusList().removeAll(PoisonStatus.class);
									npc.say("The 蔬菜汤's on the table for you. It will heal you. "
											+ "My magical method in making the 蔬菜汤 has given you a little karma too.");
									player.setQuest(QUEST_SLOT, "done;"
											+ System.currentTimeMillis());
									player.notifyWorldAboutChanges();
									npc.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Don't take me for a fool, traveller. You don't have "
									+ itemName
									+ " with you.");
							}
						} else {
							npc.say("You brought me that ingredient already.");
						}
					}
			});
		}

		// Perhaps player wants to give all the ingredients at once
		npc.add(ConversationStates.QUESTION_1, "everything",
				null,
				ConversationStates.QUESTION_1,
				null,
				new ChatAction() {
			    @Override
				public void fire(final Player player, final Sentence sentence,
					   final EventRaiser npc) {
			    	checkForAllIngredients(player, npc);
			}
		});

		// player says something which isn't in the needed food list.
		npc.add(ConversationStates.QUESTION_1, "",
			new NotCondition(new TriggerInListCondition(NEEDED_FOOD)),
			ConversationStates.QUESTION_1,
			"I won't put that in your 蔬菜汤.", null);

		// allow to say goodbye while Helena is listening for food names
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.GOODBYE_MESSAGES, null,
				ConversationStates.IDLE, "Bye.", null);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.ATTENDING,
			"I'm not sure what you want from me, then.", null);

		// player says he didn't bring any Food to different question
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
			new AndCondition(new QuestStartedCondition(QUEST_SLOT), new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "done"))),
			ConversationStates.ATTENDING, "Okay then. Come back later.",
			null);
	}

	// if we're checking all at once it's a bit different method
	// also player gets no karma (don't get karma for being lazy)
	private void checkForAllIngredients(final Player player, final EventRaiser npc) {
		List<String> missing = missingFood(player, false);
		for (final String food : missing) {
		if (player.drop(food)) {
			// register ingredient as done
			final String doneText = player.getQuest(QUEST_SLOT);
			player.setQuest(QUEST_SLOT, doneText + ";"
			+ food);
			}
		}
		// check if the player has brought all Food
		missing = missingFood(player, true);
		if (!missing.isEmpty()) {
			npc.say("You didn't have all the ingredients I need. I still need "
							+ missing.size() +
									"ingredient" + ": "
							+ missing
							+ ". You'll get bad karma if you keep making mistakes like that!");
			// to fix bug [ 2517439 ]
			player.addKarma(-5.0);
			return;
		} else {
			// you get less XP if you did it the lazy way
			// and no karma
			player.addXP(20);
			placeSoupFor(player);
			player.getStatusList().removeAll(PoisonStatus.class);
			npc.say("The soup's on the table for you, it will heal you. Tell me if I can help you with anything else.");
			player.setQuest(QUEST_SLOT, "done;"
					+ System.currentTimeMillis());
			player.notifyWorldAboutChanges();
			npc.setCurrentState(ConversationStates.ATTENDING);
		}
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"蔬菜汤",
				"老妇荷茉娜 makes a wonderful vegetable 蔬菜汤.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			if (!isCompleted(player)) {
				res.add("I'm collecting ingredients to make vegetable 蔬菜汤. I still need " + missingFood(player, false) + ".");
			} else if(isRepeatable(player)){
				res.add("老妇荷茉娜 is ready to make 蔬菜汤 for me again!");
			} else {
				res.add("I made some healthy 蔬菜汤. 老妇荷茉娜 is now busy washing the dishes.");
			}
			return res;
	}

	@Override
	public String getName() {
		return "蔬菜汤";
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,REQUIRED_MINUTES)).fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}

	@Override
	public String getNPCName() {
		return "老妇荷茉娜";
	}
}
