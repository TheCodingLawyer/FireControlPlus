package me.confuser.banmanager.ultixreports.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * UUID utility class for BanManager integration
 * BanManager stores UUIDs as byte arrays, so we need conversion utilities
 * 
 * @author confuser
 */
public class UUIDUtils {
    
    /**
     * Convert UUID to byte array (for BanManager storage)
     */
    public static byte[] toBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
    
    /**
     * Convert byte array to UUID (from BanManager storage)
     */
    public static UUID fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new IllegalArgumentException("Byte array must be exactly 16 bytes");
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long mostSigBits = bb.getLong();
        long leastSigBits = bb.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }
    
    /**
     * Parse UUID from string, handling various formats
     */
    public static UUID fromString(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Handle UUID with or without dashes
            String cleanUuid = uuidString.trim().replace("-", "");
            if (cleanUuid.length() != 32) {
                return null;
            }
            
            // Insert dashes in the correct positions
            String formatted = cleanUuid.substring(0, 8) + "-" +
                              cleanUuid.substring(8, 12) + "-" +
                              cleanUuid.substring(12, 16) + "-" +
                              cleanUuid.substring(16, 20) + "-" +
                              cleanUuid.substring(20, 32);
            
            return UUID.fromString(formatted);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if a string is a valid UUID
     */
    public static boolean isValidUUID(String uuidString) {
        return fromString(uuidString) != null;
    }
} 