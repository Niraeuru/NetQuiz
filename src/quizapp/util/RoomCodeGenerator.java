package quizapp.util;

import java.util.Random;

/**
 * Utility class for generating unique room codes.
 */
public class RoomCodeGenerator {
    
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    
    public static String generateRoomCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        
        return sb.toString();
    }
}