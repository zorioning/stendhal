package games.stendhal.server.maps.nalwor.postoffice;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SellerBehaviour;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.ZoneConfigurator;
import games.stendhal.server.pathfinder.FixedPath;
import games.stendhal.server.pathfinder.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the post office elf NPC.
 * She may be used later for something else like a newspaper. 
 * Now she sells nalwor scrolls
 * @author kymara
 */
public class IL0_PostNPC implements ZoneConfigurator {

	private NPCList npcs = NPCList.get();

	private ShopList shops = ShopList.get();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		SpeakerNPC npc = new SpeakerNPC("Lorithien") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(11, 3));
				nodes.add(new Node(16, 3));
				nodes.add(new Node(16, 8));
				nodes.add(new Node(11, 8));
				nodes.add(new Node(11, 5));
				nodes.add(new Node(7, 5));
				nodes.add(new Node(7, 2));
				nodes.add(new Node(3, 2));
				nodes.add(new Node(3, 5));
				nodes.add(new Node(3, 2));
				nodes.add(new Node(7, 2));
				nodes.add(new Node(7, 5));
				nodes.add(new Node(11, 5));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Hi, can I #help you?");
				addJob("I work in this post office. But I'm new and I haven't been trusted with much yet.");
				addHelp("I've not had this #job long ... come back soon and I might have been given something interesting to do.");
				addSeller(new SellerBehaviour(shops.get("nalworscrolls")));
				addGoodbye("Bye - nice to meet you!");
			}
		};
		npc.setDescription("You see a pretty elf girl.");
		npcs.add(npc);
		zone.assignRPObjectID(npc);
		npc.setEntityClass("postelfnpc");
		npc.setPosition(11, 3);
		npc.initHP(100);
		zone.add(npc);
	}
}
