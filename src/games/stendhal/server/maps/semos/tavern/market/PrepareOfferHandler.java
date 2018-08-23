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

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.dbcommand.LogTradeEventCommand;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.RingOfLife;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.trade.Market;
import games.stendhal.server.entity.trade.Offer;
import games.stendhal.server.util.AsynchronousProgramExecutor;
import marauroa.server.db.command.DBCommandQueue;


public class PrepareOfferHandler {
	private Item item;
	private int price;
	private int quantity;

	public void add(SpeakerNPC npc) {
		npc.add(ConversationStates.ATTENDING, "sell",
				new LevelLessThanCondition(6),
				ConversationStates.ATTENDING,
				"抱歉，我只接受有点名气的人的代理销售，你可以帮助镇民或到城外杀怪提高经验。", null);
		npc.add(ConversationStates.ATTENDING, "sell",
				new LevelGreaterThanCondition(5),
				ConversationStates.ATTENDING, null,
				new PrepareOfferChatAction());
		npc.add(ConversationStates.ATTENDING, "sell", null, ConversationStates.ATTENDING, null,
				new PrepareOfferChatAction());
		npc.add(ConversationStates.SELL_PRICE_OFFERED, ConversationPhrases.YES_MESSAGES,
				ConversationStates.ATTENDING, null, new ConfirmPrepareOfferChatAction());
		npc.add(ConversationStates.SELL_PRICE_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Ok, 还有什么事吗？", null);
	}

	private void setData(Item item, int price, int quantity) {
		this.item = item;
		this.price = price;
		this.quantity = quantity;
	}

	/**
	 * Builds the message for the tweet to be posted
	 * @param i the offered item
	 * @param q the quantity of the offered item
	 * @param p the price for the item
	 * @return the message to be posted in the tweet
	 */
	public String buildTweetMessage(Item i, int q, int p) {
		StringBuilder message = new StringBuilder();
		message.append(" 给出 ");
		message.append( i.getName());
		message.append(" 的定价 ");
		message.append(p);
		message.append(" 金币. ");
		String stats = "";
		String description = i.describe();
		int start = description.indexOf("状态 (");
		if(start > -1) {
			stats = description.substring(start);
		}
		message.append(stats);
		return message.toString();
	}

	private class PrepareOfferChatAction implements ChatAction {
		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			if (sentence.hasError()) {
				npc.say("抱歉，我不了解你拿的的奇怪玩意儿。");
				npc.setCurrentState(ConversationStates.ATTENDING);
			} else if (sentence.getExpressions().iterator().next().toString().equals("sell")){
				handleSentence(player, sentence, npc);
			}
		}

		private void handleSentence(Player player, Sentence sentence, EventRaiser npc) {
			if(TradingUtility.isPlayerWithinOfferLimit(player)) {
				if (sentence.getExpressions().size() < 3 || sentence.getNumeralCount() != 1) {
					npc.say("我不明白你说的，请说 \"卖 物品名称 价格\" 或 \"sell item price\".");
					npc.setCurrentState(ConversationStates.ATTENDING);
					return;
				}
				String itemName = determineItemName(sentence);
				int number = determineNumber(sentence);
				int price = determinePrice(sentence);
				Integer fee = Integer.valueOf(TradingUtility.calculateFee(player, price).intValue());
				if(TradingUtility.canPlayerAffordTradingFee(player, price)) {
					Item item = player.getFirstEquipped(itemName);
					if (item == null) {
						// Some items are in plural. look for those
						item = player.getFirstEquipped(itemName);
					}

					if (item == null) {
						npc.say("抱歉, 可是我没看到你身上有 "
								+ itemName + ".");
						return;
					}
					// The item name might not be what was used for looking it up (plurals)
					itemName = item.getName();

					if ((number > 1) && !(item instanceof StackableItem)) {
						npc.say("抱歉，你只能把这些东西拆成单个卖出.");
						return;
					} else if (item.isBound()) {
						npc.say("这个 " + itemName + " 只有由你使用，我不能代销它.");
						return;
					} else if (item.getDeterioration() > 0) {
						npc.say("这个 " + itemName + " 损坏了，我不能代销它.");
						return;
					} else if (number > 1000) {
						npc.say("抱歉，我的仓库放不下这么多的 " + itemName + ".");
						return;
					} else if (price > 1000000) {
						npc.say("狮子大张口啊，你的 " + itemName + " 要价太高. 很抱歉我不能帮你代销.");
						return;
					} else if (item.hasSlot("content") && item.getSlot("content").size() > 0) {
						npc.say("请先把你的 " + itemName + " 清空.");
						return;
					} else if (item instanceof RingOfLife) {
					    // broken ring of life should not be sold via Harold
					    if(((RingOfLife) item).isBroken()) {
					        npc.say("代销之前，请先把你的 " + itemName + " 修好.");
					        return;
					    }
					}

					// All looks ok so far. Ask confirmation from the player.
					setData(item, price, number);
					StringBuilder msg = new StringBuilder();
					msg.append("你想代理销售的 ");
					msg.append( itemName);
					msg.append(" ，总计 ");
					msg.append(price);
					msg.append(" 金币. 代销手续费 ");
					msg.append(fee);
					msg.append(" 金身.");
					npc.say(msg.toString());

					npc.setCurrentState(ConversationStates.SELL_PRICE_OFFERED);
					return;
				}
				npc.say("你不能付起手续费 " + fee.toString());
				return;
			}
			npc.say("为你上架代理销售的商品不能超过 " + TradingUtility.MAX_NUMBER_OFF_OFFERS + " 批次.");
		}

		private int determineNumber(Sentence sentence) {
			Expression expression = sentence.getExpression(1,"");
			return expression.getAmount();
		}

		private String determineItemName(Sentence sentence) {
			Expression expression = sentence.getExpression(1,"");
			return expression.getNormalized();
		}

		private int determinePrice(Sentence sentence) {
			return sentence.getNumeral().getAmount();
		}
	}

