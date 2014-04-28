package com.taobao.shuihan.rpc.protos.util;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;
import com.taobao.shuihan.rpc.protos.PersonProtos;
import com.taobao.shuihan.rpc.protos.PersonProtos.FullAddress;
import com.taobao.shuihan.rpc.protos.PersonProtos.Person;
import com.taobao.shuihan.rpc.protos.PersonProtos.Person.PersonStatus;
import com.taobao.shuihan.rpc.protos.PersonProtos.PersonInfo;
import com.taobao.shuihan.rpc.protos.PersonProtos.Phone;


public class MessageCorverter {

    public static com.taobao.rpc.benchmark.dataobject.Person toInterfacePerson(Person person) {
        com.taobao.rpc.benchmark.dataobject.Person p = new com.taobao.rpc.benchmark.dataobject.Person();
        p.setAttachment(person.getAttachment().toByteArray());
        p.setInfo(toInterfacePersonInfo(person.getInfo()));
        p.setLoginName(getOriginalString(person.getLoginName().trim()));
        p.setPersonId(getOriginalString(person.getPersonId().trim()));
        p.setStatus(toInterfacePersonStatus(person.getStatus()));
        return p;

    }


    public static Person toProtoPerson(com.taobao.rpc.benchmark.dataobject.Person person) {
        PersonProtos.Person.Builder p = PersonProtos.Person.newBuilder();
        if (ByteString.copyFrom(person.getAttachment()) != null) {
            p.setAttachment(ByteString.copyFrom(person.getAttachment()));
        }
        if (person.getInfo() != null) {
            p.setInfo(toProtoPersonInfo(person.getInfo()));
        }
        if (person.getLoginName() != null) {
            p.setLoginName(person.getLoginName());
        }
        if (person.getPersonId() != null) {
            p.setPersonId(person.getPersonId());
        }

        return p.setStatus(toProtoPersonStatus(person.getStatus())).build();

    }


    public static com.taobao.rpc.benchmark.dataobject.FullAddress toInterfaceFullAddress(FullAddress fullAddress) {
        com.taobao.rpc.benchmark.dataobject.FullAddress address = new com.taobao.rpc.benchmark.dataobject.FullAddress();
        address.setCityId(getOriginalString(fullAddress.getCityId().trim()));
        address.setCityName(getOriginalString(fullAddress.getCityName().trim()));
        address.setCountryId(getOriginalString(fullAddress.getCountryId().trim()));
        address.setCountryName(getOriginalString(fullAddress.getCountryName().trim()));
        address.setProvinceName(getOriginalString(fullAddress.getProvinceName().trim()));
        address.setStreetAddress(getOriginalString(fullAddress.getStreetAddress().trim()));
        address.setZipCode((fullAddress.getZipCode().trim()));
        return address;

    }


    public static FullAddress toProtoFullAddress(com.taobao.rpc.benchmark.dataobject.FullAddress fullAddress) {
        PersonProtos.FullAddress.Builder p = PersonProtos.FullAddress.newBuilder();
        if (fullAddress.getCityId() != null) {
            p.setCityId(fullAddress.getCityId());
        }
        if (fullAddress.getCityName() != null) {
            p.setCityName(fullAddress.getCityName());
        }
        if (fullAddress.getCountryId() != null) {
            p.setCountryId(fullAddress.getCountryId());
        }
        if (fullAddress.getCountryName() != null) {
            p.setCountryName(fullAddress.getCountryName());
        }
        if (fullAddress.getProvinceName() != null) {
            p.setProvinceName(fullAddress.getProvinceName());
        }
        if (fullAddress.getStreetAddress() != null) {
            p.setStreetAddress(fullAddress.getStreetAddress());
        }
        if (fullAddress.getZipCode() != null) {
            p.setZipCode(fullAddress.getZipCode());
        }
        return p.build();

    }


