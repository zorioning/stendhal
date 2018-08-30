/**
 *
 */
package games.stendhal.server.maps.semos.apothecary_lab;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author AntumDeluge
 *
 */
public class ApothecaryNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
	    final SpeakerNPC npc = new SpeakerNPC("Jameson") {

	        @Override
			protected void createPath() {
	            List<Node> nodes=new LinkedList<Node>();
	            nodes.add(new Node(7,9));
	            nodes.add(new Node(16,9));
	            nodes.add(new Node(16,12));
	            nodes.add(new Node(19,12));
	            nodes.add(new Node(19,16));
	            nodes.add(new Node(19,12));
	            nodes.add(new Node(15,12));
	            nodes.add(new Node(15,17));
	            nodes.add(new Node(15,12));
	            nodes.add(new Node(7,12));
				setPath(new FixedPath(nodes, true));
	        }

	        @Override
			protected void createDialog() {
	            addGreeting("Hello, 欢迎来到我的试验室.");
	            addJob("我以前是个药济师 #apothecary ，但现在我退休了");
	            addHelp("抱歉，但我不认为我能帮到你什么.");
	            addOffer("我什么都不卖.");
	            addReply("克拉丝", "Oh 是的，我的老朋友，我以前常去 #Athor 旅行，去采集很稀有的 #kokuda 草药，所以我对 克拉丝 非常熟悉.");
	            addReply("kokuda", "是一种草药，只在 #Athor 岛的迷宫内生长.");
	            addReply("Athor", "你还没去过 Athor? 那是个很漂亮的岛，值得去的一个好地方。但要远离食人族领地. 如果他们请你吃饭，那你可能就再回不了家了.");
	            addReply("Apothecary", "我听说有一队研究人员为 Faimouni 的一个有很能力的领队工作。然而，这个领队被收买腐蚀，我用我的技术制作了致命的战争武器，还好，我逃离了那里并在这里躲到现在。");
	            addGoodbye("请不要把我的试验室的秘密告诉外人.");
	        }
	    };

	    // The NPC sprite from data/sprites/npc/
	    npc.setEntityClass("apothecarynpc");
	    // set a description for when a player does 'Look'
	    npc.setDescription("你见到了 Jameson, 他仍在有序工作着.");
	    // Set the initial position to be the first node on the Path you defined above.
	    npc.setPosition(7, 9);
	    npc.initHP(100);

	    zone.add(npc);
	}
}
