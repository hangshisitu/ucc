package com.ucc.addrbook;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.jasig.cas.client.validation.Assertion;
import org.ldaptive.io.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by Administrator on 2016/2/24.
 */

public class ToolUntil {

    public static boolean CheckRole(String role, String corpid,Assertion ass)
    {
        if(ass==null || ass.getPrincipal()==null || ass.getPrincipal().getAttributes()==null)
        {
            return false;
//            return true;
        }


        Map attrs = ass.getPrincipal().getAttributes();

        if(attrs.get("role").equals("root"))
        {
            return true;
        }
        else if(attrs.get("role").equals("admin") &&
                attrs.get("corpId").equals(corpid) && !role.equals("root"))
        {
            return true;
        }
        else if(attrs.get("corpId").equals(corpid) &&
                role.equals("normal"))
        {
            return true;
        }
        return false;
    }

//    public static void main(String[] arg)
//    {
//        String tmp = getPingYin("人参参abc加001");
//        System.out.println(tmp);
//    }

    // 将汉字转换为全拼
    public static String getPingYin(String src) {

        char[] t1 = null;
        t1 = src.toCharArray();
        String[] t2 = new String[t1.length];
        HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();

        t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        t3.setVCharType(HanyuPinyinVCharType.WITH_V);
        String t4 = "";
        int t0 = t1.length;
        try {
            for (int i = 0; i < t0; i++) {
                // 判断是否为汉字字符
                if (java.lang.Character.toString(t1[i]).matches(
                        "[\\u4E00-\\u9FA5]+")) {
                    t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
                    t4 += t2[0];
                } else
                    t4 += java.lang.Character.toString(t1[i]);
            }
            // System.out.println(t4);
            return t4;
        } catch (BadHanyuPinyinOutputFormatCombination e1) {
            e1.printStackTrace();
        }
        return t4;
    }

    public static String dn2Fullname(String dn,boolean hasdomain)
    {
        StringBuilder sb =new StringBuilder();
        String item[] = dn.split(",");
        for (int i=item.length-1;i>=0;i--)
        {
            if(item[i].startsWith("o") && hasdomain==true )
            {
                sb.append(String.format("%s",item[i].substring(2)));
            }else if(item[i].startsWith("cn"))
            {
                sb.append(String.format("/%s",item[i].substring(3)));
            }
        }
        String fullname = sb.toString();
        if(fullname.startsWith("/"))
        {
            fullname = fullname.substring(1);
        }
        return fullname;
    }


    /**
     * 从部门dn中截取根部门dn
     * @param gdn
     * @return
     */
    public static String dn2Rootdn(String gdn)
    {
        String dns[] = gdn.split(",");
        StringBuilder sb = new StringBuilder();
        int ou=0;
        for (; ou<dns.length;ou++)
        {
            if(dns[ou].equals("ou=groups"))
            {
                ou--;
                break;
            }
        }
        for(;ou<dns.length;ou++)
        {
            sb.append(dns[ou]);
            sb.append(",");
        }
        String rootdn = sb.toString();
        if(rootdn.endsWith(","))
        {
            rootdn = rootdn.substring(0,rootdn.length()-1);
        }
        return rootdn;
    }

    /**
     * 部门全名转部门dn
     * @param fullname 部门全名
     * @param corpdn   部门所在企业dn
     * @return
     */
    public static String fullName2dn(String fullname,String corpdn)
    {
        String names[] = fullname.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = names.length-1;i>=0;i--)
        {
            sb.append(String.format("cn=%s,",names[i]));
        }
        sb.append(String.format("ou=groups,%s",corpdn));
        return sb.toString();
    }

    /**
     * 从dn中获取企业dn
     * @param dn
     * @return
     */
    public static String dn2Corpdn(String dn)
    {
        String corpdn="";
        if(dn!=null && !dn.isEmpty())
        {
            String items[] = dn.split(",");
            int length = items.length;
            if(length>=3)
            {
                StringBuilder sb = new StringBuilder();
                for (int i=length-3; i<length;i++)
                {
                    sb.append(items[i]);
                    sb.append(",");
                }
                corpdn = sb.toString();
                corpdn = corpdn.substring(0,corpdn.length()-1);
            }
        }
        return corpdn;
    }


    /**
     * 从dn获取父dn
     * @param dn
     * @return
     */
    public static String dn2Parentdn(String dn)
    {
        String parentdn="";
        if(dn!=null && !dn.isEmpty())
        {
            return dn.substring(dn.indexOf(",")+1);

        }
        return parentdn;
    }
    /**
     * 将两个ASCII字符合成一个字节；
     * 如："EF"--> 0xEF
     * @param src0 byte
     * @param src1 byte
     * @return byte
     */
    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte)(_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte)(_b0 ^ _b1);
        return ret;
    }

    /**
     * 将指定字符串src，以每两个字符分割转换为16进制形式
     * 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF, 0xD9}
     * @param src String
     * @return byte[]
     */
    public static byte[] HexString2Bytes(String src){
        byte[] ret = new byte[16];
        byte[] tmp = src.getBytes();
        for(int i=0; i<16; i++){
            ret[i] = uniteBytes(tmp[i*2], tmp[i*2+1]);
        }
        return ret;
    }


    /**
     *
     * @param strmd5
     * @return
     */
    public static String EncodePwd(String strmd5)
    {
        byte pd[] = HexString2Bytes(strmd5);
        byte pwd[] = Base64.encodeToByte(pd,false);

        StringBuilder sb = new StringBuilder();
        for(byte e:pwd)
        {
            sb.append(String.format("%c",e));
        }

        return "{md5}"+sb.toString();
    }

    public static boolean verifyMD5(String ldappw,String inputpw)
    {
        // MessageDigest 提供了消息摘要算法，如 MD5 或 SHA，的功能，这里LDAP使用的是SHA-1
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 取出加密字符
            if (ldappw.startsWith("{md5}")) {
                ldappw = ldappw.substring(5);
            }
            // 解码BASE64
            byte[] ldappwbyte = Base64.decode(ldappw);

            // 返回校验结果
            return MessageDigest.isEqual(ldappwbyte, ToolUntil.HexString2Bytes(inputpw));

        }catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return false;
    }
