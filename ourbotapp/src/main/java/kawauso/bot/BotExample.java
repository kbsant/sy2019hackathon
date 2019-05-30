package kawauso.bot;
import authentication.ISymAuth;
import authentication.SymBotAuth;
import authentication.SymBotRSAAuth;
import clients.SymBotClient;
import configuration.SymConfig;
import configuration.SymConfigLoader;
import listeners.IMListener;
import listeners.RoomListener;
import model.*;
import org.apache.log4j.BasicConfigurator;
import services.DatafeedEventsService;
import javax.ws.rs.core.NoContentException;

import java.net.URL;
import java.util.List;

public class BotExample {

    public static void main(String [] args) {
        BotExample app = new BotExample();
    }


    public BotExample() {
        BasicConfigurator.configure();

        String configPath = System.getenv("SY_CONFIG");
        //URL url = getClass().getResource("config.json");
        //URL url = new URL("file://./config.json");
        SymConfigLoader configLoader = new SymConfigLoader();
        //SymConfig config = configLoader.loadFromFile(url.getPath());
        SymConfig config = configLoader.loadFromFile(configPath);
        ISymAuth botAuth = new SymBotRSAAuth(config);
        botAuth.authenticate();
        SymBotClient botClient = SymBotClient.initBot(config, botAuth);
        DatafeedEventsService datafeedEventsService = botClient.getDatafeedEventsService();
        RoomListener roomListenerTest = new RoomListenerTestImpl(botClient);
        datafeedEventsService.addRoomListener(roomListenerTest);
        IMListener imListener = new IMListenerImpl(botClient);
        datafeedEventsService.addIMListener(imListener);
        createRoom(botClient);

    }

    private void createRoom(SymBotClient botClient){



        try {

            String email = System.getenv("SY_EMAIL");
            UserInfo userInfo = botClient.getUsersClient().getUserFromEmail(email, true);
            //get user IM and send message
            String IMStreamId = botClient.getStreamsClient().getUserIMStreamId(userInfo.getId());
            OutboundMessage message = new OutboundMessage();
            message.setMessage("test IM");
            botClient.getMessagesClient().sendMessage(IMStreamId,message);

            Room room = new Room();
            room.setName("bot256 kawauso test room preview");
            room.setDescription("test");
            room.setDiscoverable(true);
            room.setPublic(true);
            room.setViewHistory(true);
            RoomInfo roomInfo = null;
            roomInfo = botClient.getStreamsClient().createRoom(room);
            botClient.getStreamsClient().addMemberToRoom(roomInfo.getRoomSystemInfo().getId(),userInfo.getId());

            Room newRoomInfo = new Room();
            newRoomInfo.setName("test generator");
            botClient.getStreamsClient().updateRoom(roomInfo.getRoomSystemInfo().getId(),newRoomInfo);

            List<RoomMember> members =  botClient.getStreamsClient().getRoomMembers(roomInfo.getRoomSystemInfo().getId());

            botClient.getStreamsClient().promoteUserToOwner(roomInfo.getRoomSystemInfo().getId(), userInfo.getId());

            botClient.getStreamsClient().deactivateRoom(roomInfo.getRoomSystemInfo().getId());


        } catch (NoContentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
