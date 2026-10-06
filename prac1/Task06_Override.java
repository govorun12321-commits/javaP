package prac1;

class ParentSender {
    protected Notification createNotification() {
        return new Notification("parent");
    }

    public Object label() {
        return "parent";
    }

    public void send(Notification n) {
        System.out.println("Parent: " + n.text);
    }
}

class CorrectChildSender extends ParentSender {
    @Override
    protected Notification createNotification() {
        return new Notification("child");
    }

    @Override
    public String label() {
        return "child";
    }

    @Override
    public void send(Notification n) {
        System.out.println("Correct child: " + n.text);
    }
}

class AccidentalOverloadSender extends ParentSender {
    public void send(EmailNotification notification) {
        System.out.println("Accidental overload");
    }
}

class EmailNotification extends Notification {
    EmailNotification(String text) {
        super(text);
    }
}

public class Task06_Override {
    public static void main(String[] args) {
        ParentSender a = new CorrectChildSender();
        a.send(new Notification("hello")); 
        ParentSender b = new AccidentalOverloadSender();
        b.send(new Notification("hello")); 
        System.out.println(a.label());
    }
}