//    /**
//     * 去除多音字重复数据
//     *
//     * @param theStr
//     * @return
//     */
//    private static List<Map<String, Integer>> discountTheChinese(String theStr) {
//        // 去除重复拼音后的拼音列表
//        List<Map<String, Integer>> mapList = new ArrayList<Map<String, Integer>>();
//        // 用于处理每个字的多音字，去掉重复
//        Map<String, Integer> onlyOne = null;
//        String[] firsts = theStr.split(" ");
//        // 读出每个汉字的拼音
//        for (String str : firsts) {
//            onlyOne = new Hashtable<String, Integer>();
//            String[] china = str.split(",");
//            // 多音字处理
//            for (String s : china) {
//                Integer count = onlyOne.get(s);
//                if (count == null) {
//                    onlyOne.put(s, new Integer(1));
//                } else {
//                    onlyOne.remove(s);
//                    count++;
//                    onlyOne.put(s, count);
//                }
//            }
//            mapList.add(onlyOne);
//        }
//        return mapList;
//    }
//
//    /**
//     * 解析并组合拼音，对象合并方案(推荐使用)
//     *
//     * @return
//     */
//    private static String parseTheChineseByObject(
//            List<Map<String, Integer>> list) {
//        Map<String, Integer> first = null; // 用于统计每一次,集合组合数据
//        // 遍历每一组集合
//        for (int i = 0; i < list.size(); i++) {
//            // 每一组集合与上一次组合的Map
//            Map<String, Integer> temp = new Hashtable<String, Integer>();
//            // 第一次循环，first为空
//            if (first != null) {
//                // 取出上次组合与此次集合的字符，并保存
//                for (String s : first.keySet()) {
//                    for (String s1 : list.get(i).keySet()) {
//                        String str = s + s1;
//                        temp.put(str, 1);
//                    }
//                }
//                // 清理上一次组合数据
//                if (temp != null && temp.size() > 0) {
//                    first.clear();
//                }
//            } else {
//                for (String s : list.get(i).keySet()) {
//                    String str = s;
//                    temp.put(str, 1);
//                }
//            }
//            // 保存组合数据以便下次循环使用
//            if (temp != null && temp.size() > 0) {
//                first = temp;
//            }
//        }
//        String returnStr = "";
//        if (first != null) {
//            // 遍历取出组合字符串
//            for (String str : first.keySet()) {
//                returnStr += (str + ",");
//            }
//        }
//        if (returnStr.length() > 0) {
//            returnStr = returnStr.substring(0, returnStr.length() - 1);
//        }
//        return returnStr;
//    }
//
//    /**
//     * 汉字转换位汉语全拼，英文字符不变，特殊字符丢失
//     * 支持多音字，生成方式如（重当参:zhongdangcen,zhongdangcan,chongdangcen
//     * ,chongdangshen,zhongdangshen,chongdangcan）
//     *
//     * @param chines
//     *            汉字
//     * @return 拼音
//     */
//    public static String converterToSpell(String chines) {
//        StringBuffer pinyinName = new StringBuffer();
//        char[] nameChar = chines.toCharArray();
//        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
//        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
//        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//        for (int i = 0; i < nameChar.length; i++) {
//            if (nameChar[i] > 128) {
//                try {
//                    // 取得当前汉字的所有全拼
//                    String[] strs = PinyinHelper.toHanyuPinyinStringArray(
//                            nameChar[i], defaultFormat);
//                    if (strs != null) {
//                        for (int j = 0; j < strs.length; j++) {
//                            pinyinName.append(strs[j]);
//                            if (j != strs.length - 1) {
//                                pinyinName.append(",");
//                            }
//                        }
//                    }
//                } catch (BadHanyuPinyinOutputFormatCombination e) {
//                    e.printStackTrace();
//                }
//            } else {
//                pinyinName.append(nameChar[i]);
//            }
//            pinyinName.append(" ");
//        }
//        // return pinyinName.toString();
//        return parseTheChineseByObject(discountTheChinese(pinyinName.toString()));
//    }

//    /**
//     * 生成32位编码
//     * @return string
//     */
//    public static String getUUID(){
//        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
//        return uuid;
//    }
//
//    /**
//     * 自定义规则生成32位编码
//     * @return string
//     */
//    public static String getUUIDByRules(String rules)
//    {
//        int rpoint = 0;
//        StringBuffer generateRandStr = new StringBuffer();
//        Random rand = new Random();
//        int length = 32;
//        for(int i=0;i<length;i++)
//        {
//            if(rules!=null){
//                rpoint = rules.length();
//                int randNum = rand.nextInt(rpoint);
//                generateRandStr.append(radStr.substring(randNum,randNum+1));
//            }
//        }
//        return generateRandStr+"";
//    }

}
