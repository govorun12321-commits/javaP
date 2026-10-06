package prac1;

interface Printable {
    default String format() {
        return "printable";
    }
}

interface Exportable {
    default String format() {
        return "exportable";
    }
}

class DocumentWithResolvedConflict implements Printable, Exportable {
    @Override
    public String format() {
        return Printable.super.format() + " + " + Exportable.super.format();
    }
}

public class Task10_DefaultConflict {
    public static void main(String[] args) {
        DocumentWithResolvedConflict document =
                new DocumentWithResolvedConflict();

        System.out.println(document.format());

    }
}