    public static com.taobao.rpc.benchmark.dataobject.PersonInfo toInterfacePersonInfo(PersonInfo personInfo) {
        com.taobao.rpc.benchmark.dataobject.PersonInfo p = new com.taobao.rpc.benchmark.dataobject.PersonInfo();
        p.setDepartment(getOriginalString(personInfo.getDepartment().trim()));
        p.setFax(toInterfacePhone(personInfo.getFax()));
        p.setFemale(personInfo.getFemale());
        p.setFullAddress(toInterfaceFullAddress(personInfo.getFullAddress()));
        p.setHomepageUrl(getOriginalString(personInfo.getHomepageUrl().trim()));
        p.setJobTitle(getOriginalString(personInfo.getJobTitle().trim()));
        p.setMale(personInfo.getMale());
        p.setMobileNo(getOriginalString(personInfo.getMobileNo().trim()));
        p.setName(getOriginalString(personInfo.getName().trim()));
        List<com.taobao.rpc.benchmark.dataobject.Phone> phones =
                new ArrayList<com.taobao.rpc.benchmark.dataobject.Phone>();
        for (Phone phone : personInfo.getPhonesList()) {
            phones.add(toInterfacePhone(phone));
        }
        p.setPhones(phones);
        return p;
    }


    public static PersonInfo toProtoPersonInfo(com.taobao.rpc.benchmark.dataobject.PersonInfo personInfo) {
        PersonInfo.Builder info = PersonProtos.PersonInfo.newBuilder();
        if (personInfo.getDepartment() != null) {
            info.setDepartment(personInfo.getDepartment().trim());
        }
        info.setFax(toProtoPhone(personInfo.getFax()));
        info.setFemale(personInfo.isFemale());
        info.setFullAddress(toProtoFullAddress(personInfo.getFullAddress()));
        if (personInfo.getHomepageUrl() != null) {
            info.setHomepageUrl(personInfo.getHomepageUrl().trim());
        }
        if (personInfo.getJobTitle() != null) {
            info.setJobTitle(personInfo.getJobTitle().trim());
        }
        info.setMale(personInfo.isMale());
        if (personInfo.getMobileNo() != null) {
            info.setMobileNo(personInfo.getMobileNo().trim());
        }
        if (personInfo.getName() != null) {
            info.setName(personInfo.getName().trim());
        }
        for (com.taobao.rpc.benchmark.dataobject.Phone phone : personInfo.getPhones()) {
            info.addPhones(toProtoPhone(phone));
        }
        return info.build();
    }


    public static com.taobao.rpc.benchmark.dataobject.PersonStatus toInterfacePersonStatus(PersonStatus personStatus) {

        switch (personStatus) {
        case DISABLED:
            return com.taobao.rpc.benchmark.dataobject.PersonStatus.DISABLED;
        case ENABLED:
            return com.taobao.rpc.benchmark.dataobject.PersonStatus.ENABLED;
        default:
            return null;
        }

    }


    public static PersonStatus toProtoPersonStatus(com.taobao.rpc.benchmark.dataobject.PersonStatus personStatus) {
        switch (personStatus) {
        case DISABLED:
            return PersonProtos.Person.PersonStatus.DISABLED;
        case ENABLED:
            return PersonProtos.Person.PersonStatus.ENABLED;
        default:
            return null;

        }
    }


    public static com.taobao.rpc.benchmark.dataobject.Phone toInterfacePhone(Phone phone) {
        com.taobao.rpc.benchmark.dataobject.Phone p = new com.taobao.rpc.benchmark.dataobject.Phone();
        p.setArea(getOriginalString(phone.getArea().trim()));
        p.setCountry(getOriginalString(phone.getCountry().trim()));
        p.setExtensionNumber(getOriginalString(phone.getExtensionNumber().trim()));
        p.setNumber(getOriginalString(phone.getNumber().trim()));
        return p;

    }


    public static Phone toProtoPhone(com.taobao.rpc.benchmark.dataobject.Phone phone) {
        Phone.Builder p = PersonProtos.Phone.newBuilder();
        if (phone.getArea() != null) {
            p.setArea(phone.getArea());
        }
        if (phone.getCountry() != null) {
            p.setCountry(phone.getCountry());
        }
        if (phone.getExtensionNumber() != null) {
            p.setExtensionNumber(phone.getExtensionNumber());
        }
        if (phone.getNumber() != null) {
            p.setNumber(phone.getNumber());
        }
        return p.build();
    }


    public static String getOriginalString(String protoString) {
        if (protoString == null || protoString.length() == 0) {
            return null;
        }

        return protoString;
    }

}
