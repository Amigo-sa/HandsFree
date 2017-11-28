package by.citech.contact;

public enum ContactState {

    Null {
        public String getMessage() {return "Default state";}
        public String getName() {return "Null";}
    },

    SuccessAdd {
        public String getMessage() {return "Contact add success";}
        public String getName() {return "SuccessAdd";}
    },

    SuccessDelete {
        public String getMessage() {return "Contact remove success";}
        public String getName() {return "SuccessDelete";}
    },

    SuccessUpdate {
        public String getMessage() {return "Contact update success";}
        public String getName() {return "SuccessUpdate";}
    },

    FailInvalidContact {
        public String getMessage() {return "Invalid contact fields";}
        public String getName() {return "FailInvalidContact";}
    },

    FailNotUniqueContact {
        public String getMessage() {return "Contact already exists";}
        public String getName() {return "FailNotUniqueContact";}
    },

    FailToAdd {
        public String getMessage() {return "Fail to add contact to database";}
        public String getName() {return "FailToAdd";}
    };

    public abstract String getMessage();
    public abstract String getName();

}
