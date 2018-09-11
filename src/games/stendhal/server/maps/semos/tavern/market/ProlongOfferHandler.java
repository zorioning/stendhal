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
package games.stendhal.server.maps.semos.tavern.market;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.trade.Market;
import games.stendhal.server.entity.trade.Offer;
import games.stendhal.server.util.TimeUtil;

public class ProlongOfferHandler extends OfferHandler {
	@Override
	public void add(SpeakerNPC npc) {
		npc.add(ConversationStates.ATTENDING, "prolong", null, ConversationStates.ATTENDING, null,
				new ProlongOfferChatAction());
		npc.add(ConversationStates.SERVICE_OFFERED, ConversationPhrases.YES_MESSAGES,
				ConversationStates.ATTENDING, null, new ConfirmProlongOfferChatAction());
		// PRODUCTION is a misnomer for "prolong all" service, but it's a simple
		// way to distinguish it from a single prolong
		npc.add(ConversationStates.PRODUCTION_OFFERED, ConversationPhrases.YES_MESSAGES,
				ConversationStates.ATTENDING, null, new ConfirmProlongAllChatAction());
		npc.add(ConversationStates.SERVICE_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Ok, 还有需要我能帮你什么?", null);
		npc.add(ConversationStates.PRODUCTION_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Ok, 还有需要我能帮你什么?", null);
	}

	protected class ProlongOfferChatAction extends KnownOffersChatAction {

		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			if (sentence.hasError()) {
				npc.say("抱歉, 我不明白 "
						+ sentence.getErrorString());
			} else if (sentence.getExpressions().iterator().next().toString().equals("prolong")){
				handleSentence(player, sentence, npc);
			}
		}

		private void handleSentence(Player player, Sentence sentence, EventRaiser npc) {
			MarketManagerNPC manager = (MarketManagerNPC) npc.getEntity();
			try {
				String offerNumber = getOfferNumberFromSentence(sentence).toString();

				Map<String,Offer> offerMap = manager.getOfferMap();
				if (offerMap == null) {
					npc.say("请先核对你的供货.");
					return;
				}
				if(offerMap.containsKey(offerNumber)) {
					Offer o = offerMap.get(offerNumber);
					if(o.getOfferer().equals(player.getName())) {
						setOffer(o);
						int quantity = 1;
						if (o.hasItem()) {
							quantity = getQuantity(o.getItem());
						}
						StringBuilder message = new StringBuilder();

						if (TradeCenterZoneConfigurator.getShopFromZone(player.getZone()).contains(o)) {
							message.append("你代销的物品 ");
							message.append( o.getItemName());
							message.append(" 你希望延长 ");
							message.append(TimeUtil.approxTimeUntil((int) ((o.getTimestamp() - System.currentTimeMillis() + 1000 * OfferExpirer.TIME_TO_EXPIRING) / 1000)));
							message.append(". 的上架销售时间 ");
							message.append(TimeUtil.timeUntil(OfferExpirer.TIME_TO_EXPIRING));
							message.append(" 需要支付 ");
							message.append(TradingUtility.calculateFee(player, o.getPrice()).intValue());
							message.append(" 钱?");
						} else {
							message.append("你要延长代销商品 ");
							message.append(o.getItemName());
							message.append(" 的价格为 ");
							message.append(o.getPrice());
							message.append(" , 要支付手续费 ");
							message.append(TradingUtility.calculateFee(player, o.getPrice()).intValue());
							message.append(" 钱?");
						}
						npc.say(message.toString());
						npc.setCurrentState(ConversationStates.SERVICE_OFFERED);
					} else {
						npc.say("你只能延长自已的代销商品, 请说 #show #mine 查看你供应的商品.");
					}
				} else {
					npc.say("抱歉, 请选择一个数量给你要延长的商品.");
					return;
				}
			} catch (NumberFormatException e) {
				if (!handleProlongAll(player, sentence, npc)) {
					npc.say("抱歉, 请说 #prolong #number");
				}
			}
		}

		private boolean handleProlongAll(Player player, Sentence sentence, EventRaiser npc) {
			MarketManagerNPC manager = (MarketManagerNPC) npc.getEntity();
			int last = sentence.getExpressions().size();

			for (Expression expr : sentence.getExpressions().subList(1, last)) {
				if ("all".equals(expr.toString())) {
					Collection<Offer> offers = manager.getOfferMap().values();
					if (offers.isEmpty()) {
						npc.say("抱歉, 你必须指定需要延长的代销商品的序号.");
						return true;
					}
					int price = 0;
					int numOffers = offers.size();
					List<String> offerDesc = new ArrayList<>(numOffers);
					for (Offer o : offers) {
						if (!o.getOfferer().equals(player.getName())) {
							npc.say("你只能延长你自已的代销商品, 请说 #show #mine 或者 #show #expired 查看你的代销商品.");
							return true;
						}
						int quantity = 1;
						if (o.hasItem()) {
							quantity = getQuantity(o.getItem());
						}
						price += TradingUtility.calculateFee(player, o.getPrice()).intValue();
						offerDesc.add(o.getItemName()
								+ " 的定价 " + o.getPrice());
					}
					String total = numOffers > 1 ? "total" : "";
					npc.say("你想延长你的 "
							+ "代销商品" + " of "
							+ offerDesc
							+ " for a " + total + " 支付手续费 " + price + " 钱?");
					npc.setCurrentState(ConversationStates.PRODUCTION_OFFERED);
					return true;
				}
			}

			return false;
		}
	}

	protected class ConfirmProlongOfferChatAction implements ChatAction {
		@Override
		public void fire (Player player, Sentence sentence, EventRaiser npc) {
			Offer offer = getOffer();
			if (!wouldOverflowMaxOffers(player, offer)) {
				Integer fee = Integer.valueOf(TradingUtility.calculateFee(player, offer.getPrice()).intValue());
				if (player.isEquipped("money", fee)) {
					if (prolongOffer(player, offer)) {
						TradingUtility.substractTradingFee(player, offer.getPrice());
						npc.say("我已延长了你的上架时间, 收取了 "+fee.toString()+" 的手续费.");
					} else {
						npc.say("抱歉, 你供的货物已从市场下架.");
					}
					// Changed the status, or it has been changed by expiration. Obsolete the offers
					((MarketManagerNPC) npc.getEntity()).getOfferMap().clear();
				} else {
					npc.say("你不能负担起这些手续费 "+fee.toString());
				}
			} else {
				npc.say("抱歉, 你最多只能同时上架 " + TradingUtility.MAX_NUMBER_OFF_OFFERS
						+ " 种商品.");
			}
		}

		/**
		 * Check if prolonging an offer would result the player having too many active offers on market.
		 *
		 * @param player the player to be checked
		 * @param offer the offer the player wants to prolong
		 * @return true if prolonging the offer should be denied
		 */
		boolean wouldOverflowMaxOffers(Player player, Offer offer) {
			Market market = TradeCenterZoneConfigurator.getShopFromZone(player.getZone());

			if ((market.countOffersOfPlayer(player) == TradingUtility.MAX_NUMBER_OFF_OFFERS)
					&& market.getExpiredOffers().contains(offer)) {
				return true;
			}

			return false;
		}

		boolean prolongOffer(Player player, Offer o) {
			Market market = TradeCenterZoneConfigurator.getShopFromZone(player.getZone());
			if (market != null) {
				if (market.prolongOffer(o) != null) {
					String messageNumberOfOffers = "你现在已上架 "+Integer.valueOf(market.countOffersOfPlayer(player)).toString()+" 的商品.";
					player.sendPrivateText(messageNumberOfOffers);

					return true;
				}
			}

			return false;
		}
	}

	private class ConfirmProlongAllChatAction extends ConfirmProlongOfferChatAction {
		@Override
		public void fire (Player player, Sentence sentence, EventRaiser npc) {
			MarketManagerNPC manager = (MarketManagerNPC) npc.getEntity();
			Collection<Offer> offers = manager.getOfferMap().values();
			boolean clear = false;
			for (Offer offer : offers) {
				int quantity = 1;
				if (offer.hasItem()) {
					quantity = getQuantity(offer.getItem());
				}
				String offerDesc = offer.getItemName();

				if (!offer.getOfferer().equals(player.getName())) {
					// This should not be possible, but it does not hurt to check
					// it anyway.
					npc.say("你只能延长自已的商品时间.");
					// clear the offer map as it apparently resulted in an
					// incorrect request already.
					clear = true;
					break;
				}

				if (!wouldOverflowMaxOffers(player, offer)) {
					Integer fee = Integer.valueOf(TradingUtility.calculateFee(player, offer.getPrice()).intValue());
					if (player.isEquipped("money", fee)) {

						if (prolongOffer(player, offer)) {
							TradingUtility.substractTradingFee(player, offer.getPrice());
							npc.say("我延长了你的代销商品 " + offerDesc
									+ " , 并收取手续费" + fee.toString() + ".");
						} else {
							npc.say("抱歉, 商品 " + offerDesc + " 已从市场下架.");
						}
						clear = true;
					} else {
						npc.say("你不能付起手续费 " + fee.toString());
					}
				} else {
					npc.say("Sorry, 你最多只能有 " + TradingUtility.MAX_NUMBER_OFF_OFFERS
							+ " 种可同时代销的商品.");
					// Avoid complaining about the offer limit multiple times
					break;
				}
			}

			if (clear) {
				// Changed the status, or it has been changed by expiration. Obsolete the offers
				((MarketManagerNPC) npc.getEntity()).getOfferMap().clear();
			}
		}
	}
}
