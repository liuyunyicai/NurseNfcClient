package hust.nursenfcclient.patient;

/**
 * Created by admin on 2015/11/23.
 */
public class PatientInfoItem {
    private String bedId;
    private String bedState;
    private String houseId;
    private String patientId;
    private String patientName;
    private int patientAge;
    private boolean patientGender;
    private String patientRecord;
    private float lastTemper;
    private String patientPhoto;
    private String tag_id;
    private boolean isChecked;

    public static final String BED_CHECKED_STATE = "已测量";
    public static final String BED_UNCHECK_STATE = "未测量";
    public static final String BED_EMPTY_STATE   = "空床位";

    private static final String CHECKED = "checked";
    private static final String UNCHECK = "uncheck";
    private static final String EMPTY = "empty";

    public static final String GENDER_MALE = "male";
    public static final String GENDER_FEMALE = "female";

    public PatientInfoItem(String bedId, String bedState, String patientId, String patientName,
                           int patientAge, boolean patientGender, String patientRecord,
                           float lastTemper, String patientPhoto, String tag_id) {
        this.bedId = bedId;
        this.bedState = bedState;
        this.patientId = patientId;

        this.patientName = patientName;

        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.patientRecord = patientRecord;
        this.lastTemper = lastTemper;
        this.patientPhoto = patientPhoto;
        this.tag_id = tag_id;
    }

    public PatientInfoItem() {}

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }

    public String getBedId() {
        return bedId;
    }

    public void setBedId(String bedId) {
        this.bedId = bedId;
    }

    public String getBedState() {
        return bedState;
    }

    public void setBedState(String bedState) {
        switch (bedState) {
            case CHECKED:
                this.bedState = BED_CHECKED_STATE;
                isChecked =true;
                break;
            case UNCHECK:
                this.bedState = BED_UNCHECK_STATE;
                break;
            case EMPTY:
                this.bedState = BED_UNCHECK_STATE;
                break;
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(int patientAge) {
        this.patientAge = patientAge;
    }

    public boolean getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGenderIsMale) {
        if (patientGenderIsMale.equals(GENDER_MALE))
            patientGender = true;
        if (patientGenderIsMale.equals(GENDER_FEMALE))
            patientGender = false;
    }

    public String getPatientRecord() {
        return patientRecord;
    }

    public void setPatientRecord(String patientRecord) {
        this.patientRecord = patientRecord;
    }

    public float getLastTemper() {
        return lastTemper;
    }

    public void setLastTemper(float lastTemper) {
        this.lastTemper = lastTemper;
    }

    public String getPatientPhoto() {
        return patientPhoto;
    }

    public void setPatientPhoto(String patientPhoto) {
        this.patientPhoto = patientPhoto;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }
}
