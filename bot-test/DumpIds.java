import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.GameProtocols;

public class DumpIds {
    public static void main(String[] a) {
        net.minecraft.server.Bootstrap.bootStrap();
        System.out.println("== config serverbound ==");
        ConfigurationProtocols.SERVERBOUND_TEMPLATE.details().listPackets((t, i) ->
            System.out.printf("sb cfg %02x %s%n", i, t.id()));
        System.out.println("== config clientbound ==");
        ConfigurationProtocols.CLIENTBOUND_TEMPLATE.details().listPackets((t, i) ->
            System.out.printf("cb cfg %02x %s%n", i, t.id()));
        System.out.println("== play serverbound (selected) ==");
        GameProtocols.SERVERBOUND_TEMPLATE.details().listPackets((t, i) -> {
            String id = t.id().toString();
            if (id.contains("chat") || id.contains("keep_alive") || id.contains("position")
                    || id.contains("configuration"))
                System.out.printf("sb play %02x %s%n", i, id);
        });
        System.out.println("== play clientbound (selected) ==");
        GameProtocols.CLIENTBOUND_TEMPLATE.details().listPackets((t, i) -> {
            String id = t.id().toString();
            if (id.contains("system_chat") || id.contains("keep_alive") || id.contains("disconnect")
                    || id.contains("login") || id.contains("player_position"))
                System.out.printf("cb play %02x %s%n", i, id);
        });
    }
}
