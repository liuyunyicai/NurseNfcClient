package hust.nursenfcclient.network;

public interface ServicesHelper {

	// ============ 一次性上传下载  ========= //
	int NURSE_DOWNLOAD = 2000; // 一次性下载功能
	int NURSE_UPLOAD   = 2001; // 一次性上传功能

	int NURSE_DOWNLOAD_ACK = 2100;
	int NURSE_UPLOAD_ACK   = 2101;

	// ======= 图片传输进行单独处理 ====== //
	int GET_IMAGE = 1200;
	int GET_IMAGE_ACK = 1300;

	int ACK_INTERAL = 100;

	// ====== 特殊标志  =======//
	int ACK_FAILURE   = -100;   // 表示处理失败
	int ACK_NOT_FOUND = -101;   // 表示数据库查找成功，但未发现相关记录
	int ACK_SUCCESS   = -102;   // 表示数据库查找成功，且获得相应数据

	// ======== 客户端发送状态表示 ====== //
	int LOG_BEFORE    = 100;
	int QUERY_HOUSE   = 101;
	int QUERY_BED     = 102;
	int QUERY_PATIENT = 103;
	int QUERY_TEMPER  = 104;
	int TEMPER_PAIR   = 105;
	int PATIENT_EXIT  = 106;
	int NURSE_EXIT    = 107;


	// ======== 服务器返回状态标志 ====== //
	int LOG_BEFORE_ACK    = 200;
	int QUERY_HOUSE_ACK   = 201;
	int QUERY_BED_ACK     = 202;
	int QUERY_PATIENT_ACK = 203;
	int QUERY_TEMPER_ACK  = 204;
	int TEMPER_PAIR_ACK   = 205;
	int PATIENT_EXIT_ACK  = 206;
	int NURSE_EXIT_ACK    = 207;

	// ======= 用于测试  ======== //
	int TEST     = 1000;
	int TEST_ACK = 1100;

	String IMAGE_PATH = "image_path"; // 数据库中图片的位置

	// ====== 数据库中对应的字段号 ======= //
	String NURSE_INFO_TABLE_NAME = "nurse_info";
	String NURSE_ID    = "nurse_id";
	String NURSE_NAME  = "nurse_name";
	String NURSE_PHOTO = "nurse_photo";

	String HOUSE_INFO_TABLE_NAME = "house_info";
	String HOUSE_ID    = "house_id";
	String HOUSE_STATE = "house_state";

	String BED_INFO_TABLE_NAME = "bed_info";
	String BED_ID    = "bed_id";
	String BED_STATE = "bed_state";

	String PATIENT_INFO_TABLE_NAME = "patient_info";
	String PATIENT_ID     = "patient_id";
	String PATIENT_NAME   = "patient_name";
	String PATIENT_AGE    = "patient_age";
	String PATIENT_GENDER = "patient_gender";
	String PATIENT_RECORD = "patient_record";
	String PATIENT_PHOTO  = "patient_photo";

	String TEMPER_INFO_TABLE_NAME = "temperature_info";
	String ID         = "id";
	String TAG_ID     = "tag_id";
	String TEMPER_NUM = "temper_num";
	String LAST_TIME  = "last_time";
	String NEXT_TIME  = "next_time";

	// ====== 数据库中对应数据类型 ====== //
	String TYPE_STRING  = "String";
	String TYPE_INTEGER = "Int";
	String TYPE_FLOAT   = "Float";
	String TYPE_VARCHAR = "varchar";
	String TYPE_ENUM    = "enum";
	String TYPE_TEXT    = "text";
	String TYPE_TIME    = "timestamp";

	// 时间格式
	String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";



}
