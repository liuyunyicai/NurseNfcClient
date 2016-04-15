package hust.nursenfcclient.nfctag;

import android.content.Intent;
import android.nfc.NfcAdapter;

/**
 * Created by admin on 2015/12/8.
 */
public class NFCUidCoverter {
    // Hex help
    private final static byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1',
            (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
            (byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
            (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F' };

    public static String getHexString(byte[] raw, int len) {
        byte[] hex = new byte[2 * len];
        int index = hex.length - 1;
        int pos = 0;

        for (byte b : raw) {
            if (pos >= len)
                break;

            pos++;
            int v = b & 0xFF;
            hex[index--] = HEX_CHAR_TABLE[v & 0xF];
            hex[index--] = HEX_CHAR_TABLE[v >>> 4];
        }

        return new String(hex);
    }
    public static String getUid(Intent intent){
        byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        return getHexString(myNFCID, myNFCID.length);
    }
}
