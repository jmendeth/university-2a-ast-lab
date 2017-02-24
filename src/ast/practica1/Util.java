package ast.practica1;

/**
 * Random utilities.
 * @author Xavier Mendez
 */
public class Util {

    public static void expect(String a, String b) {
        if ((a == null) ? (b != null) : !a.equals(b))
            throw new AssertionError(
                    String.format("String «%s» doesn't match «%s»", a, b),
                    null);
    }

    public static void expect(boolean a, boolean b) {
        if (a != b)
            throw new AssertionError(
                    String.format("Boolean «%s» doesn't match «%s»", a, b),
                    null);
    }

    public static void expect(boolean a) {
        if (!a)
            throw new AssertionError();
    }

    public static void expect(int a, int b) {
        if (a != b)
            throw new AssertionError(
                    String.format("String «%s» doesn't match «%s»", a, b),
                    null);
    }

    public static void expect(short a, short b) {
        if (a != b)
            throw new AssertionError(
                    String.format("String «%s» doesn't match «%s»", a, b),
                    null);
    }

    public static void expect(long a, long b) {
        if (a != b)
            throw new AssertionError(
                    String.format("String «%s» doesn't match «%s»", a, b),
                    null);
    }

    public static void expect(char a, char b) {
        if (a != b)
            throw new AssertionError(
                    String.format("String «%s» doesn't match «%s»", a, b),
                    null);
    }

    public static void expect(byte a, byte b) {
        if (a != b)
            throw new AssertionError(
                    String.format("String «%s» doesn't match «%s»", a, b),
                    null);
    }

}
