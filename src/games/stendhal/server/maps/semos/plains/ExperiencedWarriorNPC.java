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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Experienced warrior knowing a lot about creatures (location semos_plains_s).
 * Original name: Starkad
 *
 * @author johnnnny
 */

public class ExperiencedWarriorNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	/**
	 * cost of the information for players. Final cost is: INFORMATION_BASE_COST +
	 * creatureLevel * INFORMATION_COST_LEVEL_FACTOR
	 */
	static final int INFORMATION_BASE_COST = 2;

	/**
	 * multiplier of the creature level for the information cost.
	 */
	static final double INFORMATION_COST_LEVEL_FACTOR = 3;

	/**
	 * literals for probabilities. %s is replaced with item description (name
	 * and amount)
	 */
	private static Map<Double, String> probabilityLiterals;

	/**
	 * literal for item amounts %s is replaced with singular item name, %a with
	 * "a/an item name" depending on the item name.
	 */
	private static Map<Integer, String> amountLiterals;

	/**
	 * literal for how dangerous a creature is based on the percentage
	 * difference to player level %s is replaced with singular creature name, %S
	 * with plural.
	 */
	private static Map<Double, String> dangerLiterals;

	/**
	 * literals for line starts. %s is replaced with singular creature name, %S
	 * plural.
	 */
	private static final String[] LINE_STARTS = new String[] { "Oh 是的,我知道 %s!",
		"当我在你这个年纪，我杀了很多 %S!",
		"这些 %S 是我喜欢的一种!",
		"让我想想...%s...我想起来了!",
		"上次我差点被 %a 杀死!",
		"I've had some nice battles with %S!", };

	static {
		probabilityLiterals = new LinkedHashMap<Double, String>();
		probabilityLiterals.put(100.0, "一贯 %s");
		probabilityLiterals.put(99.99, "几乎一贯 %s");
		probabilityLiterals.put(75.0, "大多数情况 %s");
		probabilityLiterals.put(55.0, "多数情况 %s");
		probabilityLiterals.put(40.0, "经常 %s");
		probabilityLiterals.put(20.0, "时常 %s");
		probabilityLiterals.put(5.0, "有时 %s");
		probabilityLiterals.put(1.0, "偶尔 %s");
		probabilityLiterals.put(0.1, "很少 %s");
		probabilityLiterals.put(0.001, "稀有 %s");
		probabilityLiterals.put(0.0001, "非常稀有 %s");
		probabilityLiterals.put(0.00000001,
		"也许 %s too, 但我只是听说");
		probabilityLiterals.put(0.0, "不可能 %s");

		amountLiterals = new LinkedHashMap<Integer, String>();
		amountLiterals.put(2000, "数千 %s");
		amountLiterals.put(200, "数百 %s");
		amountLiterals.put(100, "许多 %s");
		amountLiterals.put(10, "一些 %s");
		amountLiterals.put(2, "几个 %s");
		amountLiterals.put(1, "一个 %a");

		dangerLiterals = new LinkedHashMap<Double, String>();
		dangerLiterals.put(40.0, "%s 会秒杀你!");
		dangerLiterals.put(15.0,
		"%s 对你是致命的, 不要去招惹!");
		dangerLiterals.put(2.0, "%s 对你绝对危险, 提高警戒!");
		dangerLiterals.put(1.8, "%S 对你非常危险, 小心!");
		dangerLiterals.put(1.7,
		"%S 对你有危险, 记得及时补血!");
		dangerLiterals.put(1.2,
		"它对你有点危险，注意血量!");
		dangerLiterals.put(0.8,
		"要杀死它，很有挑战性!");
		dangerLiterals.put(0.5, "杀死 %s 对你来说很平常.");
		dangerLiterals.put(0.3, "杀死 %s 应该很容易.");
		dangerLiterals.put(0.0, "%s 不对你构成危协.");
	}

	/**
	 * %1 = time to respawn.
	 */
	private static final String[] RESPAWN_TEXTS = new String[] {
		"如果你在正确的地点等待 %1, 应该可能见到一只.",
		"可能只有 %1 的机会能发现一只.", "猎杀它们能达到 %1 ，对你来说就是个好机会." };

	/**
	 * %1 = list of items dropped.
	 */
	private static final String[] CARRY_TEXTS = new String[] { "They carry %1.",
		"Dead ones have %1.", "The corpses contain %1." };

	/**
	 * no attributes.
	 */
	private static final String[] CARRY_NOTHING_TEXTS = new String[] {
		"I don't know if they carry anything.",
	"None of the ones I've seen carried anything." };

	/**
	 * %1 = list of locations.
	 */
	private static final String[] LOCATION_TEXTS = new String[] {
		"I have seen them %1.", "You should be able to find them %1.",
	"I have killed few of those %1." };

	/**
	 * %1 = name of the creature.
	 */
	private static final String[] LOCATION_UNKNOWN_TEXTS = new String[] { "I don't know of any place where you could find %1." };

	private static CreatureInfo creatureInfo = new CreatureInfo(probabilityLiterals,
			amountLiterals, dangerLiterals, LINE_STARTS, RESPAWN_TEXTS,
			CARRY_TEXTS, CARRY_NOTHING_TEXTS, LOCATION_TEXTS,
			LOCATION_UNKNOWN_TEXTS);

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Starkad") {

			@Override
			@SuppressWarnings("all") // "dead"
			public void createDialog() {
				class StateInfo {
					private String creatureName;

					private int informationCost;

					void setCreatureName(final String creatureName) {
						this.creatureName = creatureName;
					}

					String getCreatureName() {
						return creatureName;
					}

					void setInformationCost(final int informationCost) {
						this.informationCost = informationCost;
					}

					int getInformationCost() {
						return informationCost;
					}
				}

				final StateInfo stateInfo = new StateInfo();

				addGreeting();
				setLevel(368);

				addJob("My job? I'm a well known warrior, strange that you haven't heard of me!");
				addQuest("Thanks, but I don't need any help at the moment.");
				addHelp("If you want, I can tell you about the #creatures I have encountered.");
				addOffer("I offer you information on #creatures I've seen for a reasonable fee.");

				add(ConversationStates.ATTENDING, "creature", null,
						ConversationStates.QUESTION_1,
						"Which creature you would like to hear more about?", null);

				add(ConversationStates.QUESTION_1, "",
						new NotCondition(new TriggerInListCondition(ConversationPhrases.GOODBYE_MESSAGES)),
						ConversationStates.ATTENDING, null,
						new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser speakerNPC) {
						final String creatureName = sentence.getTriggerExpression().getNormalized();
						final DefaultCreature creature = SingletonRepository.getEntityManager().getDefaultCreature(creatureName);
						if (creature == null) {
							speakerNPC.say("I have never heard of such a creature! Please tell the name again.");
							speakerNPC.setCurrentState(ConversationStates.QUESTION_1);
						} else {
							stateInfo.setCreatureName(creatureName);
							if (INFORMATION_BASE_COST > 0) {
								final int informationCost = getCost(player, creature);
								stateInfo.setInformationCost(informationCost);
								speakerNPC.say("This information costs "
										+ informationCost
										+ ". Are you still interested?");
								speakerNPC.setCurrentState(ConversationStates.BUY_PRICE_OFFERED);
							} else {
								speakerNPC.say(getCreatureInfo(player,
										stateInfo.getCreatureName())
										+ " If you want to hear about another creature, just tell me which.");
								speakerNPC.setCurrentState(ConversationStates.QUESTION_1);
							}
						}
					}

					private int getCost(final Player player, final DefaultCreature creature) {
						return (int) (INFORMATION_BASE_COST + INFORMATION_COST_LEVEL_FACTOR
								* creature.getLevel());
					}
				});

				add(ConversationStates.BUY_PRICE_OFFERED,
						ConversationPhrases.YES_MESSAGES, null,
						ConversationStates.ATTENDING, null,
						new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser speakerNPC) {
						if (stateInfo.getCreatureName() != null) {
							if (player.drop("money",
									stateInfo.getInformationCost())) {
								String infoString = getCreatureInfo(player, stateInfo.getCreatureName());
								infoString += " If you want to hear about another creature, just tell me which.";
								speakerNPC.say(infoString);
								speakerNPC.setCurrentState(ConversationStates.QUESTION_1);
							} else {
								speakerNPC.say("You don't have enough money with you.");
							}
						}
					}
				});

				add(ConversationStates.BUY_PRICE_OFFERED,
						ConversationPhrases.NO_MESSAGES, null, ConversationStates.ATTENDING,
						"Ok, come back if you're interested later. What else can I do for you?", null);

				addGoodbye("Farewell and godspeed!");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(37,2));
				nodes.add(new Node(37,16));
				nodes.add(new Node(85,16));
				nodes.add(new Node(85,32));
				nodes.add(new Node(107,32));
				nodes.add(new Node(107,2));

				setPath(new FixedPath(nodes, true));
			}

		};
		npc.setPosition(37, 2);
		npc.setEntityClass("experiencedwarriornpc");
		npc.setDescription("You see Starkad, the mighty warrior and defender of Semos.");
		zone.add(npc);
	}

	private static String getCreatureInfo(final Player player, final String creatureName) {
		String result;
		final DefaultCreature creature = SingletonRepository.getEntityManager().getDefaultCreature(creatureName);
		if (creature != null) {
			result = creatureInfo.getCreatureInfo(player, creature, 3, 8, true);
		} else {
			result = "I have never heard of such a creature!";
		}
		return result;
	}
}
