package coursehub;


public final class Task04TryCatchFinally {
    static String success() {
        try { return "try"; }
        finally { System.out.println("success: finally"); }
    }

    static String caught() {
        try { throw new IllegalArgumentException("bad input"); }
        catch (IllegalArgumentException e) { System.out.println("catch"); return "caught"; }
        finally { System.out.println("caught: finally"); }
    }

    static void uncaught() {
        try { throw new IllegalStateException("boom"); }
        finally { System.out.println("uncaught: finally"); }
    }

    public static void main(String[] args) {
        System.out.println(success());
        System.out.println(caught());
        try { uncaught(); } catch (IllegalStateException e) { System.out.println("caller: " + e.getMessage()); }
        System.out.println("Если написать return в finally, он перезапишет return/исключение из try или catch.");
    }
}
