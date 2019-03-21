public class EntryTemplate {
  String tableName = "c_ingress.kv_store";
  String actionName = "c_ingress.reply_to_read";
  String matchField = "hdr.data.key1";
  enum matchType
  {
    EXACT, TERNARY, LPM
  }
  byte[] MASK = new byte[] { (byte)0xff, (byte)0xff, (byte)0xff,
  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
  (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff };

  byte[] key = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00,
  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x12 };
  
  Map<String,byte[]> actionParam = new HashMap<String,byte[]>();
  // actionParam.put("value",new byte);

  int timeout = 65535;
  int FLOW_RULE_PRIORITY = 100;
  boolean isPermenant = true;

}
