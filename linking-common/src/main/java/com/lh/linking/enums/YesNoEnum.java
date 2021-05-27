package com.lh.linking.enums;

public enum YesNoEnum {

    YES(1), // yes
    NO(0); // no

    private Integer value;

    YesNoEnum(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    /**
     * 获取元素
     *
     * @param value
     * @return
     */
    public static YesNoEnum getEnum(Integer value) {
        YesNoEnum[] values = YesNoEnum.values();
        for (YesNoEnum em : values) {
            if (em.getValue().equals(value)) {
                return em;
            }
        }
        return null;
    }

    /**
     * is yes
     *
     * @param val
     * @return
     */
    public static boolean isYes(Integer val) {
        return YES.getValue().equals(val);
    }
}
