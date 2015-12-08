package com.android.googlesheetsdemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by OPTIMUSDOM ubuntu151 on 6/11/15.
 */
public class EmployeeModel implements Parcelable {

    public int EmpCode;
    public String Name;
    public String MailId;
    // for checkbox state
    public boolean lunchCheck;
    // for counting total users of the day

    public int getEmpCode() {
        return EmpCode;
    }

    public void setEmpCode(int empCode) {
        EmpCode = empCode;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getMailId() {
        return MailId;
    }

    public void setMailId(String mailId) {
        MailId = mailId;
    }

    public boolean isLunchCheck() {
        return lunchCheck;
    }

    public void setLunchCheck(boolean lunchCheck) {
        this.lunchCheck = lunchCheck;
    }

    EmployeeModel() {
    }

    protected EmployeeModel(Parcel in) {
        EmpCode = in.readInt();
        Name = in.readString();
        MailId = in.readString();
        lunchCheck = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(EmpCode);
        dest.writeString(Name);
        dest.writeString(MailId);
        dest.writeByte((byte) (lunchCheck ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<EmployeeModel> CREATOR = new Parcelable.Creator<EmployeeModel>() {
        @Override
        public EmployeeModel createFromParcel(Parcel in) {
            return new EmployeeModel(in);
        }

        @Override
        public EmployeeModel[] newArray(int size) {
            return new EmployeeModel[size];
        }
    };
}