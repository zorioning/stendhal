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
package games.stendhal.server.entity.npc.behaviour.adder;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.SoundLayer;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.BehaviourAction;
import games.stendhal.server.entity.npc.action.ComplainAboutSentenceErrorAction;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.journal.MerchantsRegister;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.SentenceHasErrorCondition;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.SoundEvent;

public class BuyerAdder {
	private static Logger logger = Logger.getLogger(BuyerAdder.class);

    private final MerchantsRegister merchantsRegister = SingletonRepository.getMerchantsRegister();

	/**
	 * Behaviour parse result in the current conversation.
	 * Remark: There is only one conversation between a player and the NPC at any time.
	 */
	private ItemParserResult currentBehavRes;

	public void addBuyer(final SpeakerNPC npc, final BuyerBehaviour buyerBehaviour, final boolean offer) {
		final Engine engine = npc.getEngine();

		merchantsRegister.add(npc, buyerBehaviour);

		if (offer) {
			engine.add(
					ConversationStates.ATTENDING,
					ConversationPhrases.OFFER_MESSAGES,
					null,
					false,
					ConversationStates.ATTENDING,
					"我想买 " + buyerBehaviour.dealtItems() + ".",
					null);
		}
//		engine.add(ConversationStates.ATTENDING, "sell", new SentenceHasErrorCondition(),
		engine.add(ConversationStates.ATTENDING, ConversationPhrases.SELL_MESSAGES, new SentenceHasErrorCondition(),
				false, ConversationStates.ATTENDING,
				null, new ComplainAboutSentenceErrorAction());

//		engine.add(ConversationStates.ATTENDING, "sell",
		engine.add(ConversationStates.ATTENDING, ConversationPhrases.SELL_MESSAGES,
			new AndCondition(
					new NotCondition(new SentenceHasErrorCondition()),
					new NotCondition(buyerBehaviour.getTransactionCondition())),
			false, ConversationStates.ATTENDING,
			null, buyerBehaviour.getRejectedTransactionAction());

//		engine.add(ConversationStates.ATTENDING, "sell",
		engine.add(ConversationStates.ATTENDING, ConversationPhrases.SELL_MESSAGES,
				new AndCondition(
						new NotCondition(new SentenceHasErrorCondition()),
						buyerBehaviour.getTransactionCondition()),
				false, ConversationStates.ATTENDING,
				null,
				new BehaviourAction(buyerBehaviour, "sell", "buy") {
//				new BehaviourAction(buyerBehaviour,  ConversationPhrases.SELL_MESSAGES, ConversationPhrases.BUY_MESSAGES) {
					@Override
					public void fireRequestOK(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.isBadBoy()) {
							// don't buy from player killers at all
							raiser.say("抱歉, 我不能相信你, 你看起来太危险, 走开!");
							raiser.setCurrentState(ConversationStates.IDLE);
							return;
						}

						String chosenItemName = res.getChosenItemName();

						if (res.getAmount() > 1000) {
							logger.warn("拒绝购买如此巨量的 "
									+ res.getAmount()
									+ " " + chosenItemName
									+ " "
									+ player.getName() + " 对 "
									+ raiser.getName() + " 讲 "
									+ sentence);
							raiser.say("抱歉, 一次性购买"
									+ chosenItemName
									+ " 的最大数量是 1000.");
						} else if (res.getAmount() > 0) {
							final String itemName = chosenItemName;
							// will check if player have claimed amount of items
							if (itemName.equals("sheep")) {
								// player have no sheep...
								if (!player.hasSheep()) {
									raiser.say("你没有羊, " + player.getTitle() + "! 你拉的是什么?");
									return;
								}
							} else {
								// handle other items as appropriate
							}

							final int price = buyerBehaviour.getCharge(res, player);

							if (price != 0) {
    							raiser.say(res.getAmount() + chosenItemName
    									+ " " + res.getAmount() + " 价值 "
    									+ price + ". 你要卖出 "
    									+ res.getAmount() + "?");

    							currentBehavRes = res;
    							npc.setCurrentState(ConversationStates.SELL_PRICE_OFFERED); // success
							} else {
								raiser.say("抱歉, "
										+ res.getAmount() + " "
										+ chosenItemName
    									+ " 不值钱worth nothing.");
							}
						} else {
							raiser.say("抱歉, 你要卖出多少 " + chosenItemName + " ?");
						}
					}
				});

		engine.add(ConversationStates.SELL_PRICE_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				false, ConversationStates.ATTENDING,
				null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						logger.debug("Buying something from player " + player.getName());

						boolean success = buyerBehaviour.transactAgreedDeal(currentBehavRes, raiser, player);
						if (success) {
							raiser.addEvent(new SoundEvent("coins-1", SoundLayer.CREATURE_NOISE));
						}

						currentBehavRes = null;
					}
				});

		engine.add(ConversationStates.SELL_PRICE_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				false,
				ConversationStates.ATTENDING, "Ok, 还有什么事吗？", null);
	}

}