	private class ConfirmPrepareOfferChatAction implements ChatAction {
		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			int fee = TradingUtility.calculateFee(player, price).intValue();
			if (TradingUtility.canPlayerAffordTradingFee(player, price)) {
				if (createOffer(player, item, price, quantity)) {
					TradingUtility.substractTradingFee(player, price);
					new AsynchronousProgramExecutor("trade", buildTweetMessage(item, quantity, price)).start();
					DBCommandQueue.get().enqueue(new LogTradeEventCommand(player, item, quantity, price));
					npc.say("我已为你把代理销售的东西上架到交易中心，手续费用为 "+ fee +" 金币.");
					npc.setCurrentState(ConversationStates.ATTENDING);
				} else {
					npc.say("你身上没有 " +  item.getName() + ".");
				}
				return;
			}
			npc.say("你不能付得起交易手续费 " + fee);
		}

		/**
		 * Try creating an offer.
		 *
		 * @param player the player who makes the offer
		 * @param item item for sale
		 * @param price price for the item
		 * @param number number of items to sell
		 * @return true if making the offer was successful, false otherwise
		 */
		private boolean createOffer(Player player, Item item, int price, int number) {
			Market shop = TradeCenterZoneConfigurator.getShopFromZone(player.getZone());
			if(shop != null) {
				Offer o = shop.createOffer(player, item, Integer.valueOf(price), Integer.valueOf(number));
				if (o == null) {
					return false;
				}

				StringBuilder message = new StringBuilder("代理销售你的 ");
				message.append(item.getName());
				message.append(" 的定价为 ");
				message.append(price);
				message.append(" 金币，已上架销售. ");
				String messageNumberOfOffers = "目前你上架代销的商品 "
					+  "offer"  + ".";
				player.sendPrivateText(message.toString() + messageNumberOfOffers);
				return true;
			}
			return false;
		}
	}
}
