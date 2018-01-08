package by.citech.handsfree.contact;

public enum EContactState {

    Null {
        public String getMessage() {return "Default state";}
    },

    SuccessAdd {
        public String getMessage() {return "Contact add success";}
    },

    SuccessDelete {
        public String getMessage() {return "Contact remove success";}
    },

    SuccessUpdate {
        public String getMessage() {return "Contact update success";}
    },

    FailDelete {
        public String getMessage() {return "Failed to delete contact";}
    },

    FailInvalid {
        public String getMessage() {return "Invalid contact fields";}
    },

    FailNotUnique {
        public String getMessage() {return "Contact already exists";}
    },

    FailToAdd {
        public String getMessage() {return "Fail to add contact to database";}
    },

    FailUpdate {
        public String getMessage() {return "Fail to update contact";}
    };

    public abstract String getMessage();

    public String getName() {
        return this.toString();
    }

}
