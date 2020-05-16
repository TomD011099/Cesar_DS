package Client.Util;

public class CesarString {
    private String string;

    public CesarString(String name) {
        this.string = name;
    }

    public String getString() {
        return string;
    }

    public void setString(String name) {
        this.string = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CesarString cesarString = (CesarString) o;

        return string.equals(cesarString.string);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < string.length(); i++) {
            hash = hash + (int) Math.pow(string.charAt(i), i);
        }
        while ((hash < 0) || (hash > 32768)) {
            if (hash < 0) {
                hash += 32768;
            } else {
                hash -= 32768;
            }
        }
        return hash;
    }

    @Override
    public String toString() {
        return string;
    }

    public int length() {
        return string.length();
    }

    public String subString(int beginIndex, int endIndex) {
        return string.substring(beginIndex, endIndex);
    }
}