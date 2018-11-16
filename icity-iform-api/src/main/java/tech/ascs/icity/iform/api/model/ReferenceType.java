package tech.ascs.icity.iform.api.model;

public enum ReferenceType {
    OneToOne("OneToOne"),
    OneToMany("OneToMany"),
    ManyToOne("ManyToOne"),
    ManyToMany("ManyToMany");
    private String value;

    private ReferenceType(String value){
        this.value = value;
    }
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static ReferenceType getReverseReferenceType(ReferenceType referenceType) {
        if(referenceType == ReferenceType.ManyToOne){
            return ReferenceType.OneToMany;
        }else  if(referenceType == ReferenceType.OneToMany){
            return ReferenceType.ManyToOne;
        }
        return referenceType;
    }
}
